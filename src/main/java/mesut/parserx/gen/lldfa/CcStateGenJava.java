package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ParserUtils;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import static mesut.parserx.gen.ParserUtils.dollar;
import static mesut.parserx.gen.lexer.LexerGenerator.makeOctal;
import static mesut.parserx.gen.lldfa.CcGenJava.ruleHeader;

public class CcStateGenJava {
    Tree tree;
    Options options;
    CodeWriter w = new CodeWriter(true);
    LLDfaBuilder builder;
    //rule name -> index
    HashMap<Name, Integer> indexMap = new HashMap<>();
    IdMap idMap;
    static final int INVALID_STATE = -1;

    public CcStateGenJava(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public void gen() throws IOException {
        new RecursionHandler(tree).handleAll();
        LLDfaBuilder.cc = true;
        ItemSet.forceClosure = true;
        builder = new LLDfaBuilder(tree);
        builder.factor();
        ParserUtils.genTokenType(tree);
        CcGenJava.writeTS(options);
        idMap = LexerGenerator.gen(tree, Lang.JAVA).idMap;

        header();
        writeUnpack();
        writeDecider();
        for (var entry : builder.rules.entrySet()) {
            var rule = entry.getKey();

            var decl = tree.getRule(rule);
            w.append("public %s %s throws IOException{", decl.retType, ruleHeader(decl));
            w.append("%s res = new %s();", decl.retType, decl.retType);

            var rhs = decl.rhs.asOr();
            w.append("switch(decide(%s)){", indexMap.get(rule));
            for (int i = 1; i <= rhs.size(); i++) {
                var ch = rhs.get(i - 1);
                w.append("case %d:{", i);
                var n = new NormalWriter(w, tree);
                n.curRule = decl;
                ch.accept(n, null);
                w.append("break;");
                w.append("}");
            }
            w.append("}");//switch
            w.append("return res;");
            w.append("}");
        }
        CcGenJava.writeRest(tree, builder, w);

        w.append("}");
        var file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(w.get(), file);
    }

    void header() {
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("import java.io.IOException;");
        w.append("");
        w.append("public class %s{", options.parserClass);

        w.append("TokenStream ts;", options.lexerClass);
        w.append("static String trans_packed = %s;", getPacked());
        //w.append("static int[][][] trans = %s;", genTrans());
        w.append("static int[][][] trans;");
        w.append("");

        w.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);
        w.all("this.ts = new TokenStream(lexer);\n}");
        w.append("");
    }

    void writeUnpack() {
        w.append("static{");
        w.append("int pos = 0;");
        w.append("int ruleCount = trans_packed.charAt(pos++);");
        w.append("int tokenCount = trans_packed.charAt(pos++);");
        w.append("trans = new int[ruleCount][][];");

        w.append("for(int rule = 0;rule < ruleCount;rule++){");
        w.append("int maxState = trans_packed.charAt(pos++);");
        w.append("int[][] arr = new int[maxState + 1][tokenCount];");
        w.append("trans[rule] = arr;");
        w.append("for(int state = 1;state <= maxState;state++){");
        w.append("int trCount = trans_packed.charAt(pos++);");
        w.append("for(int tr = 0;tr < trCount;tr++){");
        w.append("int token = trans_packed.charAt(pos++);");
        w.append("int action = trans_packed.charAt(pos++);");
        w.append("arr[state][token] = action;");
        w.append("}");//tr
        w.append("}");//state
        w.append("}");//rule
        w.append("}");
    }

    void writeDecider() {
        //last 6 bit is alt number max: 2^6-1=63
        //first 25 bit is target state max: 67million
        w.append("public int decide(int ruleIndex) throws IOException{");
        w.append("int[][] map = trans[ruleIndex];");
        w.append("int alt = 0;");
        w.append("int state = 1;");
        w.append("while(true){");
        w.append("int act = map[state][ts.la.type];");
        w.append("if(act == 0){");
        w.append("ts.pop();");
        w.append("break;");
        w.append("}");
        w.append("int tmpAlt = act & ((1 << 6) - 1);");
        w.append("alt = tmpAlt == 0 ? alt : tmpAlt;");
        w.append("state = act >>> 6;");
        w.append("ts.pop();");
        w.append("}");
        w.append("if(alt == 0) throw new RuntimeException(\"parse error: no alt\");");
        w.append("ts.unmark();");
        w.append("return alt;");
        w.all("}\n");
    }

