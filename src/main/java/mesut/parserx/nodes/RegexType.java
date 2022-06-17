package mesut.parserx.nodes;

public enum RegexType {
    STAR, PLUS, OPTIONAL;

    public static RegexType from(String str) {
        if (str.equals("*")) return STAR;
        if (str.equals("+")) return PLUS;
        if (str.equals("?")) return OPTIONAL;
        throw new RuntimeException("invalid regex type: " + str);
    }

    @Override
    public String toString() {
        if (this == STAR) return "*";
        if (this == PLUS) return "+";
        return "?";
    }
}
