package mesut.parserx.gen.targets;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.gen.Writer;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Range;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static mesut.parserx.gen.LexerGenerator.makeOctal;

public class JavaLexer {
    Template template;
    LexerGenerator gen;
    Options options;
    NFA dfa;

    public void gen(LexerGenerator gen) throws IOException {
        this.gen = gen;
        options = gen.tree.options;
        dfa = gen.dfa;
        template = new Template("lexer.java.template");
        if (options.packageName == null) {
            template.set("package", "");
        }
        else {
            template.set("package", "package " + options.packageName + ";\n");
        }
        template.set("lexer_class", options.lexerClass);
        template.set("token_class", options.tokenClass);
        template.set("next_token", options.lexerFunction);
        template.set("skip_list", NodeList.join(LexerGenerator.makeIntArr(gen.skipList), ","));
        template.set("final_list", NodeList.join(LexerGenerator.makeIntArr(dfa.accepting), ","));

        nameAndId();
        writeTrans();
        cmap();

        File file = new File(options.outDir, options.lexerClass + ".java");
        Utils.write(template.toString(), file);

        writeTokenClass();
    }

    //write char ranges
    private void cmap() {
        Writer cmapWriter = new Writer();
        cmapWriter.print("\"");
        int column = 20;
        int i = 0;
        //sorted ranges for error report
        TreeSet<Map.Entry<Node, Integer>> entries = new TreeSet<>(new Comparator<Map.Entry<Node, Integer>>() {
            @Override
            public int compare(Map.Entry<Node, Integer> o1, Map.Entry<Node, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Iterator<Map.Entry<Node, Integer>> it = dfa.getAlphabet().map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Node, Integer> entry = it.next();
            Range range = entry.getKey().asRange();
            entries.add(entry);
            cmapWriter.print(makeOctal(range.start));
            cmapWriter.print(makeOctal(range.end));
            cmapWriter.print(makeOctal(entry.getValue()));
            i++;
            if (i % column == 0) {
                cmapWriter.print("\"+\n");
                if (it.hasNext()) {
                    cmapWriter.print("            ");
                    cmapWriter.print("\"");//next line start
                }
            }
        }
        cmapWriter.print("\"");
        template.set("cMap", cmapWriter.getString());

        Writer regexWriter = new Writer();
        for (Iterator<Map.Entry<Node, Integer>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<Node, Integer> entry = it.next();
            regexWriter.print("\"");
            regexWriter.print(UnicodeUtils.escapeString(entry.getKey().toString()));
            regexWriter.print("\"");
            if (it.hasNext()) {
                regexWriter.print(", ");
            }
        }
        template.set("cMapRegex", regexWriter.getString());
    }

    void writeTrans() {
        Writer transWriter = new Writer();
        String indent = "        ";
        transWriter.print("\"");
        transWriter.print(makeOctal(dfa.getAlphabet().size()));
        transWriter.print("\" +");
        transWriter.print("\n");
        for (int state : dfa.it()) {
            List<Transition> list = dfa.trans[state];
            transWriter.print(indent);
            transWriter.print("\"");
            if (list == null || list.isEmpty()) {
                transWriter.print(makeOctal(0));
            }
            else {
                transWriter.print(makeOctal(list.size()));
                for (Transition tr : list) {
                    transWriter.print(makeOctal(tr.input));
                    transWriter.print(makeOctal(tr.target));
                }
            }
            transWriter.print("\"");
            if (state <= dfa.lastState - 1) {
                transWriter.print(" +\n");
            }
        }
        template.set("trans", transWriter.getString());
    }

    private void nameAndId() {
        //id list
        Writer idWriter = new Writer();
        for (int state : dfa.it()) {
            idWriter.print(gen.idArr[state]);
            if (state <= dfa.lastState - 1) {
                idWriter.print(",");
                if (state > 0 && state % 20 == 0) {
                    idWriter.print("\n            ");
                }
            }
        }
        template.set("id_list", idWriter.getString());

        //write
        Writer nameWriter = new Writer();
        int i = 0;
        int column = 20;
        for (Map.Entry<Name, Integer> entry : gen.tokens) {
            if (i > 0) {
                nameWriter.print(",");
                if (i % column == 0) {
                    nameWriter.print("\n");
                }
            }
            nameWriter.print("\"" + entry.getKey().name + "\"");
            i++;
        }
        template.set("name_list", nameWriter.getString());
    }

    private void writeTokenClass() throws IOException {
        File out = new File(options.outDir, options.tokenClass + ".java");
        Template template = new Template("token.java.template");

        if (options.packageName == null) {
            template.set("package", "");
        }
        else {
            template.set("package", "package " + options.packageName + ";\n");
        }
        template.set("token_class", options.tokenClass);

        Utils.write(template.toString(), out);
    }
}
