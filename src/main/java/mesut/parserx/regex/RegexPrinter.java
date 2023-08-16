package mesut.parserx.regex;

import mesut.parserx.nodes.*;

public class RegexPrinter extends BaseVisitor<String, Void> {


    public static String print(Node node) {
        return node.accept(new RegexPrinter(), null);
    }

    @Override
    public String visitString(StringNode node, Void arg) {
        return node.value
                //.replace("[","\\[")
                .replace("*","\\*")
                .replace("+","\\+")
                .replace("?","\\?")
                .replace("(","\\(");
    }

    @Override
    public String visitRegex(Regex regex, Void arg) {
        return regex.node.accept(this, arg) + regex.type.toString();
    }

    @Override
    public String visitGroup(Group node, Void arg) {
        return "(" + node.node.accept(this, arg) + ")";
    }

    @Override
    public String visitSequence(Sequence seq, Void arg) {
        StringBuilder sb = new StringBuilder();
        for (Node ch : seq) {
            sb.append(ch.accept(this, arg));
        }
        return sb.toString();
    }

    @Override
    public String visitOr(Or or, Void arg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < or.size(); i++) {
            sb.append(or.get(i).accept(this, arg));
            if (i < or.size() - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    @Override
    public String visitBracket(Bracket node, Void arg) {
        return node.toString();
    }

    @Override
    public String visitDot(Dot node, Void arg) {
        return ".";
    }
}
