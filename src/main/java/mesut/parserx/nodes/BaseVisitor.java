package mesut.parserx.nodes;

public class BaseVisitor<R, A> implements Visitor<R, A> {

    @Override
    public R visitEpsilon(Epsilon epsilon, A arg) {
        return null;
    }

    @Override
    public R visitDot(Dot dot, A arg) {
        return null;
    }

    @Override
    public R visitUntil(Until until, A arg) {
        return null;
    }

    @Override
    public R visitString(StringNode string, A arg) {
        return null;
    }

    @Override
    public R visitGroup(Group group, A arg) {
        return null;
    }

    @Override
    public R visitSequence(Sequence seq, A arg) {
        return null;
    }

    @Override
    public R visitOr(Or or, A arg) {
        return null;
    }

    @Override
    public R visitRegex(Regex regex, A arg) {
        return null;
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
}
