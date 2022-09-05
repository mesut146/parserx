package mesut.parserx.gen;

public class Lang {
    public String lang;

    public Lang(String lang) {
        if (!lang.equals("java") && !lang.equals("c++") && !lang.equals("cpp")) {
            throw new RuntimeException("invalid lang: " + lang);
        }
        this.lang = lang;
    }
}
