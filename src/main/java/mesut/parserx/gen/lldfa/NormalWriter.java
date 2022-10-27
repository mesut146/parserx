package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.ParserUtils;
import mesut.parserx.nodes.*;

public class NormalWriter extends BaseVisitor<Void, Void> {
    CodeWriter w;
    Tree tree;

    public NormalWriter(CodeWriter w, Tree tree) {
        this.w = w;
        this.tree = tree;
    }

    @Override
    public Void visitOr(Or or, Void arg) {
        int id = 1;
        for (var ch : or) {
            w.append("%sif(%s){", id > 1 ? "else " : "", ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type"));
            w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
            w.append("%s.holder = res;", ch.astInfo.varName);
            w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
            w.append("res.which = %s;", id++);
            ch.accept(this, arg);
            w.append("}");
        }
        w.append("else throw new RuntimeException(\"expecting one of %s got: \"+ts.la);", FirstSet.tokens(or, tree));
        return null;
    }

    @Override
    public Void visitName(Name name, Void arg) {
        consumer(name);
        return null;
    }

    @Override
    public Void visitRegex(Regex regex, Void arg) {
        var ch = regex.node;
        var la = ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type");
        if (regex.isOptional()) {
            w.append("if(%s){", la);
            ch.accept(this, null);
            w.append("}");
        }
        else if (regex.isStar()) {
            w.append("while(%s){", la);
            ch.accept(this, null);
            w.append("}");
        }
        else {
            w.append("do{");
            ch.accept(this, null);
            w.down();
            w.append("}while(%s);", la);
        }
        return null;
    }

    private void consumer(Name name) {
        String outer = name.astInfo.outerVar;
        String rhs;
        if (name.isToken) {
            rhs = String.format("ts.consume(%s.%s, \"%s\")", ParserUtils.tokens, name.name, name.name);
        }
        else {
            rhs = name.name + "()";
        }
        if (name.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", outer, name.astInfo.varName, rhs);
        }
        else {
            w.append("%s.%s = %s;", outer, name.astInfo.varName, rhs);
        }
    }
}
