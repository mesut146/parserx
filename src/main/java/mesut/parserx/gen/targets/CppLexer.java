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

public class CppLexer {

    Template source;
    Template header;
    LexerGenerator gen;
    Options options;
    NFA dfa;

    public void gen(LexerGenerator gen) throws IOException {
        this.gen = gen;
        options = gen.tree.options;
        dfa = gen.dfa;
        header = new Template("lexer.h.template");
        source = new Template("lexer.cpp.template");

        writeHeader();
        writeSource();

        tokenHeader();
        tokenSource();
    }

    void writeHeader() throws IOException {
        //todo ns
        header.set("namespace_begin", "");
        header.set("namespace_end", "");
        header.set("lexer_class", options.lexerClass);
        header.set("token_class", options.tokenClass);
        header.set("next_token", options.lexerFunction);
        header.set("skip_list", NodeList.join(LexerGenerator.makeIntArr(gen.skipList), ","));
        header.set("final_list", NodeList.join(LexerGenerator.makeIntArr(dfa.acc()), ","));

        nameAndId();
        writeTrans();
        cmap();

        File file = new File(options.outDir, options.lexerClass + ".h");
        Utils.write(header.toString(), file);
    }

    void writeSource() throws IOException {
        source.set("lexer_class", options.lexerClass);
        source.set("token_class", options.tokenClass);
        source.set("next_token", options.lexerFunction);
        File file = new File(options.outDir, options.lexerClass + ".cpp");
        Utils.write(source.toString(), file);
    }

    void writeTrans() {
        StringBuilder transWriter = new StringBuilder();
        String indent = "        ";
        int len = 0;
        for (var state : dfa.it()) {
            List<Transition> list = state.transitions;
            transWriter.append("\"");
            if (list == null || list.isEmpty()) {
                transWriter.append(makeOctal(0));
                len++;
            }
            else {
                transWriter.append(makeOctal(list.size()));
                len++;
                for (Transition tr : list) {
                    transWriter.append(makeOctal(tr.input));
                    transWriter.append(makeOctal(tr.target.id));
                    len += 2;
                }
            }
            transWriter.append("\"");
            if (state.id <= dfa.lastState - 1) {
                transWriter.append("\n");
                transWriter.append(indent);
            }
        }
        header.set("trans", transWriter.toString());
        source.set("trans_str_len", "" + len);
        header.set("max_trans", "" + dfa.lastState);
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
        header.set("id_list", idWriter.toString());

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
        header.set("name_list", nameWriter.toString());
    }

    //write char ranges
    private void cmap() {
        StringBuilder cmapWriter = new StringBuilder();
        cmapWriter.append("L\"");
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
            cmapWriter.append(makeOctal(range.start));
            cmapWriter.append(makeOctal(range.end));
            cmapWriter.append(makeOctal(entry.getValue()));
            i++;
            if (i % column == 0) {
                cmapWriter.append("\"\n");//end line
                if (it.hasNext()) {
                    cmapWriter.append("            L\"");
                }
            }
        }
        cmapWriter.append("\"");
        header.set("cMap", cmapWriter.toString());

        StringBuilder regexWriter = new StringBuilder();
        for (Iterator<Map.Entry<Node, Integer>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<Node, Integer> entry = it.next();
            regexWriter.append("\"");
            regexWriter.append(UnicodeUtils.escapeString(entry.getKey().toString()));
            regexWriter.append("\"");
            if (it.hasNext()) {
                regexWriter.append(", ");
            }
        }
        header.set("cMapRegex", regexWriter.toString());
        header.set("max_input", "" + dfa.getAlphabet().size());
    }

    private void tokenSource() throws IOException {
        File out = new File(options.outDir, options.tokenClass + ".h");
        Template template = new Template("token.h.template");
        template.set("token_class", options.tokenClass);
        Utils.write(template.toString(), out);
    }

    void tokenHeader() throws IOException {
        File out = new File(options.outDir, options.tokenClass + ".cpp");
        Template template = new Template("token.cpp.template");
        template.set("token_class", options.tokenClass);
        Utils.write(template.toString(), out);
    }
}
