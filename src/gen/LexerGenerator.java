package gen;

import dfa.DFA;
import dfa.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexerGenerator extends IndentWriter {
    DFA dfa;
    String dir;
    String className;
    String packageName;
    String tokenClassName = "Token";
    String functionName = "next";

    public LexerGenerator(DFA dfa, String dir) {
        this.dfa = dfa;
        this.dir = dir;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void generate() throws FileNotFoundException {

        File file = new File(dir, className + ".java");
        writer = new PrintWriter(file);

        if (packageName != null) {
            writer.println("package " + packageName + ";\n");
        }
        writeImports();

        writer.printf("public class %s{\n", className);
        indent();

        makeTables();
        writeFields();
        writeConstructor();
        writeRead();
        writeGetState();
        writeNextToken();

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

    void writeImports() {
        lineln("import java.io.*;\n");
    }

    void writeFields() {
        lineln("Reader reader;");
        lineln("int curState;");
        lineln("int lastState=-1;");
        lineln("static int INITIAL=0;");
        lineln("int yypos=0;");
        lineln("int yychar;");
        lineln("boolean backup=false;");
        lineln("StringBuilder yybuf=new StringBuilder();");
        lineln();
    }

    void writeConstructor() {
        linef("public %s(Reader reader){\n", className);
        indent();
        lineln("this.reader=reader;");
        unindent();
        lineln("}");

        linef("public %s(String file) throws FileNotFoundException{ \n", className);
        indent();
        lineln("this.reader=new FileReader(file);");
        unindent();
        lineln("}\n");
    }

    void writeRead() {
        lineln("int read() throws IOException {");
        indent();
        lineln("if(!backup){");
        indent();
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
        indent();
        lineln("int[] arr=inputMap[curState];");//inputs
        lineln("if(arr.length==0) return -1;");
        lineln("for(int i=0;i<arr.length;i++){");
        indent();
        lineln("int[] seg={arr[i]>>>16,arr[i]&((1 << 16) - 1)};");
        lineln("if(yychar>=seg[0]&&yychar<=seg[1]){");
        indent();
        lineln("return targetMap[curState][i];");
        unindent();
        lineln("}");//if
        unindent();
        lineln("}");//for
        lineln("return -1;");
        unindent();
        lineln("}\n");
    }

    void writeNextToken() {
        linef("public %s %s() throws IOException {\n", tokenClassName, functionName);
        indent();

        lineln("curState=INITIAL;");//yyinitial
        lineln("lastState=-1;");
        lineln("int startPos=yypos;");

        lineln("while(true){");
        indent();
        lineln("read();");
        lineln("int st=getState();");
        lineln("if(st==-1){");
        indent();
        lineln("if(lastState!=-1){");
        indent();
        lineln("Token token=null;");
        lineln("if(!skip[lastState]){");
        indent();
        lineln("token=new Token(ids[lastState],yybuf.toString());");
        lineln("token.offset=startPos;");
        lineln("lastState=-1;");
        unindent();
        lineln("}");
        lineln("curState=0;");
        lineln("backup=true;");
        lineln("yybuf.setLength(0);");
        lineln("if(token!=null) return token;");
        lineln("if(yychar==-1) break;");
        linef("if(skip[lastState]) return %s();\n", functionName);
        unindent();
        lineln("}");//if last state
        lineln("else{ throw new IOException(\"invalid input=\"+yychar+\" yybuf= \"+yybuf);}");
        unindent();
        lineln("}");//if st==-1
        lineln("else{");//transit to target state
        indent();
        lineln("yybuf.append((char) yychar);");
        lineln("backup=false;");
        lineln("curState=st;");
        lineln("if(accepting[curState]) lastState = curState;");//if final

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

        tokenWriter.writer = new PrintWriter(dir + "/" + tokenClassName + ".java");
        if (packageName != null)
            tokenWriter.linef("package %s;", packageName);
        tokenWriter.linef("public class %s{\n", tokenClassName);
        tokenWriter.indent();
        tokenWriter.lineln("public int type;");
        tokenWriter.lineln("public String value;");
        tokenWriter.lineln("public int offset;");
        tokenWriter.lineln();

        tokenWriter.linef("public %s(){}\n\n", tokenClassName);
        tokenWriter.linef("public %s(int type,String value){\n", tokenClassName);
        tokenWriter.indent();
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
