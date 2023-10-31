package mesut.parserx.gen.lr;

public enum LrType {
    LR1, LALR1;

    public static LrType from(String type) {
        if (type.equals("lr1")) return LR1;
        if (type.equals("lalr1")) return LALR1;
        throw new RuntimeException("invalid LR type: " + type);
    }
}
