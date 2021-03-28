package mesut.parserx.gen;

import mesut.parserx.grammar.GParser;
import mesut.parserx.grammar.ParseException;
import mesut.parserx.nodes.Node;

import java.io.StringReader;

public class LexUtils {

    static Node make(String regex) {
        try {
            return new GParser(new StringReader(regex)).regex();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node digit() {
        return make("[0-9]");
    }

    public static Node word() {
        return make("[A-Za-z0-9_]");
    }

    public static Node nonDigit() {
        return make("[^0-9]");
    }

    public static Node nonWord() {
        return make("[^A-Za-z0-9_]");
    }

    public static Node nonWs() {
        return make("[^ \\t\\r\\n\\f]");
    }

    public static Node ws() {
        return make("[ \\t\\r\\n\\f]");
    }
}
