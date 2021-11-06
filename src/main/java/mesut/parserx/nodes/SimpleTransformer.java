package mesut.parserx.nodes;

public class SimpleTransformer {

    public Tree tree;
    public RuleDecl curRule;
    public TokenDecl curToken;

    public SimpleTransformer(Tree tree) {
        this.tree = tree;
    }

    public void transformAll() {
        transformTokens();
        transformRules();
    }

    private void transformTokens() {
        for (TokenDecl decl : tree.tokens) {
            transformToken(decl);
        }
    }

    public void transformRules() {
        for (RuleDecl decl : tree.rules) {
            transformRule(decl);
        }
    }

    public RuleDecl transformRule(RuleDecl decl) {
        curRule = decl;
        decl.rhs = transformNode(decl.rhs, null);
        curRule = null;
        return decl;
    }

    public TokenDecl transformToken(TokenDecl decl) {
        curToken = decl;
        decl.rhs = transformNode(decl.rhs, null);
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
        else if (node.isEpsilon()) {
            return transformEpsilon((Epsilon) node, parent);
        }
        return node;
    }

    public Node transformEpsilon(Epsilon node, Node parent) {
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

    public Node transformSequence(Sequence seq, Node parent) {
        for (int i = 0; i < seq.size(); i++) {
            seq.set(i, transformNode(seq.get(i), seq));
        }
        return seq;
    }

    public Node transformRegex(Regex regex, Node parent) {
        regex.node = transformNode(regex.node, regex);
        return regex;
    }

    public Node transformOr(Or or, Node parent) {
        for (int i = 0; i < or.size(); i++) {
            or.set(i, transformNode(or.get(i), or));
        }
        return or;
    }

    public Node transformBracket(Bracket node, Node parent) {
        return node;
    }

    public Node transformName(Name name, Node parent) {
        return name;
    }

}
