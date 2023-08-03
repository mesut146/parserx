package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.ParserUtils;
import mesut.parserx.nodes.*;

public class NormalWriter extends BaseVisitor<Void, Void> {
    CodeWriter w;
    Tree tree;
    int index = 0;
    Node prev;

    public NormalWriter(CodeWriter w, Tree tree) {
        this.w = w;
        this.tree = tree;
    }

    @Override
    public Void visitOr(Or or, Void arg) {
        int id = 1;
        for (var ch : or) {
            w.append("%sif(%s){", id > 1 ? "else " : "", ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type"));
            ch.accept(this, arg);
            w.append("}");
            id++;
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
    public Void visitFactored(Factored factored, Void arg) {
        var outer = factored.astInfo.outerVar;
        if (factored.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", outer, factored.astInfo.varName, factored.name);
        } else {
            w.append("%s.%s = %s;", outer, factored.astInfo.varName, factored.name);
        }
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
        } else if (regex.isStar()) {
            w.append("while(%s){", la);
            ch.accept(this, null);
            w.append("}");
        } else {
            w.append("do{");
            ch.accept(this, null);
            w.down();
            w.append("}while(%s);", la);
        }
        return null;
    }

    private void consumer(Name name) {
        if (curRule.recInfo != null && (curRule.recInfo.isState || curRule.recInfo.isRec)) {
            recConsumer(name);
            return;
        }
        String outer = name.astInfo.outerVar;
        String rhs;
        if (name.isToken) {
            rhs = String.format("ts.consume(%s.%s, \"%s\")", ParserUtils.tokens, name.name, name.name);
        } else {
            rhs = name.name + "()";
        }
        if (name.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", outer, name.astInfo.varName, rhs);
        } else {
            w.append("%s.%s = %s;", outer, name.astInfo.varName, rhs);
        }
        if (name.action != null) {
            w.append(trimAction(name.action));
        }
    }

    String trimAction(String act) {
        return act.substring(2, act.length() - 2);
    }

    private void recConsumer(Name name) {
        if (index == 0) {
            var arg = "";
            if (!name.args2.isEmpty()) {
                arg = name.args2.get(0).name;
            }
            if (name.astInfo.isPrimary) {
                w.append("res = %s(%s);", name.name, arg);
            } else {
                var type = tree.getRule(name).retType;
                w.append("%s tmp = %s(%s);", type, name.name, arg);
            }
        } else {
            if (prev.astInfo.isPrimary) {
                w.append("res = %s(res);", name.name);
            } else {
                w.append("res = %s(tmp);", name.name);
            }
        }
    }

    void createAlt(Node seq) {
        if (seq.astInfo.nodeType == null) return;
        w.append("%s %s = new %s();", seq.astInfo.nodeType, seq.astInfo.varName, seq.astInfo.nodeType);
        w.append("%s.holder = res;", seq.astInfo.varName);
        w.append("res.%s = %s;", seq.astInfo.varName, seq.astInfo.varName);
        w.append("res.which = %s;", seq.astInfo.which.get());
    }

    @Override
    public Void visitSequence(Sequence seq, Void arg) {
        index = 0;
        createAlt(seq);
        prev = null;
        for (Node ch : seq) {
            ch.accept(this, arg);
            index++;
            prev = ch;
        }
        return null;
    }
}
