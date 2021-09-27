package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

public class Simplify extends SimpleTransformer {
    public Simplify(Tree tree) {
        super(tree);
    }

    public static Node simplifyNode(Node node) {
        return new Simplify(null).transformNode(node, null);
    }

    boolean isSimple(Node node) {
        return node.isName() || node.isGroup() || node.isRegex() || node.isString();
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        Node ch = transformNode(node.node, node);
        if (isSimple(ch)) {
            return ch;
        }
        return ch;
    }

    @Override
    public Node transformSequence(Sequence node, Node parent) {
        Sequence res = new Sequence();
        for (Node ch : node) {
            ch = transformNode(ch, node);
            if (ch.isSequence()) {
                res.addAll(ch.asSequence().list);
            }
            else {
                res.add(ch);
            }
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res;
    }

    @Override
    public Node transformOr(Or node, Node parent) {
        Or res = new Or();
        for (Node ch : node) {
            ch = transformNode(ch, node);
            if (ch.isOr()) {
                res.addAll(ch.asOr().list);
            }
            else {
                res.add(ch);
            }
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res.dups();
    }

    @Override
    public Node transformRegex(Regex regex, Node parent) {
        if (!regex.node.isGroup() && !regex.node.isName()) {
            return transformRegex(new Regex(new Group(regex.node), regex.type), parent);
        }
        Node ch = transformNode(regex.node, regex);
        if (regex.isOptional()) {
            if (ch.isPlus()) {
                return new Regex(ch.asRegex().node, "*");
            }
            else if (ch.isStar() || ch.isOptional()) {
                return ch;
            }
        }
        else if (regex.isStar()) {
            if (ch.isStar() || ch.isPlus()) {
                return ch;
            }
            if (ch.isOptional()) {
                return new Regex(ch.asRegex().node, "*");
            }
        }
        else {
            if (ch.isOptional() || ch.isStar()) {
                return new Regex(ch.asRegex().node, "*");
            }
            if (ch.isPlus()) {
                return ch;
            }
        }
        return new Regex(ch, regex.type);
    }
}
