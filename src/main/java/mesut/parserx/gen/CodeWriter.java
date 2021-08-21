package mesut.parserx.gen;

public class CodeWriter {
    StringBuilder sb = new StringBuilder();
    int level = 0;
    String indent;

    void init() {
        indent = "";
        for (int i = 0; i < level; i++) {
            indent += " ";
        }
    }

    public void write(String line) {
        sb.append(indent).append(line);
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

    public String get() {
        return sb.toString();
    }
}
