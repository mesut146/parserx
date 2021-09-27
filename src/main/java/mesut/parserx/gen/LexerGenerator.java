package mesut.parserx.gen;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Range;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LexerGenerator {
    public IdMap idMap = new IdMap();
    public NFA dfa;
    Options options;
    Template template;

    public LexerGenerator(NFA dfa, Options options) {
        this.dfa = dfa;
        this.options = options;
    }

    public LexerGenerator(Tree tree) {
        this.dfa = tree.makeNFA().dfa();
        this.options = tree.options;
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
        System.out.println("lexer file generated to " + file);

        writeTokenClass();
    }

    void makeTables() {
        makeTrans();
        nameAndId();
        template.set("final_list", NodeList.join(makeIntArr(dfa.accepting), ","));
        template.set("skip_list", NodeList.join(makeIntArr(dfa.isSkip), ","));

        cmap();
    }

    private void nameAndId() {
        //generate name and id list
        idMap.genSymbolIds(dfa.tree);

        int[] idArr = new int[dfa.lastState + 1];//state->id
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            //make id for token
            String name = dfa.names[state];
            if (name != null && dfa.isAccepting(state) && !dfa.isSkip[state]) {
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
                    idWriter.print("\n");
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
        for (Iterator<Range> it = dfa.getAlphabet().getRanges(); it.hasNext(); ) {
            Range range = it.next();
            int left = range.start;
            int right = range.end;
            int id = dfa.getAlphabet().getId(range);
            cmapWriter.print(UnicodeUtils.escapeUnicode(left));
            cmapWriter.print(UnicodeUtils.escapeUnicode(right));
            cmapWriter.print(UnicodeUtils.escapeUnicode(id));
            i++;
            if (i % column == 0) {
                cmapWriter.print("\"+\n");
                if (it.hasNext()) {
                    cmapWriter.print("\"");//next line start
                }
            }
        }
        cmapWriter.print("\"");

        template.set("cMap", cmapWriter.getString());
    }

    void makeTrans() {
        Writer transWriter = new Writer();
        String indent = "        ";
        transWriter.print("\n");
        int maxId = dfa.getAlphabet().size();
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
        template.set("max", String.valueOf(maxId));
    }

    Transition getTransition(int id, List<Transition> list) {
        for (Transition transition : list) {
            if (transition.input == id) {
                return transition;
            }
        }
        return null;
    }

    String makeOctal(int val) {
        if (val <= 255) {
            return "\\" + Integer.toOctalString(val);
        }
        return UnicodeUtils.escapeUnicode(val);
        //throw new RuntimeException("can't make octal from " + val);
    }

    int[] makeIntArr(boolean[] arr) {
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
        System.out.println("token class generated to " + out);
    }
}
