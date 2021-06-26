package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.utils.IOUtils;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CodeGen {
    public Options options;
    LrDFA<?> dfa;
    LRGen<?> gen;
    HashMap<Name, Integer> ids = new HashMap<>();
    Name EOF = new Name("EOF", true);
    List<RuleDecl> all;
    LexerGenerator lexerGenerator;

    public CodeGen(LRGen<?> gen, Options options) {
        this.gen = gen;
        this.options = options;
    }

    public void gen() throws IOException {
        lexerGenerator = new LexerGenerator(gen.tree, options);
        lexerGenerator.generate();

        this.dfa = gen.table;
        Template template = new Template("lalr1.java.template");
        template.set("parser_class", options.parserClass);
        template.set("lexer_class", options.lexerClass);
        template.set("lexer_method", options.lexerFunction);
        genIds();
        ruleId(template);
        writeTable(template);
        File file = new File(gen.dir, options.parserClass + ".java");
        IOUtils.write(template.toString(), file);
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

    int getId(Name name){
        if (name.isToken){
            return lexerGenerator.idMap.get(name.name);
        }
        return ids.get(name);
    }

    void writeTable(Template template) {
        StringBuilder sb = new StringBuilder();

        int width = dfa.tokens.size() + dfa.rules.size();
        lexerGenerator.idMap.put("EOF", 0);
        sb.append("\"");
        for (int state = 0; state <= dfa.lastId; state++) {
            //write shifts
            for (LrTransition<?> tr : dfa.getTrans(state)) {
                sb.append(pack(getId(tr.symbol)));
                int action = dfa.getId(tr.to);
                sb.append(pack(action));
            }
            //write reduces
            List<LrItem> list = dfa.getSet(state).getReduce();
            sb.append(pack(list.size()));//reduce count
            for (LrItem reduce : list) {
                int action = -reduce.rule.index;
                sb.append(pack(action));
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
        sb.append(field(gen.start.name, ids.get(gen.start.ref())));
        for (Name rule : dfa.rules) {
            sb.append(field(rule.name, ids.get(rule)));
        }

        sb.append("\n}");

        File file = new File(options.outDir, "sym.java");
        IOUtils.write(sb.toString(), file);
    }

    String field(String name, int id) {
        return String.format("  public static int %s = %d;\n", name, id);
    }

}
