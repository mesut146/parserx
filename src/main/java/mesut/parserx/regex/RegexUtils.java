package mesut.parserx.regex;

import mesut.parserx.nodes.*;

public class RegexUtils {

    public static String print(Node node) {
        if (node.isString()) {
            return node.asString().toString();
        }
        return node.toString();
    }

    public static Node split(Node node) {
        if (node.isSequence()) {
            Sequence sequence = node.asSequence();
            return new Sequence(sequence.list.subList(1, sequence.size())).normal();
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            return new Or(or.list.subList(1, or.size())).normal();
        }
        return node;
    }

    public static Node blockComment() {
        return Sequence.of(new StringNode("/*"), new Regex(new Or(new Bracket("[^*]"), new Sequence(new StringNode("*"), new Bracket("[^/]"))), "*"), new StringNode("*/"));
    }

    //de morgan laws
    public static Node negate(Node regex) throws Exception {
        if (regex.isOr()) {
            //(a|b)' = a' . b'
            Or or = regex.asOr();
            Node left = or.first();
            Node right = split(or);
            return new Sequence(negate(left), negate(right));
        }
        else if (regex.isSequence()) {
            //(ab)' = a' + b'
            Sequence seq = regex.asSequence();
            Node left = seq.get(0);
            Node right = split(seq);
            return new Or(negate(left), negate(right));
        }
        else if (regex.isString()) {
            StringNode str = regex.asString();
            if (str.value.length() == 1) {
                Bracket bracket = new Bracket();
                bracket.negate = true;
                bracket.add(Range.of(str.value.charAt(0)));
                return bracket;
            }
            Sequence seq = new Sequence();
            for (char c : str.value.toCharArray()) {
                seq.add(new StringNode("" + c));
            }
            return negate(seq);
        }
        else if (regex.isGroup()) {
            return new Group(negate(regex.asGroup().node));
        }
        else if (regex.isBracket()) {
            Bracket bracket = regex.asBracket();
            Bracket newNode = new Bracket(bracket.list);
            newNode.negate = !bracket.negate;
            return newNode;
        }
        throw new Exception("can't negate regex: " + regex);
    }


}
