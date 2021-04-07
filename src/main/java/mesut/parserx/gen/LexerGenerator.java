package mesut.parserx.gen;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.RangeNode;
import mesut.parserx.utils.IOUtils;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LexerGenerator {
    NFA dfa;
    public String outDir;
    public String className;
    String packageName;
    String tokenClassName = "Token";
    String functionName = "next";
    boolean outDirAuto;
    Map<String, Integer> idMap = new HashMap<>();//name -> id

    public LexerGenerator(NFA dfa, String outDir) {
        this.dfa = dfa;
        this.outDir = outDir;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setOutDirFromPackage(boolean value) {
        this.outDirAuto = value;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void generate() throws IOException {
        File file;
        if (outDirAuto) {
            file = new File(outDir, packageName.replace('.', '/') + "/" + className + ".java");
        }
        else {
            file = new File(outDir, className + ".java");
        }

        Template template = new Template("lexer.java.template");
        template.set("package", packageName);
        template.set("lexer_class", className);
        template.set("token_class", tokenClassName);
        template.set("next_token", functionName);
        makeTables(template);
        IOUtils.write(template.toString(), file);
        System.out.println("lexer file generated to " + file);

        writeTokenClass();
    }


    void makeTables(Template template) {
        makeTrans(template);
        nameAndId(template);
        template.set("final_list", NodeList.join(makeIntArr(dfa.accepting), ","));
        template.set("skip_list", NodeList.join(makeIntArr(dfa.isSkip), ","));

        cmap(template);
    }

    private void nameAndId(Template template) {
        //generate name and id list
        int[] idArr = new int[dfa.lastState + 1];
        int idIdx = 1;

        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            //make id for token
            String name = dfa.names[state];
            if (name != null && dfa.isAccepting(state)) {
                if (!idMap.containsKey(name)) {//if previously not assigned
                    idMap.put(name, idIdx);
                    idIdx++;
                }
                idArr[state] = idMap.get(name);
            }
        }
        //write name and id list
        Writer nameWriter = new Writer();
        Writer idWriter = new Writer();
        idIdx = 0;
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            if (idIdx > 0) {
                idWriter.print(",");
            }
            idWriter.print(idArr[state]);
            idIdx++;

            nameWriter.print("\"" + (dfa.names[state] == null ? "" : dfa.names[state]) + "\"");
            if (state <= dfa.lastState - 1) {
                nameWriter.print(",");
            }
        }
        template.set("name_list", nameWriter.getString());
        template.set("id_list", idWriter.getString());
    }

    private void cmap(Template template) {
        Writer cmapWriter = new Writer();
        cmapWriter.print("\"");
        for (Iterator<RangeNode> it = dfa.getAlphabet().getRanges(); it.hasNext(); ) {
            RangeNode rangeNode = it.next();
            int left = rangeNode.start;
            int right = rangeNode.end;
            int id = dfa.getAlphabet().getId(rangeNode);
            cmapWriter.print(UnicodeUtils.escapeUnicode(left));
            cmapWriter.print(UnicodeUtils.escapeUnicode(right));
            cmapWriter.print(UnicodeUtils.escapeUnicode(id));

        }
        cmapWriter.print("\"");

        template.set("cMap", cmapWriter.getString());
    }

    void makeTrans(Template template) {
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
        File out = new File(outDir, tokenClassName + ".java");
        Template template = new Template("token.java.template");

        template.set("package", packageName);
        template.set("token_class", tokenClassName);

        IOUtils.write(template.toString(), out);

        System.out.println("token class generated to " + out);
    }
}
