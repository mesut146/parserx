package mesut.parserx.nodes;

public class Transformer extends BaseVisitor<Node, Void> {

    public Tree tree;
    protected RuleDecl curRule;
    protected TokenDecl curToken;

    public Transformer(Tree tree) {
        this.tree = tree;
    }

    public Transformer() {
    }

    public void transformAll() {
        transformTokens();
        transformRules();
    }

    public void transformTokens() {
        for (var tb : tree.tokenBlocks) {
            for (var decl : tb.tokens) {
                transformToken(decl);
            }
            for (var mb : tb.modeBlocks) {
                for (var decl : mb.tokens) {
                    transformToken(decl);
                }
            }
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

    public Node transformNode(Node node, Void arg) {
        return node.accept(this, arg);
    }

    @Override
    public Node visitDot(Dot dot, Void arg) {
        return dot;
    }

    @Override
    public Node visitBracket(Bracket bracket, Void arg) {
        return bracket;
    }

    @Override
    public Node visitGroup(Group group, Void arg) {
        group.node = group.node.accept(this, arg);
        return group;
    }

    @Override
    public Node visitOr(Or or, Void arg) {
        for (int i = 0; i < or.size(); i++) {
            or.set(i, or.get(i).accept(this, arg));
        }
        return or;
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            seq.set(i, seq.get(i).accept(this, arg));
        }
        return seq;
    }

    @Override
    public Node visitRegex(Regex regex, Void arg) {
        regex.node = regex.node.accept(this, arg);
        return regex;
    }

    @Override
    public Node visitString(StringNode string, Void arg) {
        return string;
    }

    @Override
    public Node visitName(Name name, Void arg) {
        return name;
    }

    @Override
    public Node visitEpsilon(Epsilon epsilon, Void arg) {
        return epsilon;
    }

    @Override
    public Node visitRange(Range range, Void arg) {
        return range;
    }

    @Override
    public Node visitShortcut(Shortcut shortcut, Void arg) {
        return shortcut;
    }

    @Override
    public Node visitUntil(Until until, Void arg) {
        return until;
    }


}
