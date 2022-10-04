package mesut.parserx.gen.lexer;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Range;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static mesut.parserx.gen.lexer.LexerGenerator.makeOctal;

public class JavaLexer {
    Template template;
    LexerGenerator gen;
    Options options;
    NFA dfa;

    public JavaLexer(LexerGenerator gen) {
        this.gen = gen;
        options = gen.tree.options;
        dfa = gen.dfa;
    }

    public void gen() throws IOException {
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
        template.set("skip_list", NodeList.join(gen.skipList(), ","));
        template.set("final_list", NodeList.join(gen.acc(), ","));
        template.set("more", NodeList.join(gen.more(), ","));

        if (gen.tree.lexerMembers != null) {
            var sb = new StringBuilder();
            for (var member : gen.tree.lexerMembers.members) {
                sb.append("    ").append(member).append("\n");
            }
            template.set("members", sb.toString());
        }
        else {
            template.set("members", "");
        }

        nameAndId();
        writeTrans();
        cmap();
        writeModes();
        writeActions();

        File file = new File(options.outDir, options.lexerClass + ".java");
        Utils.write(template.toString(), file);

        writeTokenClass();
    }

    private void writeActions() {
        if (gen.actions == null) {
            template.set("actionCases", "");
        }
        else {
            var sb = new StringBuilder();
            for (var state : dfa.it()) {
                if (!state.accepting) continue;
                var action = gen.actions[state.id];
                if (action == null) continue;
                sb.append(String.format("case %d:{\n", state.id));
                sb.append(action.substring(2, action.length() - 2));
                sb.append("\n");
                sb.append("break;\n");
                sb.append("}\n");
            }
            template.set("actionCases", sb.toString());
        }
    }

    private void writeModes() {
        var sb = new StringBuilder();
        for (var mode : dfa.modes.entrySet()) {
            sb.append(String.format("    static final int %s = %d;\n", mode.getKey(), mode.getValue().id));
        }
        template.set("modes", sb.toString());

        //mode_map
        sb.setLength(0);
        for (int i = 0; i < gen.mode_arr.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            var mode = gen.mode_arr[i];
            sb.append(mode);
        }
        template.set("mode_map", sb.toString());

        //modenames
        sb.setLength(0);
        for (var mode : gen.dfa.modes.entrySet()) {
            sb.append(String.format("            case %d: return \"%s\";\n", mode.getValue().id, mode.getKey()));
        }
        template.set("printModes", sb.toString());
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
        var idWriter = new StringBuilder();
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
        var nameWriter = new StringBuilder();
        int i = 0;
        int column = 20;
        for (var entry : gen.tokens) {
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
        var template = new Template("token.java.template");
        if (options.packageName == null) {
            template.set("package", "");
        }
        else {
            template.set("package", "package " + options.packageName + ";\n");
        }
        template.set("token_class", options.tokenClass);
        var out = new File(options.outDir, options.tokenClass + ".java");
        Utils.write(template.toString(), out);
    }
}
