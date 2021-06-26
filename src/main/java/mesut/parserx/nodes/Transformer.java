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

    public Node transformGroup(Group node) {
        return new Group(transformNode(node.node));
    }

    public Node transformSequence(Sequence node) {
        Sequence res = new Sequence();
        for (Node ch : node) {
            res.add(transformNode(ch));
        }
        return res;
    }

    public Node transformRegex(Regex node) {
        return new Regex(transformNode(node.node), node.type);
    }

    public Node transformOr(Or node) {
        Or res = new Or();
        for (Node ch : node) {
            res.add(transformNode(ch));
        }
        return res;
    }

    public Node transformBracket(Bracket node) {
        return node.normalize().optimize();
    }

    public Node transformName(Name node) {
        return node;
    }

}
