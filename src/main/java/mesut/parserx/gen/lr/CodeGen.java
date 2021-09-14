package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CodeGen {
    public Options options;
    LrDFA<?> dfa;
    LRTableGen<?> gen;
    HashMap<Name, Integer> ids = new HashMap<>();
    Name EOF = new Name("EOF", true);
    List<RuleDecl> all;
    LexerGenerator lexerGenerator;
    boolean islr0;

    public CodeGen(LRTableGen<?> gen, boolean islr0) {
        this.gen = gen;
        this.options = gen.tree.options;
        this.islr0 = islr0;
    }

    public void gen() throws IOException {
        lexerGenerator = new LexerGenerator(gen.tree);
        lexerGenerator.generate();

        this.dfa = gen.table;
        Template template = new Template("lalr1.java.template");
        template.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        template.set("parser_class", options.parserClass);
        template.set("lexer_class", options.lexerClass);
        template.set("lexer_method", options.lexerFunction);
        template.set("token_class", options.tokenClass);
        genIds();
        ruleId(template);
        writeTable(template);
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(template.toString(), file);
        writeSym();
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
            ruleIds.append(ids.get(all.get(i).ref()));
            sizes.append(all.get(i).rhs.asSequence().size());
            if (i < all.size() - 1) {
                ruleIds.append(", ");
                sizes.append(", ");
            }
        }
        template.set("ruleIds", ruleIds.toString());
        template.set("rhs_sizes", sizes.toString());
    }

    //symbol ids
    void genIds() {
        int id = lexerGenerator.idMap.size() + 1;
        for (Name rule : dfa.rules) {
            ids.put(rule, id++);
        }
        ids.put(gen.start.ref(), id);
    }

    int getId(Name name) {
        if (name.isToken) {
            if (name.name.equals("$")) {
                return lexerGenerator.idMap.get("EOF");
            }
            return lexerGenerator.idMap.get(name.name);
        }
        return ids.get(name);
    }

    void writeTable(Template template) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        sb.append(pack(dfa.lastId + 1));//state count,width
        sb.append(pack(dfa.tokens.size() + dfa.rules.size() + 2));//symbol count,height

        //write accept
        sb.append(pack(gen.acc));
        LrItemSet acc = dfa.getSet(gen.acc);
        if (islr0) {
            //all tokens acc
            sb.append(pack(dfa.tokens.size()));
            for (Name tok : dfa.tokens) {
                sb.append(pack(getId(tok)));
            }
        }
        else {
            sb.append(pack(1));
            for (LrItem item : acc.kernel) {
                if (item.hasReduce() && item.rule.name.equals(gen.start.name) && item.lookAhead.contains(LRTableGen.dollar)) {
                    sb.append(pack(getId(EOF)));
                    break;
                }
            }
        }


        for (int state = 0; state <= dfa.lastId; state++) {
            //write shifts
            List<? extends LrTransition<?>> shifts = dfa.getTrans(state);
            sb.append(pack(shifts.size()));//shift count
            for (LrTransition<?> tr : shifts) {
                sb.append(pack(getId(tr.symbol)));
                int action = dfa.getId(tr.to);
                sb.append(pack(action));
            }
            //write reduces
            List<LrItem> list = dfa.getSet(state).getReduce();
            int count = list.size();//reduce count
            if (acc == dfa.getSet(state)) {
                count--;
            }
            sb.append(pack(count));
            for (LrItem reduce : list) {
                if (reduce.rule.name.equals(gen.start.name)) {
                    continue;
                }
                int action = reduce.rule.index;
                sb.append(pack(action));
                sb.append(pack(reduce.lookAhead.size()));
                for (Name sym : reduce.lookAhead) {
                    sb.append(pack(getId(sym)));
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

    public void writeSym() throws IOException {
        StringBuilder sb = new StringBuilder();

        if (options.packageName != null) {
            sb.append("package ").append(options.packageName).append(";\n\n");
        }

        sb.append("public class sym{\n");

        sb.append("  //tokens\n");
        sb.append(field("EOF", 0));
        for (Name token : dfa.tokens) {
            if (token.name.equals("$")) {
                continue;
            }
            sb.append(field(token.name, lexerGenerator.idMap.get(token.name)));
        }
        sb.append("  //rules\n");
        //sb.append(field(gen.start.name, ids.get(gen.start.ref())));
        for (Name rule : dfa.rules) {
            sb.append(field(rule.name, ids.get(rule)));
        }

        sb.append("\n}");

        File file = new File(options.outDir, "sym.java");
        Utils.write(sb.toString(), file);
    }

    String field(String name, int id) {
        return String.format("  public static int %s = %d;\n", name, id);
    }

}
