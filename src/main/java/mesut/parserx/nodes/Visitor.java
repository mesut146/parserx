package mesut.parserx.nodes;

public interface Visitor<R, A> {
    R visitEpsilon(Epsilon epsilon, A arg);

    R visitDot(Dot dot, A arg);

    R visitUntil(Until until, A arg);

    R visitString(StringNode string, A arg);

    R visitGroup(Group group, A arg);

    R visitSequence(Sequence seq, A arg);

    R visitRegex(Regex regex, A arg);

    R visitOr(Or or, A arg);

    R visitBracket(Bracket bracket, A arg);

    R visitName(Name name, A arg);

    R visitRange(Range range, A arg);

    R visitShortcut(Shortcut shortcut, A arg);

    R visitModeBlock(ModeBlock modeBlock, A arg);

}
