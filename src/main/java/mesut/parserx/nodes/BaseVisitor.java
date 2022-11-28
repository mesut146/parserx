package mesut.parserx.nodes;

public class BaseVisitor<R, A> implements Visitor<R, A> {
    public RuleDecl curRule;

    @Override
    public R visitEpsilon(Epsilon epsilon, A arg) {
        return null;
    }

    @Override
    public R visitFactored(Factored factored, A arg) {
        return null;
    }

    @Override
    public R visitDot(Dot dot, A arg) {
        return null;
    }

    @Override
    public R visitUntil(Until until, A arg) {
        return until.node.accept(this, arg);
    }

    @Override
    public R visitString(StringNode string, A arg) {
        return null;
    }

    @Override
    public R visitGroup(Group group, A arg) {
        return group.node.accept(this, arg);
    }

    @Override
    public R visitSequence(Sequence seq, A arg) {
        for (Node ch : seq) {
            ch.accept(this, arg);
        }
        return null;
    }

    @Override
    public R visitOr(Or or, A arg) {
        for (Node ch : or) {
            ch.accept(this, arg);
        }
        return null;
    }

    @Override
    public R visitRegex(Regex regex, A arg) {
        return regex.node.accept(this, arg);
    }

    @Override
    public R visitBracket(Bracket bracket, A arg) {
        return null;
    }

    @Override
    public R visitName(Name name, A arg) {
        return null;
    }

    @Override
    public R visitRange(Range range, A arg) {
        return null;
    }

    @Override
    public R visitShortcut(Shortcut shortcut, A arg) {
        return null;
    }

    @Override
    public R visitSub(Sub sub, A arg) {
        sub.node.accept(this, arg);
        sub.string.accept(this, arg);
        return null;
    }

    @Override
    public R visitModeBlock(ModeBlock modeBlock, A arg) {
        return null;
    }
}
