package mesut.parserx.regex;

import mesut.parserx.nodes.*;

//normalize & optimize regex
//merges sequences,ors,single length strings
public class RegexOptimizer extends Transformer {
    Node regex;

    public RegexOptimizer(Node regex) {
        super(null);
        this.regex = regex;
    }

    public Node optimize() {
        return transformNode(regex, null);
    }

    @Override
    public Node transformNode(Node node, Void parent) {
        node = node.normal();//merge
        return super.transformNode(node, parent);
    }

    //shrink or into bracket by combining single length strings
    @Override
    public Node visitOr(Or node, Void parent) {
        Or res = new Or();
        Bracket bracket = new Bracket();
        for (Node ch : node) {
            ch = transformNode(ch, parent);
            if (ch.isString() && ch.asString().value.length() == 1) {
                bracket.add(ch.asString().value.charAt(0));
            }
            else if (ch.isRange()) {
                bracket.add(ch);
            }
            else {
                res.add(ch);
            }
        }
        if (bracket.size() != 0) {
            bracket.normalize().optimize();
            if (bracket.size() == 1 && bracket.get(0).asRange().isSingle()) {
                res.add(new StringNode("" + (char) bracket.get(0).asRange().start));
            }
            else {
                res.add(bracket);
            }
        }
        return res.normal();
    }

    //shrink sequence by merging consecutive strings
    /*@Override
    public Node transformSequence(Sequence node, Node parent) {
        StringBuilder sb = new StringBuilder();
        Sequence res = new Sequence();
        for (int i = 0; i < node.size(); i++) {
            Node n = transformNode(node.get(i), node);
            if (n.isString()) {
                sb.append(n.asString().value);
            }
            else if (n.isRegex() && n.asRegex().isStar()) {
                //a a* = a+
                //a* a=a+
                Node ch = n.asRegex().node;
                if (i > 1 && node.get(i - 1).equals(ch)) {
                    res.list.remove(i - 1);
                    res.add(new Regex(ch, "+"));
                }
                else if (i < node.size() - 1 && node.get(i + 1).equals(ch)) {
                    res.add(new Regex(ch, "+"));
                    i++;//skip next
                }
            }
            else {
                if (sb.length() != 0) {
                    res.add(new StringNode(sb.toString()));
                    sb.setLength(0);
                }
                res.add(n);
            }
        }
        if (sb.length() != 0) {
            res.add(new StringNode(sb.toString()));
        }
        return res.normal();
    }*/

}
