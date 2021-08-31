package mesut.parserx.gen;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class Writer {
    ByteArrayOutputStream baos;
    PrintWriter w;

    public Writer() {
        baos = new ByteArrayOutputStream();
        w = new PrintWriter(baos);
    }

    public String getString() {
        w.flush();
        return baos.toString();
    }

    public void print(String s) {
        w.print(s);
    }

    public void print(int i) {
        w.print(i);
    }
}
