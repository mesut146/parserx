package mesut.parserx.nodes;

public class BaseVisitor<R, A> implements Visitor<R, A> {

    @Override
    public R visitEpsilon(Epsilon node, A arg) {
        return null;
    }

    @Override
    public R visitDot(Dot node, A arg) {
        return null;
    }

    @Override
    public R visitUntil(Until node, A arg) {
        return null;
    }

    @Override
    public R visitString(StringNode node, A arg) {
        return null;
    }

    @Override
    public R visitGroup(Group node, A arg) {
        return node.node.accept(this, arg);
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
    public R visitBracket(Bracket node, A arg) {
        return null;
    }

    @Override
    public R visitName(Name node, A arg) {
        return null;
    }

    @Override
    public R visitRange(Range node, A arg) {
        return null;
    }

    @Override
    public R visitShortcut(Shortcut node, A arg) {
        return null;
    }
}
