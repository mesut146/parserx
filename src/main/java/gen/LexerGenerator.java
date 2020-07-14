package gen;

import dfa.CharClass;
import dfa.DFA;
import dfa.Transition;
import utils.UnicodeUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

    public void generate() throws FileNotFoundException {
        File file;
        if (outDirAuto) {
            file = new File(outDir, packageName.replace('.', '/') + "/" + className + ".java");
        }
        else {
            file = new File(outDir, className + ".java");
        }

        writer = new PrintWriter(file);

        if (packageName != null) {
            writer.println("package " + packageName + ";\n");
        }
        writeImports();

        writer.printf("public class %s{\n", className);
        calcIndent();

        makeTables2();
        writeFields();
        writeConstructor();
        writeRead();
        writeGetState2();
        writeGetBit();
        writeNextToken();
        writeUnpack();
        writer.println("}");//end class
        unindent();
        writer.close();
        writeTokenClass();
        System.out.println("lexer file generated");
    }

    void makeTables() {
        int[][] inputMap = new int[dfa.numStates + 1][];//[state]={input set}
        int[][] targetMap = new int[dfa.numStates + 1][];//[state][offset] = target state

        Map<String, Integer> idMap = new HashMap<>();//incremental unique ids for tokens
        int[] idArr = new int[dfa.numStates + 1];
        int idIdx = 1;

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
                int inputIdx = 0;
                inputMap[state] = new int[list.size()];
                targetMap[state] = new int[list.size()];
                for (Transition tr : list) {
                    inputMap[state][inputIdx] = tr.input;
                    targetMap[state][inputIdx] = tr.target;
                    inputIdx++;
                }
            }
        }
        //write inputMap
        line("static int[][] inputMap = {");
        for (int i = 0; i < inputMap.length; i++) {
            int[] arr = inputMap[i];
            print("{");
            if (arr != null)
                for (int j = 0; j < arr.length; j++) {
                    print(arr[j]);
                    if (j < arr.length - 1) {
                        print(",");
                    }
                }
            print("}");
            if (i < inputMap.length - 1) {
                print(",");
            }
        }
        println("};");

        line("static int[][] targetMap = {");
        for (int i = 0; i < targetMap.length; i++) {
            int[] arr = targetMap[i];
            print("{");
            if (arr != null)
                for (int j = 0; j < arr.length; j++) {
                    print(arr[j]);
                    if (j < arr.length - 1) {
                        print(",");
                    }
                }
            print("}");
            if (i < targetMap.length - 1) {
                print(",");
            }
        }
        println("};");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter acc = new PrintWriter(baos);

        ByteArrayOutputStream names_baos = new ByteArrayOutputStream();
        PrintWriter names_pw = new PrintWriter(names_baos);

        ByteArrayOutputStream id_baos = new ByteArrayOutputStream();
        PrintWriter id_pw = new PrintWriter(id_baos);

        line("boolean[] skip={");
        acc.print("boolean[] accepting={");
        names_pw.print("String[] names={");
        id_pw.print("int[] ids={");
        idIdx = 0;
        for (int state = dfa.initial; state <= dfa.numStates; state++) {
            if (idIdx > 0) {
                id_pw.print(",");
            }
            id_pw.print(idArr[state]);
            idIdx++;

            print(dfa.isSkip[state]);
            acc.print(dfa.isAccepting(state));
            names_pw.print("\"" + dfa.names[state] + "\"");
            if (state <= dfa.numStates - 1) {
                print(",");
                acc.print(",");
                names_pw.print(",");
            }
        }
        println("};");
        acc.println("};");
        names_pw.println("};");
        id_pw.println("};");
        acc.flush();
        names_pw.flush();
        id_pw.flush();
        line(baos.toString());
        line(names_baos.toString());
        line(id_baos.toString());
    }

    void makeTables2() {
        int[][] inputMap = new int[dfa.numStates + 1][];//[state]={input set}
        int[][] targetMap = new int[dfa.numStates + 1][];//[state][offset] = target state

        Map<String, Integer> idMap = new HashMap<>();//incremental unique ids for tokens
        int[] idArr = new int[dfa.numStates + 1];
        int idIdx = 1;

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
                int inputIdx = 0;
                inputMap[state] = new int[list.size()];
                targetMap[state] = new int[list.size()];
                for (Transition tr : list) {
                    inputMap[state][inputIdx] = tr.input;
                    targetMap[state][inputIdx] = tr.target;
                    inputIdx++;
                }
            }
        }
        //write inputMap
        lineln("static int[][] inputMap = unpack(");
        String indent = "            ";
        for (int i = 0; i < inputMap.length; i++) {
            int[] arr = inputMap[i];
            int len = arr == null ? 0 : arr.length;
            print(indent);
            print("\"");
            print(UnicodeUtils.escapeUnicode(len));
            for (int j = 0; j < len; j++) {
                int[] seg = CharClass.desegment(arr[j]);
                print(UnicodeUtils.escapeUnicode(seg[0]));
                print(UnicodeUtils.escapeUnicode(seg[1]));
                print(UnicodeUtils.escapeUnicode(targetMap[i][j]));
            }
            print("\"");
            if (i < inputMap.length - 1) {
                print("+\n");
            }
        }

        println(");");

        /*line("static int[][] targetMap = {");
        for (int i = 0; i < targetMap.length; i++) {
            int[] arr = targetMap[i];
            print("{");
            if (arr != null)
                for (int j = 0; j < arr.length; j++) {
                    print(arr[j]);
                    if (j < arr.length - 1) {
                        print(",");
                    }
                }
            print("}");
            if (i < targetMap.length - 1) {
                print(",");
            }
        }
        println("};");*/

        ByteArrayOutputStream names_baos = new ByteArrayOutputStream();
        PrintWriter names_pw = new PrintWriter(names_baos);

        ByteArrayOutputStream ids_baos = new ByteArrayOutputStream();
        PrintWriter ids_pw = new PrintWriter(ids_baos);

        line("int[] skip=");
        printArr(makeIntArr(dfa.isSkip));

        line("int[] accepting=");
        printArr(makeIntArr(dfa.accepting));

        names_pw.print("String[] names={");
        ids_pw.print("int[] ids={");
        idIdx = 0;
        for (int state = dfa.initial; state <= dfa.numStates; state++) {
            if (idIdx > 0) {
                ids_pw.print(",");
            }
            ids_pw.print(idArr[state]);
            idIdx++;

            names_pw.print("\"" + dfa.names[state] + "\"");
            if (state <= dfa.numStates - 1) {
                names_pw.print(",");
            }
        }


        names_pw.println("};");
        ids_pw.println("};");
        names_pw.flush();
        ids_pw.flush();

        line(names_baos.toString());
        line(ids_baos.toString());
    }

    private void printArr(int[] arr) {
        print("{");
        for (int i = 0; i < arr.length; i++) {
            print(arr[i]);
            if (i < arr.length - 1) {
                print(",");
            }
        }
        println("};");
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

    void writeUnpack() {
        linef("static int[][] unpack(String str){\n");
        calcIndent();
        lineln("int pos = 0;");
        lineln("List<int[]> list = new ArrayList<>();");
        lineln("while (pos < str.length()) {");
        calcIndent();
        lineln("char groupLen = str.charAt(pos++);");
        lineln("int[] arr = new int[groupLen * 3];//left,right,target");
        lineln("int arrPos = 0;");
        lineln("for (int i = 0; i < groupLen; i++) {");
        calcIndent();
        lineln("arr[arrPos++] = str.charAt(pos++);");//left
        lineln("arr[arrPos++] = str.charAt(pos++);");//right
        lineln("arr[arrPos++] = str.charAt(pos++);");//target
        unindent();
        lineln("}");
        lineln("list.add(arr);");
        unindent();
        lineln("}");
        lineln("return list.toArray(new int[0][]);");

        unindent();

        lineln("}");

    }

    void writeImports() {
        lineln("import java.io.*;\n");
        lineln("import java.util.*;\n");
    }

    void writeFields() {
        lineln("Reader reader;");
        lineln("int curState;");
        lineln("int lastState=-1;");
        lineln("static int INITIAL=0;");
        lineln("static int EOF=-1;");
        lineln("int yypos=0;");
        lineln("int yychar;");
        lineln("boolean backup=false;");
        lineln("StringBuilder yybuf=new StringBuilder();");
        lineln();
    }

    void writeConstructor() {
        linef("public %s(Reader reader){\n", className);
        calcIndent();
        lineln("this.reader=reader;");
        unindent();
        lineln("}");

        linef("public %s(String file) throws FileNotFoundException{ \n", className);
        calcIndent();
        lineln("this.reader=new FileReader(file);");
        unindent();
        lineln("}\n");
    }

    void writeRead() {
        lineln("int read() throws IOException {");
        calcIndent();
        lineln("if(!backup){");
        calcIndent();
        lineln("yychar=reader.read();");
        lineln("yypos++;");
        unindent();
        lineln("}");
        lineln("return yychar;");
        unindent();
        lineln("}\n");
    }

    void writeGetState() {
        lineln("int getState(){");
        calcIndent();
        lineln("int[] arr=inputMap[curState];");//inputs
        lineln("if(arr.length==0) return -1;");
        lineln("for(int i=0;i<arr.length;i++){");
        calcIndent();
        lineln("int[] seg={arr[i]>>>16,arr[i]&((1 << 16) - 1)};");
        lineln("if(yychar>=seg[0]&&yychar<=seg[1]){");
        calcIndent();
        lineln("return targetMap[curState][i];");
        unindent();
        lineln("}");//if
        unindent();
        lineln("}");//for
        lineln("return -1;");
        unindent();
        lineln("}\n");
    }

    void writeGetState2() {
        lineln("int getState(){");
        calcIndent();
        lineln("int[] arr=inputMap[curState];");//inputs
        lineln("for(int i=0;i<arr.length;i+=3){");
        calcIndent();
        lineln("if(yychar>=arr[i]&&yychar<=arr[i+1]){");
        calcIndent();
        lineln("return arr[i+2];");
        unindent();
        lineln("}");//if
        unindent();
        lineln("}");//for
        lineln("return -1;");
        unindent();
        lineln("}\n");
    }

    void writeGetBit() {
        lineln("static boolean getBit(int[] arr, int state) {");
        calcIndent();
        lineln("return ((arr[state/32]>>(state%32))&1)!=0;");
        unindent();
        lineln("}\n");
    }

    void writeNextToken() {
        linef("public %s %s() throws IOException {\n", tokenClassName, functionName);
        calcIndent();

        lineln("curState=INITIAL;");//yyinitial
        lineln("lastState=-1;");
        lineln("int startPos=yypos;");

        lineln("read();");
        lineln("if(yychar==EOF) return null;");
        lineln("backup=true;");

        lineln("while(true){");
        calcIndent();
        lineln("read();");
        lineln("int st=getState();");
        lineln("if(st==-1){");
        calcIndent();
        lineln("if(lastState!=-1){");
        calcIndent();
        lineln("Token token=null;");
        lineln("if(!getBit(skip,lastState)){");
        calcIndent();
        lineln("token=new Token(ids[lastState],yybuf.toString());");
        lineln("token.offset=startPos;");
        lineln("token.name=names[lastState];");
        lineln("lastState=-1;");
        unindent();
        lineln("}");
        lineln("curState=0;");
        lineln("backup=true;");
        //lineln("yypos--;//we read extra input, push back");
        lineln("yybuf.setLength(0);");
        lineln("if(token!=null) return token;");
        lineln("if(yychar==-1) break;");
        linef("if(getBit(skip,lastState)) return %s();\n", functionName);
        unindent();
        lineln("}");//if last state
        lineln("else{ throw new IOException(\"invalid input=\"+yychar+\" yybuf= \"+yybuf);}");
        unindent();
        lineln("}");//if st==-1
        lineln("else{");//transit to target state
        calcIndent();
        lineln("yybuf.append((char) yychar);");
        lineln("backup=false;");
        lineln("curState=st;");
        lineln("if(getBit(accepting,curState)) lastState = curState;");//if final

        unindent();
        lineln("}");//else
        unindent();
        lineln("}");//while

        lineln("return null;");
        unindent();
        lineln("}\n");
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