    String getPacked() {
        var sb = new StringBuilder();
        sb.append("\"");
        sb.append(makeOctal(builder.rules.size()));

        //token count
        sb.append(makeOctal(idMap.lastTokenId + 1));

        int index = 0;
        for (var e : builder.rules.entrySet()) {
            indexMap.put(e.getKey(), index);
            //shrink state ids to have less space
            //old id -> new id
            var stateIdMap = new HashMap<Integer, Integer>();
            int id = 0;
            for (var state : e.getValue()) {
                stateIdMap.put(state.stateId, ++id);
            }

            sb.append(makeOctal(id));//max state

            var sorted = e.getValue().stream()
                    .sorted(Comparator.comparingInt(s -> stateIdMap.get(s.stateId)))
                    .collect(Collectors.toList());

            for (var state : sorted) {
                sb.append(makeOctal(state.transitions.size()));
                for (var tr : state.transitions) {
                    var sym = idMap.getId(tr.symbol.asName());
                    sb.append(makeOctal(sym));
                    //acc with EOF to ?
                    var targetSet = tr.target;
                    int target = stateIdMap.get(targetSet.stateId);
                    //int alt = builder.findWhich(targetSet, e.getKey());
                    int alt = targetSet.which.get();
                    sb.append(makeOctal(alt | (target << 6)));
                }
            }
            index++;
        }
        sb.append("\"");
        return sb.toString();
    }

    String genTrans() {
        int index = 0;
        var sb = new StringBuilder();
        sb.append("{");
        boolean firstRule = true;
        boolean firstState;
        boolean firstLa;
        for (var e : builder.rules.entrySet()) {
            if (!firstRule) {
                sb.append(",");
            }
            firstRule = false;
            sb.append("\n");
            sb.append("{");//rule index
            indexMap.put(e.getKey(), index);
            //shrink state ids to have less space
            //old id -> new id
            var stateIdMap = new HashMap<Integer, Integer>();
            int id = -1;
            for (var state : e.getValue()) {
                stateIdMap.put(state.stateId, ++id);
            }
            firstState = true;
            var sorted = e.getValue().stream().sorted(Comparator.comparingInt(s -> stateIdMap.get(s.stateId)));
            for (var state : sorted.collect(Collectors.toList())) {
                sb.append("\n");
                if (!firstState) {
                    sb.append(",");
                }
                firstState = false;
                sb.append("{");//state
                //la row
                firstLa = true;
                int col = 0;
                for (int tok = 0; tok <= idMap.lastTokenId; tok++) {
                    ItemSet targetSet = null;
                    for (var tr : state.transitions) {
                        if (idMap.getId(tr.symbol.asName()) == tok) {
                            targetSet = tr.target;
                            break;
                        }
                    }
                    if (!firstLa) {
                        sb.append(",");
                    }
                    firstLa = false;
                    //acc with EOF to ?
                    if (targetSet != null) {
                        int target = stateIdMap.get(targetSet.stateId);
                        //int alt = builder.findWhich(targetSet, e.getKey());
                        int alt = targetSet.which.get();
                        sb.append(alt | (target << 6));
                    }
                    else {
                        //exit?
                        if (isExit(state, idMap.getName(tok))) {
                            sb.append("-2");
                        }
                        else {
                            sb.append("-1");
                        }
                    }
                    col++;
                    if (col == 200) {
                        sb.append("\n");
                        col = 0;
                    }
                }
                sb.append("}");
            }
            sb.append("}");
            index++;
        }
        sb.append("}");
        return sb.toString();
    }

    boolean isExit(ItemSet set, Name la) {
        if (la.equals(IdMap.EOF)) {
            la = dollar;
        }
        for (var item : set.all) {
            if (item.isFinalReduce(tree) && item.lookAhead.contains(la)) {
                return true;
            }
        }
        return false;
    }


}
