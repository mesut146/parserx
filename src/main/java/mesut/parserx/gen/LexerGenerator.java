package mesut.parserx.gen;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LexerGenerator {
    public IdMap idMap = new IdMap();
    public NFA dfa;
    public Tree tree;
    Options options;
    Template template;

    public LexerGenerator(Tree tree) {
        this.tree = tree;
        this.dfa = tree.makeNFA().dfa();
        this.dfa = Minimization.optimize(this.dfa);
        this.options = tree.options;
    }

    //compress boolean bits to integers
    public static int[] makeIntArr(boolean[] arr) {
        int[] res = new int[arr.length / 32 + 1];
        int pos = 0;
        int cur;
        for (int start = 0; start < arr.length; start += 32) {
            cur = 0;
            for (int j = 0; j < 32 && start + j < arr.length; j++) {
                int bit = arr[start + j] ? 1 : 0;
                cur |= bit << j;
            }
            res[pos++] = cur;
        }
        return res;
    }

    public static String makeOctal(int val) {
        if (val <= 255) {
            return "\\" + Integer.toOctalString(val);
        }
        return UnicodeUtils.escapeUnicode(val);
    }

    public void generate() throws IOException {
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
        makeTables();

        File file = new File(options.outDir, options.lexerClass + ".java");
        Utils.write(template.toString(), file);

        writeTokenClass();
    }

    void makeTables() {
        makeTrans();
        nameAndId();
        template.set("final_list", NodeList.join(makeIntArr(dfa.accepting), ","));
        cmap();
        skipList();
    }

    private void skipList() {
        boolean[] arr = new boolean[idMap.lastTokenId + 1];
        for (int id = 1; id <= idMap.lastTokenId; id++) {
            Name tok = idMap.getName(id);
            if (tree.getToken(tok.name).isSkip) {
                arr[id] = true;
            }
        }
        template.set("skip_list", NodeList.join(makeIntArr(arr), ","));
    }

    private void nameAndId() {
        //generate name and id list
        idMap.genSymbolIds(dfa.tree);

        int[] idArr = new int[dfa.lastState + 1];//state->id
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            //make id for token
            String name = dfa.names[state];
            if (name != null && dfa.isAccepting(state)) {
                //!dfa.isSkip[state]
                idArr[state] = idMap.getId(new Name(name, true));
            }
        }
        //id list
        Writer idWriter = new Writer();
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            idWriter.print(idArr[state]);
            if (state <= dfa.lastState - 1) {
                idWriter.print(",");
                if (state > dfa.initial && state % 20 == 0) {
                    idWriter.print("\n            ");
                }
            }
        }
        template.set("id_list", idWriter.getString());
        //sort tokens by id
        TreeSet<Map.Entry<Name, Integer>> tokens = new TreeSet<>(new Comparator<Map.Entry<Name, Integer>>() {
            @Override
            public int compare(Map.Entry<Name, Integer> o1, Map.Entry<Name, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<Name, Integer> entry : idMap.map.entrySet()) {
            if (entry.getKey().isToken) {
                tokens.add(entry);
            }
        }
        //write
        Writer nameWriter = new Writer();
        int i = 0;
        int column = 20;
        for (Map.Entry<Name, Integer> entry : tokens) {
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

    private void cmap() {
        Writer cmapWriter = new Writer();
        cmapWriter.print("\"");
        int column = 20;
        int i = 0;
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

    void makeTrans() {
        Writer transWriter = new Writer();
        String indent = "        ";
        transWriter.print("\"");
        transWriter.print(makeOctal(dfa.getAlphabet().size()));
        transWriter.print("\" +");
        transWriter.print("\n");
        for (int state = 0; state <= dfa.lastState; state++) {
            List<Transition> list = dfa.trans[state];
            transWriter.print(indent);
            transWriter.print("\"");
            if (list == null || list.isEmpty()) {
                transWriter.print(makeOctal(0));
            }
            else {
                transWriter.print(makeOctal(list.size()));
                for (Transition transition : list) {
                    transWriter.print(makeOctal(transition.input));
                    transWriter.print(makeOctal(transition.target));
                }
            }
            transWriter.print("\"");
            if (state <= dfa.lastState - 1) {
                transWriter.print(" +\n");
            }
        }
        template.set("trans", transWriter.getString());
    }

    void writeTokenClass() throws IOException {
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
