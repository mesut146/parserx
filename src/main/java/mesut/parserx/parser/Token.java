package mesut.parserx.parser;

public class Token {
    public int type;
    public String value;
    public int offset;
    public int line;
    public String name;//token name that's declared in grammar

    public Token() {
    }

    public Token(int type) {
        this.type = type;
    }

    public Token(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        if (type == 0) return "EOF";
        return String.format("%s{value = '%s', line = %s}", name, formatValue(), line);
    }

    public String formatValue() {
        return value.replace("\n", "\\n").replace("\r", "\\r").replace("\'", "\\\'");
    }
}
