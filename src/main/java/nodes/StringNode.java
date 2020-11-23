package nodes;

import grammar.ParseException;
import utils.UnicodeUtils;

import java.util.Objects;

//lexer rule without regex
//can be in lexer or parser part
public class StringNode extends Node {

    public String value;
    public boolean isDot = false;//[^\n]
    public static boolean string_quote = true;
    public Bracket bracket;

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
        if (bracket != null) {
            return bracket;
        }
        Bracket b = new Bracket();
        b.add('\n');
        b.negate = true;
        bracket = b;
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
        if (string_quote) {
            return "\"" + UnicodeUtils.escapeString(value) + "\"";
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringNode node = (StringNode) o;
        return isDot == node.isDot &&
                Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isDot);
    }
}
