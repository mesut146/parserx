package mesut.parserx.nodes;

public interface Visitor<R, A> {
    R visitEpsilon(Epsilon node, A arg);

    R visitDot(Dot node, A arg);

    R visitUntil(Until node, A arg);

    R visitString(StringNode node, A arg);

    R visitGroup(Group node, A arg);

    R visitSequence(Sequence seq, A arg);

    R visitRegex(Regex regex, A arg);

    R visitOr(Or or, A arg);

    R visitBracket(Bracket node, A arg);

    R visitName(Name node, A arg);
}
