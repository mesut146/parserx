package nodes;

public abstract class Transformer {

    public RuleDecl transformRule(RuleDecl decl) {
        return new RuleDecl(decl.name, transformNode(decl.rhs));
    }

    public Node transformNode(Node node) {
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
        else if (node.isBracket()) {
            return transformBracket(node.asBracket());
        }
        return node;
    }

    public Node transformGroup(GroupNode node) {
        return new GroupNode(transformNode(node.rhs));
    }

    public Node transformSequence(Sequence node) {
        Sequence newNode = new Sequence();
        for (Node ch : node) {
            newNode.add(transformNode(ch));
        }
        return newNode;
    }

    public Node transformRegex(RegexNode node) {
        return new RegexNode(transformNode(node.node), node.type);
    }

    public Node transformOr(OrNode node) {
        return node;
    }

    public Node transformBracket(Bracket node) {
        return node.normalize().optimize();
    }

}
