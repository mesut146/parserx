package nodes;

public class SimpleTransformer {

    public RuleDecl transformRule(RuleDecl decl) {
        decl.rhs = transformNode(decl.rhs);
        return decl;
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
        else if (node.isName()) {
            return transformName(node.asName());
        }
        else {
            System.out.println("no transform for:" + node.getClass() + " =" + node);
        }
        return node;
    }

    public Node transformGroup(GroupNode node) {
        node.node = transformNode(node.node);
        return node;
    }

    public Node transformSequence(Sequence node) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i)));
        }
        return node;
    }

    public Node transformRegex(RegexNode node) {
        node.node = transformNode(node.node);
        return node;
    }

    public Node transformOr(OrNode node) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i)));
        }
        return node;
    }

    public Node transformBracket(Bracket node) {
        return node;
    }

    public Node transformName(NameNode node) {
        return node;
    }
}
