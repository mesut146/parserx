package nodes;

import gen.Config;
import grammar.ParseException;
import utils.UnicodeUtils;

//lexer rule without regex
//can be in lexer or parser part
public class StringNode extends Node {

    public String value;
    public boolean isDot = false;//[^\n]

    public StringNode() {
    }

    public StringNode(String value) {
        this.value = value;
    }

    public static StringNode from(String str) throws ParseException {
        return new StringNode(UnicodeUtils.fromEscaped(UnicodeUtils.trimQuotes(str)));
    }

    //convert dot to bracket node
    public Bracket toBracket() {
        if (!isDot) {
            return null;
        }
        Bracket b = new Bracket();
        b.add('\n');
        b.negate = true;
        return b;
    }

    Bracket dot2() {
        Bracket b = new Bracket();
        b.add('\n');
        b.add('\r');
        b.negate = true;
        return b;
    }

    @Override
    public String toString() {
        if (isDot) {
            return ".";
        }
        if (Config.string_quote) {
            return "\"" + value + "\"";
        }
        return value;
    }


}
