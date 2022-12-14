package mesut.parserx.gen.ast;

import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.gen.lldfa.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;

import java.io.IOException;

//generate ast file and astinfo per node
public class AstGen extends BaseVisitor<Void, Void> {
    Tree tree;
    RuleDecl curRule;
    String outer;
    Type outerCls;
    CountingMap2<String, String> varCount = new CountingMap2<>();

    public AstGen(Tree tree) {
        this.tree = tree;
    }

    public static void gen(Tree tree, Lang target) throws IOException {
        new AstGen(tree).gen();
        if (target == Lang.JAVA) {
            new JavaAst(tree).genAst();
        }
        else if (target == Lang.CPP) {
            new CppAst(tree).genAst();
        }
    }

    public void gen() {
        for (var decl : tree.rules) {
            //decl.retType = new Type(tree.options.astClass, Utils.camel(decl.baseName()) + tree.options.nodeSuffix);
            decl.retType = new Type(tree.options.astClass, decl.baseName() + tree.options.nodeSuffix);
            curRule = decl;
            outerCls = decl.retType;
            outer = "res";
            decl.rhs.accept(this, null);
        }
    }

    @Override
    public Void visitOr(Or or, Void arg) {
        for (int id = 1; id <= or.size(); id++) {
            var ch = or.get(id - 1);
            if (ch.isEpsilon()) continue;
            ch.astInfo.which = id;
            ch.astInfo.nodeType = new Type(curRule.retType, "Alt" + id);
            ch.astInfo.varName = altVar(ch);
            ch.astInfo.outerVar = outer;
            outer = ch.astInfo.varName;
            outerCls = ch.astInfo.nodeType;
            ch.accept(this, null);
        }
        return null;
    }

    @Override
    public Void visitName(Name name, Void arg) {
        name.astInfo.outerVar = outer;
        name.astInfo.nodeType = new Type(name.isToken ? tree.options.tokenClass : name.name);
        name.astInfo.varName = getVarName(name);
        return null;
    }

    @Override
    public Void visitRegex(Regex regex, Void arg) {
        Name ch = regex.node.asName();
        regex.astInfo.outerVar = outer;
        regex.astInfo.varName = getVarName(ch);
        //ch doesn't have varName
        ch.astInfo.outerVar = outer;
        ch.astInfo.varName = regex.astInfo.varName;
        ch.astInfo.nodeType = new Type(ch.isToken ? tree.options.tokenClass : ch.name);
        return null;
    }

    String getVarName(Name name) {
        if (name.astInfo.varName != null) return name.astInfo.varName;
        int i = varCount.get(outerCls.toString(), name.name);
        return i == 1 ? name.name : name.name + i;
    }

    String altVar(Node ch) {
        if (ch.isName()) {
            return ch.asName().name;
        }
        else if (ch.isSequence() && ch.asSequence().size() == 1) {
            return ItemSet.sym(ch.asSequence().get(0)).name;
        }
        else {
            return ch.astInfo.nodeType.name.toLowerCase();
        }
    }
}
