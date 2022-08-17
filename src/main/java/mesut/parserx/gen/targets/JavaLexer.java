package mesut.parserx.gen.targets;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
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
        template.set("final_list", NodeList.join(LexerGenerator.makeIntArr(dfa.acc()), ","));

        nameAndId();
        writeTrans();
        cmap();

        File file = new File(options.outDir, options.lexerClass + ".java");
        Utils.write(template.toString(), file);

        writeTokenClass();
    }

    //write char ranges
    private void cmap() {
        StringBuilder cmapWriter = new StringBuilder();
        cmapWriter.append("\"");
        int column = 20;
        int i = 0;
        //sorted ranges for error report
        TreeSet<Map.Entry<Node, Integer>> entries = new TreeSet<>(Map.Entry.comparingByValue());
        for (Iterator<Map.Entry<Node, Integer>> it = dfa.getAlphabet().map.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            Range range = entry.getKey().asRange();
            entries.add(entry);
            cmapWriter.append(makeOctal(range.start));
            cmapWriter.append(makeOctal(range.end));
            cmapWriter.append(makeOctal(entry.getValue()));
            i++;
            if (i % column == 0) {
                if (it.hasNext()) {
                    cmapWriter.append("\"+\n");
                    cmapWriter.append("            ");
                    cmapWriter.append("\"");//next line start
                }
            }
        }
        cmapWriter.append("\"");
        template.set("cMap", cmapWriter.toString());

        StringBuilder regexWriter = new StringBuilder();
        for (var it = entries.iterator(); it.hasNext(); ) {
            var entry = it.next();
            regexWriter.append("\"");
            regexWriter.append(UnicodeUtils.escapeString(entry.getKey().toString()));
            regexWriter.append("\"");
            if (it.hasNext()) {
                regexWriter.append(", ");
            }
        }
        template.set("cMapRegex", regexWriter.toString());
    }

    void writeTrans() {
        StringBuilder transWriter = new StringBuilder();
        String indent = "        ";
        transWriter.append("\"");
        transWriter.append(makeOctal(dfa.getAlphabet().size()));
        transWriter.append("\" +\n");
        for (var state : dfa.it()) {
            List<Transition> list = state.transitions;
            transWriter.append(indent);
            transWriter.append("\"");
            if (list == null || list.isEmpty()) {
                transWriter.append(makeOctal(0));
            }
            else {
                transWriter.append(makeOctal(list.size()));
                for (Transition tr : list) {
                    transWriter.append(makeOctal(tr.input));
                    transWriter.append(makeOctal(tr.target.id));
                }
            }
            transWriter.append("\"");
            if (state.id <= dfa.lastState - 1) {
                transWriter.append(" +\n");
            }
        }
        template.set("trans", transWriter.toString());
    }

    private void nameAndId() {
        //id list
        StringBuilder idWriter = new StringBuilder();
        for (var state : dfa.it()) {
            idWriter.append(gen.idArr[state.id]);
            if (state.id <= dfa.lastState - 1) {
                idWriter.append(",");
                if (state.id > 0 && state.id % 20 == 0) {
                    idWriter.append("\n            ");
                }
            }
        }
        template.set("id_list", idWriter.toString());

        //write
        StringBuilder nameWriter = new StringBuilder();
        int i = 0;
        int column = 20;
        for (Map.Entry<Name, Integer> entry : gen.tokens) {
            if (i > 0) {
                nameWriter.append(",");
                if (i % column == 0) {
                    nameWriter.append("\n");
                }
            }
            nameWriter.append("\"").append(entry.getKey().name).append("\"");
            i++;
        }
        template.set("name_list", nameWriter.toString());
    }

    private void writeTokenClass() throws IOException {
        Template template = new Template("token.java.template");
        if (options.packageName == null) {
            template.set("package", "");
        }
        else {
            template.set("package", "package " + options.packageName + ";\n");
        }
        template.set("token_class", options.tokenClass);
        File out = new File(options.outDir, options.tokenClass + ".java");
        Utils.write(template.toString(), out);
    }
}
