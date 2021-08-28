package mesut.parserx.gen;

public class CodeWriter {
    public boolean auto;
    StringBuilder sb = new StringBuilder();
    int level = 0;
    String indent;

    public CodeWriter(boolean auto) {
        this.auto = auto;
        init();
    }

    void init() {
        indent = "";
        for (int i = 0; i < level; i++) {
            indent += " ";
        }
    }

    public void append(String line) {
        if (line.isEmpty()) {
            sb.append("\n");
            return;
        }
        if (auto && line.endsWith("}")) {
            down();
        }
        sb.append(indent).append(line).append("\n");
        if (auto && line.endsWith("{")) {
            up();
        }
    }

    public void all(String s) {
        String[] arr = s.split("\n");
        for (int i = 0; i < arr.length; i++) {
            append(arr[i]);
        }
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
