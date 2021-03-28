package mesut.parserx.nodes;

//transform ast without modifying original
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
        else if (node.isName()) {
            return transformName(node.asName());
        }
        else {
            System.out.println("no transform for:" + node.getClass() + " =" + node);
        }
        return node;
    }

    public Node transformGroup(GroupNode node) {
        return new GroupNode(transformNode(node.node));
    }

    public Node transformSequence(Sequence node) {
        Sequence res = new Sequence();
        for (Node ch : node) {
            res.add(transformNode(ch));
        }
        return res;
    }

    public Node transformRegex(RegexNode node) {
        return new RegexNode(transformNode(node.node), node.type);
    }

    public Node transformOr(OrNode node) {
        OrNode res = new OrNode();
        for (Node ch : node) {
            res.add(transformNode(ch));
        }
        return res;
    }

    public Node transformBracket(Bracket node) {
        return node.normalize().optimize();
    }

    public Node transformName(NameNode node) {
        return node;
    }

}
