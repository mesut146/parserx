package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrecedenceHandler {
    public static boolean isAssocLeft = true;
    Tree tree;
    HashMap<Integer, Holder> map = new HashMap<>();
    int lastLevel;
    RuleDecl decl;

    public PrecedenceHandler(Tree tree) {
        this.tree = tree;
    }

    public static void handle(Tree tree) {
        new PrecedenceHandler(tree).process();
    }

    void process() {
        for (RuleDecl decl : tree.rules) {
            if (!decl.rhs.isOr()) continue;
            this.decl = decl;
            lastLevel = 0;
            map.clear();

            Or or = decl.rhs.asOr();
            List<Node> primList = new ArrayList<>();
            //prevent regular ors being transformed
            boolean hasAny = false;
            for (Node ch : or) {
                if (ch.isSequence() && (ch.asSequence().assocLeft || ch.asSequence().assocRight)) {
                    hasAny = true;
                }
            }
            if (!hasAny) continue;
            collect(decl, or, primList);
            if (map.isEmpty()) {
                continue;
            }
            Node primNode = primList.size() == 1 ? primList.get(0) : new Or(primList);
            RuleDecl primRule = new RuleDecl(tree.getFreeName("PRIM_" + decl.baseName()), primNode);
            primRule.retType = decl.retType;
            //unary is always right assoc
            //postfix is always left assoc
            //binary can be both
            for (int i = 1; i <= lastLevel; i++) {
                Holder holder = map.get(i);
                String name = i == 1 ? decl.baseName() : holder.name;
                Name curRef = new Name(name);
                Name higher = new Name(i == lastLevel ? primRule.baseName() : map.get(i + 1).name);
                higher.astInfo.outerVar = "res";
                higher.astInfo.isPrimary = true;

                LevelInfo info = holder.info;
                Node rhs;
                if (info == LevelInfo.binary) {
                    rhs = makeBinary(decl, holder, curRef, higher);
                }
                else if (info == LevelInfo.unary) {
                    rhs = makeUnary(holder, curRef, higher);
                }
                else {
                    rhs = makePostfix(decl, holder, higher);
                }

                if (i == 1) {
                    //no need to create
                    decl.rhs = rhs;
                }
                else {
                    RuleDecl newDecl = new RuleDecl(name, rhs);
                    newDecl.retType = decl.retType;
                    tree.addRule(newDecl);
                }
            }
            tree.addRule(primRule);
            break;
        }
    }

    void addLevel(LevelInfo info, Sequence node) {
        int level = ++lastLevel;
        Holder holder = new Holder();
        holder.info = info;
        holder.name = tree.getFreeName(decl.baseName() + level);
        holder.node = node;
        map.put(level, holder);
    }

    private Node makeUnary(Holder holder, Name curRef, Name higher) {
        //right assoc
        //E: op1 op2 E | E2
        Sequence seq = holder.node.copy();
        curRef.astInfo = seq.last().astInfo.copy();
        seq.set(seq.size() - 1, curRef);
        return new Or(seq, higher);
    }

    AstInfo makeFactor(String name) {
        AstInfo info = new AstInfo();
        info.isFactor = true;
        info.varName = name;
        return info;
    }

    private Node makeBinary(RuleDecl decl, Holder holder, Name curRef, Name higher) {
        //E: E op E | E2
        if (holder.node.assocLeft || !holder.node.assocRight && isAssocLeft) {
            //left assoc
            //E: E2 (E2(E2) op E2])*
            Sequence seq = holder.node.copy();
            Name factored = higher.copy();
            factored.astInfo = seq.first().astInfo.copy();
            factored.astInfo.isFactored = true;
            factored.astInfo.factor = makeFactor("res");
            Epsilon eps = new Epsilon();
            eps.astInfo.isFactored = true;
            eps.astInfo.factor = makeFactor("tmp");
            eps.astInfo.varName = "res";
            Name h2 = higher.copy();
            h2.astInfo = seq.last().astInfo.copy();
            seq.set(0, factored);
            seq.set(seq.size() - 1, h2);
            seq.add(eps);
            Group group = new Group(seq);
            group.astInfo.nodeType = decl.retType;
            group.astInfo.varName = "tmp";
            seq.astInfo.outerVar = "tmp";

            return new Sequence(higher, new Regex(group, "*"));
        }
        else {
            //right assoc
            //E: E2 op E | E2
            Sequence seq = holder.node.copy();
            Name h2 = higher.copy();
            h2.astInfo = seq.first().astInfo.copy();
            curRef.astInfo = seq.last().astInfo.copy();
            seq.set(0, h2);
            seq.set(seq.size() - 1, curRef);
            return new Or(seq, higher);
        }
    }

    private Node makePostfix(RuleDecl decl, Holder holder, Name higher) {
        //postfix
        //left assoc
        //E: E op | E2
        //E: E2 (E2(E2) op)*
        Sequence seq = holder.node.copy();

        Name factored = higher.copy();
        factored.astInfo = holder.node.get(0).astInfo.copy();
        factored.astInfo.isFactored = true;
        factored.astInfo.factor = makeFactor("res");

        Epsilon eps = new Epsilon();
        eps.astInfo.isFactored = true;
        eps.astInfo.factor = makeFactor("tmp");
        eps.astInfo.varName = "res";

        seq.set(0, factored);
        seq.add(eps);
        Group group = new Group(seq);
        group.astInfo.nodeType = decl.retType;
        group.astInfo.varName = "tmp";
        seq.astInfo.outerVar = "tmp";

        return new Sequence(higher, new Regex(group, "*"));
    }

    private void collect(RuleDecl decl, Or or, List<Node> primList) {
        for (int i = or.size() - 1; i >= 0; i--) {
            Node ch = or.get(i);
            if (!ch.isSequence()) {
                primList.add(ch);
                continue;
            }
            Sequence seq = ch.asSequence();
            if (seq.get(0).equals(decl.ref)) {
                if (seq.last().equals(decl.ref)) {
                    addLevel(LevelInfo.binary, seq);
                }
                else {
                    addLevel(LevelInfo.postfix, seq);
                }
            }
            else if (seq.last().equals(decl.ref)) {
                addLevel(LevelInfo.unary, seq);
            }
            else {
                primList.add(ch);
            }
        }
    }

    enum LevelInfo {
        unary, binary, postfix;
    }

    static class Holder {
        LevelInfo info;
        Sequence node;
        String name;
    }
}
