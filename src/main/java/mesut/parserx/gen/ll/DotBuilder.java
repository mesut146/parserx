package mesut.parserx.gen.ll;

import java.io.PrintWriter;

public class DotBuilder {

    static int lastToken = 0;

    //make dot graph from Ast output
    public static void write(String str, PrintWriter w) {
        lastToken = 0;
        w.println("digraph G{");
        w.println("rankdir = TB;");

        int pos = 0;
        int end = str.length() - 1;

        //parse node name
        int begin = pos;
        while (Character.isLetterOrDigit(str.charAt(pos))) {
            pos++;
        }
        String name = str.substring(begin, pos);
        pos++;//{
        end--;//}
        w.printf("%d [label=%s];\n", ++lastToken, name);
        bind("" + lastToken, str.substring(pos, end + 1), w);

        w.println("}");
        w.close();
    }

    static void bind(String parent, String str, PrintWriter w) {
        if (str.isEmpty()) return;
        int pos = 0;
        if (str.charAt(pos) == '[') {
            pos++;//[
            int open = 1;
            int mark = pos;
            while (mark < str.length()) {
                if (str.charAt(mark) == '[') {
                    open++;
                }
                else if (str.charAt(mark) == ']') {
                    open--;
                    if (open == 0) {
                        break;
                    }
                }
                mark++;
            }
            //todo put something that says it's a loop
            bind(parent, str.substring(pos, mark + 1), w);
            pos = mark + 1;
        }
        else if (str.charAt(pos) == '\'') {
            //token
            pos++;
            int end = str.indexOf("'", pos + 1);
            String val = str.substring(pos, end);
            w.printf("%s -> %d;\n", parent, ++lastToken);
            w.printf("%d [label=\"%s\"];\n", lastToken, val);
            pos = end + 1;
        }
        else {
            //rule
            int begin = pos;
            while (pos < str.length() && Character.isLetterOrDigit(str.charAt(pos))) {
                pos++;
            }
            String name = str.substring(begin, pos);
            w.printf("%s -> %d;\n", parent, ++lastToken);
            w.printf("%d [label=\"%s\"];\n", lastToken, name);
            pos++;//{
            //find closing brace
            int open = 1;
            int mark = pos;
            while (mark < str.length()) {
                if (str.charAt(mark) == '{') {
                    open++;
                }
                else if (str.charAt(mark) == '}') {
                    open--;
                    if (open == 0) {
                        break;
                    }
                }
                mark++;
            }
            bind("" + lastToken, str.substring(pos, mark), w);
            pos = mark + 1;
        }
        if (pos < str.length()) {
            if (str.charAt(pos) == ',') {
                pos++;//comma
            }
            else {
                throw new RuntimeException("expecting comma got: " + str.charAt(pos));
            }
            if (str.charAt(pos) == '}' || str.charAt(pos) == ']') {
                //empty node after comma
                return;
            }
            else {
                bind(parent, str.substring(pos), w);
            }
        }
    }

}
