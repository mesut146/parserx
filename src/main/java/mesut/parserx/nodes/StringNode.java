package mesut.parserx.nodes;

import mesut.parserx.utils.UnicodeUtils;

import java.util.Objects;

//lexer rule without regex
//can be in lexer or parser part
public class StringNode extends Node {

    public String value;

    public StringNode() {
    }

    public StringNode(String value) {
        this.value = value;
    }

    public static StringNode from(String str) {
        return new StringNode(UnicodeUtils.fromEscaped(UnicodeUtils.trimQuotes(str)));
    }

    @Override
    public String toString() {
        return varString() + "\"" + printNormal() + "\"";
    }

    public String printNormal() {
        return UnicodeUtils.escapeString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringNode node = (StringNode) o;
        return Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
