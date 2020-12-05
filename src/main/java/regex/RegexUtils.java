package regex;

import nodes.*;

public class RegexUtils {

    public static Node negate(Node regex) throws Exception {
        if (regex.isOr()) {
            //(a|b)' = a' . b'
            OrNode or = regex.asOr();
            Node left = or.get(0);
            Node right = new Sequence(or.list.subList(1, or.size())).normal();
            return new Sequence(negate(left), negate(right));
        }
        else if (regex.isSequence()) {
            //(ab)' = a' + b'
            Sequence seq = regex.asSequence();
            Node left = seq.get(0);
            Node right = new Sequence(seq.list.subList(1, seq.size())).normal();
            return new OrNode(negate(left), negate(right));
        }
        else if (regex.isString()) {
            StringNode str = regex.asString();
            if (str.value.length() == 1) {
                Bracket bracket = new Bracket();
                bracket.negate = true;
                bracket.add(RangeNode.of(str.value.charAt(0)));
                return bracket;
            }
            Sequence seq = new Sequence();
            for (char c : str.value.toCharArray()) {
                seq.add(new StringNode("" + c));
            }
            return negate(seq);
        }
        else if (regex.isGroup()) {
            return new GroupNode(negate(regex.asGroup().rhs));
        }
        else if (regex.isBracket()) {
            Bracket bracket = regex.asBracket();
            Bracket newNode = new Bracket(bracket.list);
            newNode.negate = !bracket.negate;
            return newNode;
        }
        throw new Exception("invalid regex:" + regex);
    }

}
