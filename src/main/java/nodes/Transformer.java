package nodes;

public abstract class Transformer {

    public static Node transform(Node node, Transformer transformer) {
        return transformer.transform(node);
    }

    public Node transform(Node node) {
        if (node.isGroup()) {
            return transformGroup(node.asGroup());
        }
        else if (node.isSequence()) {
            return transformSequence(node.asSequence());
        }
        else if (node.isRegex()) {
            return transformRegex(node.asRegex());
        }
        else if (node.isOr()) {
            return transformOr(node.asOr());
        }
        return node;
    }

    public Node transformGroup(GroupNode node) {
        return new GroupNode(transform(node.rhs));
    }

    public Node transformSequence(Sequence node) {
        Sequence newNode = new Sequence();
        for (Node ch : node) {
            newNode.add(transform(ch));
        }
        return newNode;
    }

    public Node transformRegex(RegexNode node) {
        return new RegexNode(transform(node.node), node.type);
    }

    public Node transformOr(OrNode node) {
        return node;
    }
}
