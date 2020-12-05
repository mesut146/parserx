package regex;

import nodes.*;

public class RegexOptimizer extends Transformer {
    Node regex;

    public RegexOptimizer(Node regex) {
        this.regex = regex;
    }

    public Node optimize() {
        return transform(regex);
    }

    @Override
    public Node transform(Node node) {
        if (node.isSequence()) {
            node = transformSequence2(node.asSequence());
        }
        else if (node.isOr()) {
            node = transformOr2(node.asOr());
        }
        return super.transform(node);
    }

    @Override
    public Node transformOr(OrNode node) {
        OrNode newNode = new OrNode();
        Bracket bracket = new Bracket();
        for (Node ch : node) {
            ch = transform(ch);
            if (ch.isString() && ch.asString().value.length() == 1) {
                bracket.add(new Bracket.CharNode(ch.asString().value.charAt(0)));
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

    @Override
    public Node transformSequence(Sequence node) {
        StringBuilder sb = new StringBuilder();
        Sequence newNode = new Sequence();
        for (int i = 0; i < node.size(); i++) {
            Node n = transform(node.get(i));
            if (n.isString()) {
                sb.append(n.asString().value);
            }
            else {
                if (sb.length() != 0) {
                    newNode.add(new StringNode(sb.toString()));
                    sb.setLength(0);
                }
                newNode.add(n);
            }
        }
        if (sb.length() != 0) {
            newNode.add(new StringNode(sb.toString()));
        }
        return newNode.normal();
    }

    public Node transformSequence2(Sequence node) {
        Sequence newNode = new Sequence();
        for (int i = 0; i < node.size(); i++) {
            Node n = transform(node.get(i));
            if (n.isSequence()) {
                newNode.addAll(n.asSequence().list);
            }
            else {
                newNode.add(n);
            }
        }
        return newNode.normal();
    }

    public Node transformOr2(OrNode node) {
        OrNode newNode = new OrNode();
        for (int i = 0; i < node.size(); i++) {
            Node n = transform(node.get(i));
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
