package mesut.parserx.nodes;

public class SimpleTransformer {

    public RuleDecl curRule;
    public TokenDecl curToken;

    public void transformAll(Tree tree) {
        for (TokenDecl decl : tree.tokens) {
            transformToken(decl);
        }
        for (RuleDecl decl : tree.rules) {
            transformRule(decl);
        }
    }

    public RuleDecl transformRule(RuleDecl decl) {
        curRule = decl;
        decl.rhs = transformNode(decl.rhs, decl);
        curRule = null;
        return decl;
    }

    public TokenDecl transformToken(TokenDecl decl) {
        curToken = decl;
        decl.regex = transformNode(decl.regex, decl);
        curToken = null;
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
        else if (node instanceof Until) {
            return transformUntil(((Until) node), parent);
        }
        else if (node instanceof Dot) {
            return transformDot(((Dot) node), parent);
        }
        return node;
    }

    public Node transformDot(Dot node, Node parent) {
        return node;
    }

    public Node transformUntil(Until node, Node parent) {
        return node;
    }

    public Node transformString(StringNode node, Node parent) {
        return node;
    }

    public Node transformGroup(Group node, Node parent) {
        node.node = transformNode(node.node, node);
        return node;
    }

    public Node transformSequence(Sequence node, Node parent) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i), node));
        }
        return node;
    }

    public Node transformRegex(Regex node, Node parent) {
        node.node = transformNode(node.node, node);
        return node;
    }

    public Node transformOr(Or node, Node parent) {
        for (int i = 0; i < node.size(); i++) {
            node.set(i, transformNode(node.get(i), node));
        }
        return node;
    }

    public Node transformBracket(Bracket node, Node parent) {
        return node;
    }

    public Node transformName(Name node, Node parent) {
        return node;
    }

}
