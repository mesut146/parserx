package mesut.parserx.nodes;

public class Printer extends BaseVisitor<String, Void> {

    public boolean antlr = false;
    String indent = "    ";
    Tree tree;

    public Printer(Tree tree) {
        this.tree = tree;
    }

    public String print(){
        return tree.toString();
    }

    public String printRule(RuleDecl decl) {
        StringBuilder sb = new StringBuilder();
        sb.append(decl.getName());
        if (antlr) {
            sb.append("\n").append(indent).append(":");
            if (!decl.rhs.isOr()) {
                sb.append(indent);
            }
            sb.append(decl.rhs.accept(this, null));
            sb.append("\n").append(indent).append(";");
        }
        else {
            sb.append(": ").append(decl.rhs.accept(this, null)).append(";");
        }
        return sb.toString();
    }

    public String printToken(TokenDecl decl) {
        return decl.name + ": " + decl.rhs.accept(this, null) + ";";
    }

    @Override
    public String visitUntil(Until until, Void arg) {
        return until.toString();
    }

    @Override
    public String visitShortcut(Shortcut shortcut, Void arg) {
        return shortcut.toString();
    }

    @Override
    public String visitEpsilon(Epsilon epsilon, Void arg) {
        return epsilon.toString();
    }

    @Override
    public String visitName(Name name, Void arg) {
        return name.toString();
    }

    @Override
    public String visitString(StringNode string, Void arg) {
        return string.toString();
    }

    @Override
    public String visitGroup(Group group, Void arg) {
        return group.toString();
    }

    @Override
    public String visitDot(Dot dot, Void arg) {
        return ".";
    }

    @Override
    public String visitBracket(Bracket bracket, Void arg) {
        return bracket.toString();
    }

    @Override
    public String visitRegex(Regex regex, Void arg) {
        return regex.toString();
    }

    @Override
    public String visitRange(Range range, Void arg) {
        return range.toString();
    }

    @Override
    public String visitSequence(Sequence seq, Void arg) {
        var sb = new StringBuilder();
        for (int i = 0; i < seq.size(); i++) {
            sb.append(seq.get(i).accept(this, arg));
            if (i < seq.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public String visitOr(Or or, Void arg) {
        var sb = new StringBuilder();
        for (int i = 0; i < or.size(); i++) {
            if (antlr) {
                sb.append(indent);
            }
            sb.append(or.get(i).accept(this, arg));
            if (i < or.size() - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }
}
