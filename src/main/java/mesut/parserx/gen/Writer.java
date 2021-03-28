package mesut.parserx.gen;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Writer extends PrintWriter {
    ByteArrayOutputStream baos;

    public Writer() {
        super(new ByteArrayOutputStream());
        baos = new ByteArrayOutputStream();
        out = new BufferedWriter(new OutputStreamWriter(baos));
    }

    public String getString() {
        flush();
        return baos.toString();
    }
}
