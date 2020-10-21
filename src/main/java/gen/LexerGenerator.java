package gen;

import dfa.DFA;
import dfa.Transition;
import nodes.NodeList;
import nodes.RangeNode;
import utils.Helper;
import utils.UnicodeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexerGenerator extends IndentWriter {
    DFA dfa;
    String outDir;
    String className;
    String packageName;
    String tokenClassName = "Token";
    String functionName = "next";
    boolean external = true;
    boolean outDirAuto;

    public LexerGenerator(DFA dfa, String outDir) {
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
        writer = new PrintWriter(file);

        String template = Helper.read(getClass().getResourceAsStream("/lexer.java.template"));
        template = replace("package", packageName, template);
        template = replace("lexer_class", className, template);
        template = replace("next_token", functionName, template);
        template = makeTables(template);
        writer.write(template);
        writer.close();
        writeTokenClass();
        System.out.println("lexer file generated");
    }

    String replace(String name, String val, String template) {
        name = "$" + name + "$";
        return template.replace(name, val);
    }

    String makeTables(String template) {
        Map<String, Integer> idMap = new HashMap<>();//unique ids for tokens
        int[] idArr = new int[dfa.numStates + 1];
        int idIdx = 1;

        Writer transWriter = new Writer();
        transWriter.print("\"");
        int maxCharId = dfa.tree.alphabet.map.size();
        for (int state = dfa.initial; state <= dfa.numStates; state++) {
            //make id for token
            String name = dfa.names[state];
            if (name != null && dfa.isAccepting(state)) {
                if (!idMap.containsKey(name)) {//if previously not assigned
                    idMap.put(name, idIdx);
                    idIdx++;
                }
                idArr[state] = idMap.get(name);
            }
            List<Transition> list = dfa.trans[state];
            if (list != null) {
                transWriter.print(UnicodeUtils.escapeUnicode(list.size()));
                for (Transition transition : list) {
                    transWriter.print(UnicodeUtils.escapeUnicode(transition.input));
                    transWriter.print(UnicodeUtils.escapeUnicode(transition.target));
                }
            }
        }
        transWriter.print("\"");
        //write inputMap
        Writer cmapWriter = new Writer();
        cmapWriter.print("\"");

        for (Map.Entry<RangeNode, Integer> entry : dfa.tree.alphabet.map.entrySet()) {
            int left = entry.getKey().start;
            int right = entry.getKey().end;
            int id = entry.getValue();
            cmapWriter.print(UnicodeUtils.escapeUnicode(left));
            cmapWriter.print(UnicodeUtils.escapeUnicode(right));
            cmapWriter.print(UnicodeUtils.escapeUnicode(id));
        }
        cmapWriter.print("\"");
        template = replace("cMap", cmapWriter.getString(), template);


        template = replace("final_list", NodeList.join(makeIntArr(dfa.accepting), ","), template);
        template = replace("skip_list", NodeList.join(makeIntArr(dfa.isSkip), ","), template);

        Writer nameWriter = new Writer();
        Writer idWriter = new Writer();
        idIdx = 0;
        for (int state = dfa.initial; state <= dfa.numStates; state++) {
            if (idIdx > 0) {
                idWriter.print(",");
            }
            idWriter.print(idArr[state]);
            idIdx++;

            nameWriter.print("\"" + dfa.names[state] + "\"");
            if (state <= dfa.numStates - 1) {
                nameWriter.print(",");
            }
        }
        template = replace("name_list", nameWriter.getString(), template);
        template = replace("id_list", idWriter.getString(), template);

        return template;
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

    void writeTokenClass() throws FileNotFoundException {
        IndentWriter tokenWriter = new IndentWriter();

        tokenWriter.writer = new PrintWriter(outDir + "/" + tokenClassName + ".java");
        if (packageName != null)
            tokenWriter.linef("package %s;\n", packageName);
        tokenWriter.linef("public class %s{\n", tokenClassName);
        tokenWriter.calcIndent();
        tokenWriter.lineln("public int type;");
        tokenWriter.lineln("public String value;");
        tokenWriter.lineln("public int offset;");
        tokenWriter.lineln("public String name;//token name that's declared in grammar");
        tokenWriter.lineln();

        tokenWriter.linef("public %s(){}\n\n", tokenClassName);
        tokenWriter.linef("public %s(int type,String value){\n", tokenClassName);
        tokenWriter.calcIndent();
        tokenWriter.lineln("this.type=type;");
        tokenWriter.lineln("this.value=value;");
        tokenWriter.unindent();
        tokenWriter.lineln("}");
        tokenWriter.lineln("public String toString(){return value+\" type=\"+type;}");
        tokenWriter.unindent();
        tokenWriter.lineln("}");
        tokenWriter.flush();
    }
}
