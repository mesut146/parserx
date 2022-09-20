package mesut.parserx.gen;

public enum Lang {
    JAVA, CPP;

    public static Lang from(String lang) {
        if (lang.equals("java")) {
            return JAVA;
        }
        else if (lang.equals("cpp") || lang.equals("c++")) {
            return CPP;
        }
        throw new RuntimeException("invalid lang: " + lang);
    }
}
