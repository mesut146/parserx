package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

//table driven parser gen
public class CodeGen {
    public Options options;
    public LrDFA dfa;
    public LrDFAGen gen;
    String type;
    IdMap idMap;
    Template template;

    public CodeGen(Tree tree, String type) {
        if (!type.equals("lalr") && !type.equals("lr1")) {
            throw new RuntimeException("invalid type: " + type);
        }
        this.gen = new LrDFAGen(tree, type);
        this.options = tree.options;
        this.type = type;
        gen.generate();
        gen.checkAndReport();
    }

    public CodeGen(LrDFAGen gen, String type) {
        this.gen = gen;
        this.options = gen.tree.options;
        this.type = type;
    }

    public void gen() throws IOException {
        idMap = LexerGenerator.gen(gen.tree, "java").idMap;

        this.dfa = gen.table;
        template = new Template("lalr1.java.template");
        template.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        template.set("parser_class", options.parserClass);
        template.set("lexer_class", options.lexerClass);
        template.set("lexer_method", options.lexerFunction);
        template.set("token_class", options.tokenClass);
        ruleId();
        writeTable();
        names();
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(template.toString(), file);
        idMap.writeSym(options);

        var symbolTemplate = new Template("Symbol.java.template");
        symbolTemplate.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        symbolTemplate.set("token_class", options.tokenClass);
        Utils.write(symbolTemplate.toString(), new File(options.outDir, "Symbol.java"));
    }

    void names() {
        Set<RuleDecl> all = new TreeSet<>(Comparator.comparingInt(rd -> rd.index));
        for (var list : gen.treeInfo.ruleMap.values()) {
            all.addAll(list);
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (var rd : all) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"').append(rd.getName()).append('"');
            i++;
        }
        template.set("names", sb.toString());
    }

    //map rule ids to symbol ids
    void ruleId() {
        StringBuilder ruleIds = new StringBuilder();
        StringBuilder sizes = new StringBuilder();

        var all = new ArrayList<RuleDecl>();
        for (var rd : gen.treeInfo.ruleMap.values()) {
            all.addAll(rd);
        }

        all.sort(Comparator.comparingInt(o -> o.index));
        for (int i = 0; i < all.size(); i++) {
            var decl = all.get(i);
            ruleIds.append(idMap.getId(decl.ref));
            if (LrItem.isEpsilon(decl)) {
                sizes.append(0);
            }
            else {
                sizes.append(decl.rhs.asSequence().size());
            }
            if (i < all.size() - 1) {
                ruleIds.append(", ");
                sizes.append(", ");
            }
        }
        template.set("ruleIds", ruleIds.toString());
        template.set("rhs_sizes", sizes.toString());
    }

    void writeTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        sb.append(pack(dfa.lastId + 1));//state count,width
        sb.append(pack(idMap.lastId + 1));//symbol count,height

        //write accept
        LrItemSet acc = dfa.acc;
        sb.append(pack(acc.stateId));
        sb.append(pack(1));
        for (var item : acc.kernel) {
            if (item.isReduce(gen.tree) && item.rule.ref.equals(gen.start.ref) && item.lookAhead.contains(LrDFAGen.dollar)) {
                sb.append(pack(idMap.getId(IdMap.EOF)));
                break;
            }
        }

        for (var set : dfa.itemSets) {
            //write shifts
            List<? extends LrTransition> shifts = set.transitions;
            sb.append(pack(shifts.size()));//shift count
            for (LrTransition tr : shifts) {
                sb.append(pack(idMap.getId(tr.symbol)));
                int action = tr.target.stateId;
                sb.append(pack(action));
            }
            //write reduces
            List<LrItem> list = set.getReduce();
            int count = list.size();//reduce count
            if (acc == set) {
                count--;
            }
            sb.append(pack(count));
            for (var reduce : list) {
                if (reduce.rule.ref.equals(gen.start.ref)) {
                    continue;
                }
                int action = reduce.rule.index;
                sb.append(pack(action));
                sb.append(pack(reduce.lookAhead.size()));
                for (var sym : reduce.lookAhead) {
                    sb.append(pack(idMap.getId(sym)));
                }
            }
        }

        sb.append("\"");
        template.set("table_packed", sb.toString());
    }

    String pack(int i) {
        if (i < 0) {
            return "-" + UnicodeUtils.escapeUnicode(-i);
        }
        return UnicodeUtils.escapeUnicode(i);
    }

}
