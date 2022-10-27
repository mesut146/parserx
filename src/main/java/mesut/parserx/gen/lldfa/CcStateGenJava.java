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
        LLDfaBuilder.noshrink = true;
        LLDfaBuilder.skip = true;
        ItemSet.forceRuleClosure = true;
        builder = new LLDfaBuilder(tree);
        builder.factor();
        ParserUtils.genTokenType(tree);
        CcGenJava.writeTS(options);
        idMap = LexerGenerator.gen(tree, Lang.JAVA).idMap;

        header();
        writeDecider();
        for (var entry : builder.rules.entrySet()) {
            var rule = entry.getKey();

            var decl = tree.getRule(rule);
            w.append("public %s %s() throws IOException{", decl.retType, rule.name);
            w.append("%s res = new %s();", decl.retType, decl.retType);

            var rhs = decl.rhs.asOr();
            w.append("switch(decide(%s)){", indexMap.get(rule));
            for (int i = 1; i <= rhs.size(); i++) {
                var ch = rhs.get(i - 1);
                w.append("case %d:{", i);
                w.append("ts.unmark();");
                CcGenJava.alt(ch, i, w);
                var n = new NormalWriter(w, tree);
                ch.accept(n, null);
                w.append("break;");
                w.append("}");
            }
            w.append("}");//switch
            w.append("return res;");
            w.append("}");
        }
        //writeRest
        for (var decl : tree.rules) {
            if (builder.rules.containsKey(decl.ref)) continue;
            w.append("public %s %s() throws IOException{", decl.retType, decl.getName());
            w.append("%s res = new %s();", decl.retType, decl.retType);
            var nw = new NormalWriter(w, tree);
            decl.rhs.accept(nw, null);
            w.append("return res;");
            w.append("}");
        }

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
        w.append("static int[][][] trans = %s;", genTrans());
        w.append("");

        w.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);
        w.all("this.ts = new TokenStream(lexer);\n}");
        w.append("");
    }

    String genTrans() {
        int index = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean firstRule = true;
        boolean firstState;
        boolean firstLa;
        for (var e : builder.rules.entrySet()) {
            if (!firstRule) {
                sb.append(",");
            }
            firstRule = false;
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
                if (!firstState) {
                    sb.append(",");
                }
                firstState = false;
                sb.append("{");//state
                //la row
                firstLa = true;
                for (int tok = 0; tok <= idMap.lastTokenId; tok++) {
                    ItemSet targetSet = null;
                    for (var tr : state.transitions) {
                        if (idMap.getId(tr.symbol.asName()) == tok) {
                            targetSet = tr.target;
                            break;
                        }
                    }
                    if (!firstLa)
                        sb.append(",");
                    firstLa = false;
                    //acc with EOF to ?
                    if (targetSet != null) {
                        int target = stateIdMap.get(targetSet.stateId);
                        int alt = findWhich(targetSet, e.getKey());
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

    int findWhich(ItemSet target, Name curRule) {
        var res = target.all.stream().filter(set -> isFinal(set, curRule)).findFirst();
        if (res.isPresent()) return res.get().rule.which;
        return 0;
    }

    boolean isFinal(Item item, Name curRule) {
        return item.isReduce(tree) && item.rule.ref.equals(curRule) && item.lookAhead.contains(dollar);
    }

    void writeDecider() {
        //last 6 bit is alt number max: 2^6-1=63
        //first 25 bit is target state max: 67million
        w.append("public int decide(int ruleIndex) throws IOException{");
        w.append("int[][] map = trans[ruleIndex];");
        w.append("int alt = 0;");
        w.append("int state = 0;");
        w.append("while(true){");
        w.append("int tmp = map[state][ts.la.type];");
        w.append("if(tmp == -1) throw new RuntimeException(\"parse error: \" + ts.la);");
        w.append("else if(tmp == -2){");
        w.append("ts.pop();");
        w.append("break;");
        w.append("}");
        w.append("alt = tmp & ((1 << 6) - 1);");
        w.append("state = tmp >>> 6;");
        w.append("ts.pop();");
        w.append("}");
        w.append("if(alt == 0) throw new RuntimeException(\"parse error: no alt\");");
        w.append("return alt;");
        w.all("}\n");
    }
}
