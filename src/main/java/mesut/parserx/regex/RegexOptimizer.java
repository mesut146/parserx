package mesut.parserx.regex;

import mesut.parserx.nodes.*;

//normalize & optimize regex
//merges sequences,ors,single length strings
public class RegexOptimizer extends Transformer {
    Node regex;

    public RegexOptimizer(Node regex) {
        this.regex = regex;
    }

    public Node optimize() {
        return transformNode(regex);
    }

    @Override
    public Node transformNode(Node node) {
        if (node.isSequence()) {
            node = transformSequence2(node.asSequence());
        }
        else if (node.isOr()) {
            node = transformOr2(node.asOr());
        }
        return super.transformNode(node);
    }

    //shrink or into bracket by combining single length strings
    @Override
    public Node transformOr(OrNode node) {
        OrNode newNode = new OrNode();
        Bracket bracket = new Bracket();
        for (Node ch : node) {
            ch = transformNode(ch);
            if (ch.isString() && ch.asString().value.length() == 1) {
                bracket.add(ch.asString().value.charAt(0));
            }
            else if (ch.isRange()) {
                bracket.add(ch);
            }
            else {
                newNode.add(ch);
            }
        }
        if (bracket.size() != 0) {
            bracket.normalize().optimize();
            newNode.add(bracket);
        }
        return newNode.normal();
    }

    //shrink sequence by merging consecutive strings
    @Override
    public Node transformSequence(Sequence node) {
        StringBuilder sb = new StringBuilder();
        Sequence res = new Sequence();
        for (int i = 0; i < node.size(); i++) {
            Node n = transformNode(node.get(i));
            if (n.isString()) {
                sb.append(n.asString().value);
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
    }

    //merge sequences
    public Node transformSequence2(Sequence node) {
        Sequence newNode = new Sequence();
        for (int i = 0; i < node.size(); i++) {
            Node n = transformNode(node.get(i));
            if (n.isSequence()) {
                newNode.addAll(n.asSequence().list);
            }
            else {
                newNode.add(n);
            }
        }
        return newNode.normal();
    }

    //merge ors
    public Node transformOr2(OrNode node) {
        OrNode newNode = new OrNode();
        for (int i = 0; i < node.size(); i++) {
            Node n = transformNode(node.get(i));
            if (n.isOr()) {
                newNode.addAll(n.asOr().list);
            }
            else {
                newNode.add(n);
            }
        }
        return newNode.normal();
    }

}
