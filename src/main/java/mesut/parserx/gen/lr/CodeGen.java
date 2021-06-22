package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.NameNode;
import mesut.parserx.utils.IOUtils;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CodeGen {
    public LexerGenerator lexerGenerator;
    public String parser_class = "Parser";
    String dir;
    LrDFA<?> dfa;
    LRGen<?> gen;
    HashMap<NameNode, Integer> ids = new HashMap<>();

    public CodeGen(LRGen<?> gen) {
        this.gen = gen;
    }

    public void gen() throws IOException {
        this.dfa = gen.table;
        Template template = new Template("lalr1.java.template");
        template.set("parser_class", parser_class);
        template.set("lexer_class", lexerGenerator.className);
        template.set("lexer_method", lexerGenerator.functionName);
        writeRhs(template);
        writeTable(template);
        File file = new File(gen.dir, parser_class + ".java");
        IOUtils.write(template.toString(), file);
        writeSym();
    }

    void genIds() {
        int id = 0;
        for (NameNode token : dfa.tokens) {
            ids.put(token, id++);
        }
        for (NameNode rule : dfa.rules) {
            ids.put(rule, id++);
        }
        ids.put(gen.start.ref(), id++);
        ids.put(new NameNode("EOF", true), id);
    }

    void writeTable(Template template) {
        StringBuilder sb = new StringBuilder();

        int width = dfa.tokens.size() + dfa.rules.size() + 1;
        sb.append("\"");
        for (int i = 0; i <= dfa.lastId; i++) {
            sb.append(pack(dfa.getTrans(i).size()));//shift count
            //write shifts
            for (LrTransition<?> tr : dfa.getTrans(i)) {
                int action = dfa.getId(tr.to);
                sb.append(pack(action));
            }
            //write reduces
            List<LrItem> list = dfa.getSet(i).getReduce();
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
        genIds();
        StringBuilder sb = new StringBuilder();

        sb.append("public class sym{\n");

        sb.append("  //tokens\n");
        int id = 0;
        for (NameNode token : dfa.tokens) {
            sb.append(field(token.name, id++));
        }
        sb.append("  //rules\n");
        sb.append(field("START$", id++));
        for (NameNode rule : dfa.rules) {
            sb.append(field(rule.name, id++));
        }
        sb.append(field("EOF", id));

        sb.append("\n}");

        File file = new File(dir, "sym.java");
        IOUtils.write(sb.toString(), file);
    }

    String field(String name, int id) {
        return String.format("  public static int %s = %d;\n", name, id);
    }

    void writeRhs(Template template) {
        StringBuilder sb = new StringBuilder();
        /*for (NameNode rule : dfa.rules) {
            RuleDecl decl = gen.tree.rules.get(ids.get(rule));
            sb.append(decl.index);
        }*/
        template.set("rhs_sizes", sb.toString());
    }
}
