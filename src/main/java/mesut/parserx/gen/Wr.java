package mesut.parserx.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Wr {
    Writer writer;
    int level = 0;
    String indent;
    static int space_count = 4;

    public Wr(File file) throws IOException {
        this.writer = new FileWriter(file);
    }

    void init() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            for (int j = 0; j < space_count; j++) {
                sb.append(" ");
            }
        }
        indent = sb.toString();
    }

    public void close() throws IOException {
        this.writer.flush();
        this.writer.close();
    }

    public void up() {
        level++;
        init();
    }

    public void down() {
        if (level == 0) {
            throw new RuntimeException("negative indention");
        }
        level--;
        init();
    }

    public void append(String line, Object... args) throws IOException {
        if (args.length != 0) {
            line = String.format(line, args);
        }
        writer.append(indent).append(line);
    }
}
