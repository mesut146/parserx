package mesut.parserx.gen;

public class CodeWriter {
    static int count = 4;
    public boolean auto;
    public StringBuilder sb = new StringBuilder();
    int level = 0;
    String indent;

    public CodeWriter(boolean auto) {
        this.auto = auto;
        init();
    }

    void init() {
        indent = " ".repeat(Math.max(0, count)).repeat(Math.max(0, level));
    }


    public void print(String line, Object... args) {
        if (line.isEmpty()) {
            sb.append("\n");
            return;
        }
        if (auto && line.endsWith("}")) {
            down();
        }
        if (args.length != 0) {
            line = String.format(line, args);
        }
        sb.append(indent).append(line);
        if (auto && line.endsWith("{")) {
            up();
        }
    }

    public void append(String line, Object... args) {
        if (line.isEmpty()) {
            sb.append("\n");
            return;
        }
        if (auto && line.endsWith("}")) {
            down();
        }
        if (args.length != 0) {
            line = String.format(line, args);
        }
        sb.append(indent).append(line).append("\n");
        if (auto && line.endsWith("{")) {
            up();
        }
    }

    public void all(String s, Object... args) {
        if (args.length != 0) {
            s = String.format(s, args);
        }
        var arr = s.split("\n");
        for (var value : arr) {
            append(value);
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

    @Override
    public String toString() {
        return sb.toString();
    }
}
