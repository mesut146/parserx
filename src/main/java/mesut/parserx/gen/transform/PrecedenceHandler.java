package mesut.parserx.gen.transform;

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

    void addLevel(LevelInfo info, Sequence node, Node opNode) {
        int level = ++lastLevel;
        Holder holder = new Holder();
        holder.info = info;
        holder.name = decl.baseName() + level;
        holder.node = node;
        holder.opNode = opNode;
        map.put(level, holder);
    }

    void process() {
        for (RuleDecl decl : tree.rules) {
            if (!decl.rhs.isOr()) continue;
            this.decl = decl;
            lastLevel = 0;
            map.clear();

            Or or = decl.rhs.asOr();
            List<Node> primList = new ArrayList<>();
            for (int i = or.size() - 1; i >= 0; i--) {
                Node ch = or.get(i);
                if (!ch.isSequence()) {
                    primList.add(ch);
                    continue;
                }
                Sequence seq = ch.asSequence();
                if (seq.size() == 5) {
                    if (seq.get(0).equals(decl.ref) &&
                            !seq.get(1).equals(decl.ref) &&
                            seq.get(2).equals(decl.ref) &&
                            !seq.get(3).equals(decl.ref) &&
                            seq.get(4).equals(decl.ref)) {
                        addLevel(LevelInfo.ternary, seq, null);
                    }
                    else {
                        primList.add(ch);
                    }
                }
                else if (seq.size() == 3) {
                    if (seq.get(0).equals(decl.ref) &&
                            seq.get(2).equals(decl.ref) &&
                            !seq.get(1).equals(decl.ref)) {
                        addLevel(LevelInfo.binary, seq, seq.get(1));
                    }
                    else {
                        primList.add(ch);
                    }
                }
                else if (seq.size() == 2) {
                    if (seq.get(1).equals(decl.ref) && !seq.get(0).equals(decl.ref)) {
                        addLevel(LevelInfo.unary, seq, seq.get(0));
                    }
                    else if (seq.get(0).equals(decl.ref) && !seq.get(1).equals(decl.ref)) {
                        addLevel(LevelInfo.postfix, seq, seq.get(1));
                    }
                    else {
                        primList.add(ch);
                    }
                }
                else {
                    primList.add(ch);
                }
            }//for or
            RuleDecl primRule = new RuleDecl("PRIM", new Or(primList).normal());
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
                if (info == LevelInfo.ternary) {
                    //E: E op1 E op2 E | E2
                    if (holder.node.assocLeft || !holder.node.assocRight && isAssocLeft) {
                        //E: E2 (E2(E2) op1 E op2 E2)*
                        Name factored = higher.copy();
                        Name op1 = holder.node.get(1).asName();
                        Name op2 = holder.node.get(3).asName();
                        Name h2 = higher.copy();
                        Epsilon eps = new Epsilon();
                        factored.astInfo = holder.node.get(0).astInfo.copy();
                        factored.astInfo.isFactored = true;
                        factored.astInfo.factorName = "res";
                        op1.astInfo = holder.node.get(1).astInfo.copy();
                        op2.astInfo = holder.node.get(3).astInfo.copy();
                        h2.astInfo = holder.node.get(4).astInfo.copy();
                        Sequence seq = new Sequence(factored, op1, holder.node.get(2), op2, h2, eps);
                        seq.astInfo.from(holder.node.astInfo);
                        Group group = new Group(seq);
                        group.astInfo.createNode = true;
                        group.astInfo.nodeType = decl.retType;
                        group.astInfo.varName = "tmp";
                        seq.astInfo.from(holder.node.astInfo);
                        seq.astInfo.outerVar = "tmp";
                        eps.astInfo.isFactored = true;
                        eps.astInfo.factorName = "tmp";
                        eps.astInfo.varName = "res";
                        rhs = new Sequence(higher, new Regex(group, "*"));
                    }
                    else {
                        //E: E2 op1 E op2 E | E2
                        Name h2 = higher.copy();
                        Name op1 = holder.node.get(1).asName();
                        Name op2 = holder.node.get(3).asName();
                        h2.astInfo = holder.node.get(0).astInfo.copy();
                        op1.astInfo = holder.node.get(1).astInfo.copy();
                        op2.astInfo = holder.node.get(3).astInfo.copy();
                        Sequence seq = new Sequence(h2, op1, holder.node.get(2), op2, holder.node.get(4));
                        seq.astInfo.from(holder.node.astInfo);
                        rhs = new Or(seq, higher);
                    }
                }
                else if (info == LevelInfo.binary) {
                    Node opNode = holder.opNode.copy();
                    //E: E op E | E2
                    if (holder.node.assocLeft || !holder.node.assocRight && isAssocLeft) {
                        //left assoc
                        //E: E2 (E2(E2) op E2])*
                        Name factored = higher.copy();
                        factored.astInfo = holder.node.get(0).astInfo.copy();
                        factored.astInfo.isFactored = true;
                        factored.astInfo.factorName = "res";
                        opNode.astInfo = holder.node.get(1).astInfo.copy();
                        Name h2 = higher.copy();
                        h2.astInfo = holder.node.get(2).astInfo.copy();
                        Epsilon eps = new Epsilon();
                        Sequence seq = new Sequence(factored, opNode, h2, eps);
                        Group group = new Group(seq);
                        group.astInfo.createNode = true;
                        group.astInfo.nodeType = decl.retType;
                        group.astInfo.varName = "tmp";
                        seq.astInfo.from(holder.node.astInfo);
                        seq.astInfo.outerVar = "tmp";
                        eps.astInfo.isFactored = true;
                        eps.astInfo.factorName = "tmp";
                        eps.astInfo.varName = "res";
                        rhs = new Sequence(higher, new Regex(group, "*"));
                    }
                    else {
                        //right assoc
                        //E: E2 op E | E2
                        Name h2 = higher.copy();
                        h2.astInfo = holder.node.get(0).astInfo.copy();
                        opNode.astInfo = holder.node.get(1).astInfo.copy();
                        curRef.astInfo = holder.node.get(2).astInfo.copy();
                        Sequence seq = new Sequence(h2, opNode, curRef);
                        seq.astInfo.from(holder.node.astInfo);
                        rhs = new Or(seq, higher);
                    }
                }
                else if (info == LevelInfo.unary) {
                    //right assoc
                    //E: op E | E2
                    Node opNode = holder.opNode.copy();
                    Sequence seq = new Sequence(opNode, curRef);
                    seq.astInfo = holder.node.astInfo.copy();
                    opNode.astInfo = holder.node.get(0).astInfo.copy();
                    curRef.astInfo = holder.node.get(1).astInfo.copy();
                    rhs = new Or(seq, higher);
                }
                else {
                    //postfix
                    //left assoc
                    //E: E op | E2
                    //E: E2 (E2(E2) op)*
                    Node opNode = holder.opNode.copy();
                    Name factored = higher.copy();
                    Epsilon eps = new Epsilon();
                    factored.astInfo = holder.node.get(0).astInfo.copy();
                    factored.astInfo.isFactored = true;
                    factored.astInfo.factorName = "res";
                    opNode.astInfo = holder.node.get(1).astInfo.copy();
                    Sequence seq = new Sequence(factored, opNode, eps);
                    Group group = new Group(seq);
                    group.astInfo.createNode = true;
                    group.astInfo.nodeType = decl.retType;
                    group.astInfo.varName = "tmp";
                    seq.astInfo.from(holder.node.astInfo);
                    seq.astInfo.outerVar = "tmp";
                    eps.astInfo.isFactored = true;
                    eps.astInfo.factorName = "tmp";
                    eps.astInfo.varName = "res";
                    rhs = new Sequence(higher, new Regex(group, "*"));
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

    enum LevelInfo {
        unary, binary, postfix, ternary;
    }

    static class Holder {
        Node opNode;
        LevelInfo info;
        Sequence node;
        String name;
    }
}
