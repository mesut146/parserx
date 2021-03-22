package nodes;

public class SimpleTransformer {

    public RuleDecl transformRule(RuleDecl decl) {
        decl.rhs = transformNode(decl.rhs, decl);
        return decl;
    }

    public Node transformNode(Node node, Node parent) {
        if (node.isGroup()) {
            return transformGroup(node.asGroup(), parent);
        }
        else if (node.isSequence()) {
            return transformSequence(node.asSequence(), parent);
        }
        else if (node.isRegex()) {
            return transformRegex(node.asRegex(), parent);
        }
        else if (node.isOr()) {
            return transformOr(node.asOr(), parent);
        }
        else if (node.isBracket()) {
            return transformBracket(node.asBracket(), parent);
        }
        else if (node.isName()) {
            return transformName(node.asName(), parent);
        }
        else if (node.isString()) {
            return transformString(node.asString(), parent);
        }
        return node;
    }

    public Node transformString(StringNode node, Node parent) {
        return node;
    }

    public Node transformGroup(GroupNode node, Node parent) {
        node.node = transformNode(node.node, node);
        return node;
    }

    public Node transformSequence(Sequence node, Node parent) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i), node));
        }
        return node;
    }

    public Node transformRegex(RegexNode node, Node parent) {
        node.node = transformNode(node.node, node);
        return node;
    }

    public Node transformOr(OrNode node, Node parent) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i), node));
        }
        return node;
    }

    public Node transformBracket(Bracket node, Node parent) {
        return node;
    }

    public Node transformName(NameNode node, Node parent) {
        return node;
    }
}
