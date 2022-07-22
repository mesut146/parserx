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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//table driven parser gen
public class CodeGen {
    public Options options;
    public LrDFA dfa;
    public LrDFAGen gen;
    List<RuleDecl> all;
    String type;
    IdMap idMap;

    public CodeGen(Tree tree, String type) {
        if (type.equals("lalr") || type.equals("lr1") | type.equals("lr0")) {
            LrDFAGen generator = new LrDFAGen(tree, type);
            generator.generate();
            generator.checkAndReport();
            this.gen = generator;
        }
        else {
            throw new RuntimeException("invalid type: " + type);
        }
        this.options = tree.options;
        this.type = type;
    }

    public CodeGen(LrDFAGen gen, String type) {
        this.gen = gen;
        this.options = gen.tree.options;
        this.type = type;
    }

    public void gen() throws IOException {
        idMap = LexerGenerator.gen(gen.tree, "java").idMap;

        this.dfa = gen.table;
        Template template = new Template("lalr1.java.template");
        template.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        template.set("parser_class", options.parserClass);
        template.set("lexer_class", options.lexerClass);
        template.set("lexer_method", options.lexerFunction);
        template.set("token_class", options.tokenClass);
        ruleId(template);
        writeTable(template);
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(template.toString(), file);
        idMap.writeSym(options);
    }

    //map rule ids to symbol ids
    void ruleId(Template template) {
        StringBuilder ruleIds = new StringBuilder();
        StringBuilder sizes = new StringBuilder();
        all = gen.tree.rules;
        Collections.sort(all, new Comparator<RuleDecl>() {
            @Override
            public int compare(RuleDecl o1, RuleDecl o2) {
                return Integer.compare(o1.index, o2.index);
            }
        });
        for (int i = 0; i < all.size(); i++) {
            RuleDecl decl = all.get(i);
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

    void writeTable(Template template) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        sb.append(pack(dfa.lastId + 1));//state count,width
        sb.append(pack(idMap.lastId + 1));//symbol count,height

        //write accept
        LrItemSet acc = dfa.acc;
        sb.append(pack(acc.stateId));
        if (type.equals("lr0")) {
            //all tokens acc
            sb.append(pack(dfa.tokens.size()));
            for (Name tok : dfa.tokens) {
                sb.append(pack(idMap.getId(tok)));
            }
        }
        else {
            sb.append(pack(1));
            for (LrItem item : acc.kernel) {
                if (item.hasReduce() && item.rule.ref.equals(gen.start.ref) && item.lookAhead.contains(LrDFAGen.dollar)) {
                    sb.append(pack(idMap.getId(IdMap.EOF)));
                    break;
                }
            }
        }


        for (var set : dfa.itemSets) {
            //write shifts
            List<? extends LrTransition> shifts = set.transitions;
            sb.append(pack(shifts.size()));//shift count
            for (LrTransition tr : shifts) {
                sb.append(pack(idMap.getId(tr.symbol)));
                int action = tr.to.stateId;
                sb.append(pack(action));
            }
            //write reduces
            List<LrItem> list = set.getReduce();
            int count = list.size();//reduce count
            if (acc == set) {
                count--;
            }
            sb.append(pack(count));
            for (LrItem reduce : list) {
                if (reduce.rule.ref.equals(gen.start.ref)) {
                    continue;
                }
                int action = reduce.rule.index;
                sb.append(pack(action));
                sb.append(pack(reduce.lookAhead.size()));
                for (Name sym : reduce.lookAhead) {
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
