package gen;

import java.io.PrintWriter;

public class IndentWriter {
    int indentLevel = 0;
    String indentStr = "";
    PrintWriter writer;

    public void flush() {
        writer.flush();
    }


    void lineln(String str) {
        writer.print(indentStr);
        writer.println(str);
    }

    void println() {
        writer.print(indentStr);
        writer.println();
    }

    void lineln() {
        writer.println();
    }

    void printf(String str, Object... args) {
        writer.printf(str, args);
    }

    void linef(String str, Object... args) {
        writer.print(indentStr);
        writer.printf(str, args);
    }

    void calcIndent() {
        indentLevel++;
        indentStr = "";
        for (int i = 0; i < indentLevel; i++) {
            indentStr += "    ";
        }
    }

    void unindent() {
        indentLevel -= 2;
        calcIndent();
    }
}
