package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Prec {
    public static boolean isAssocLeft = false;
    Tree tree;
    HashMap<Integer, List<Name>> levels = new HashMap<>();
    HashMap<Integer, LevelInfo> levelInfos = new HashMap<>();
    int lastLevel;

    public Prec(Tree tree) {
        this.tree = tree;
    }

    public static void handle(Tree tree) {
        new Prec(tree).process();
    }

    void addLevel(LevelInfo info, Name... ops) {
        int level = ++lastLevel;
        levels.put(level, new ArrayList<>(Arrays.asList(ops)));
        levelInfos.put(level, info);
    }

    void process() {
        for (RuleDecl decl : tree.rules) {
            if (!decl.rhs.isOr()) continue;
            lastLevel = 0;
            levels.clear();
            levelInfos.clear();

            Or or = decl.rhs.asOr();
            List<Node> prim = new ArrayList<>();
            for (int i = or.size() - 1; i >= 0; i--) {
                Node ch = or.get(i);
                if (!ch.isSequence()) {
                    prim.add(ch);
                    continue;
                }
                Sequence seq = ch.asSequence();
                if (seq.size() == 3) {
                    if (seq.get(0).equals(decl.ref) && seq.get(2).equals(decl.ref) && !seq.get(1).equals(decl.ref)) {
                        Node op = seq.get(1);
                        if (op.isName()) {
                            Name name = op.asName();
                            if (name.isToken) {
                                addLevel(LevelInfo.binary, op.asName());
                            }
                            else {
                                Or rhs = tree.getRule(name).rhs.asOr();
                                //order matters
                                for (int j = rhs.size() - 1; j >= 0; j--) {
                                    addLevel(LevelInfo.binary, rhs.get(j).asName());
                                }
                            }
                        }
                        else {
                            //same order
                            Group group = op.asGroup();
                            List<Name> list = new ArrayList<>();
                            for (Node o : group.node.asOr()) {
                                list.add(o.asName());
                            }
                            addLevel(LevelInfo.binary, list.toArray(new Name[0]));
                        }
                    }
                    else {
                        prim.add(ch);
                    }
                }
                else if (seq.size() == 2) {
                    if (seq.get(1).equals(decl.ref) && !seq.get(0).equals(decl.ref)) {
                        addLevel(LevelInfo.unary, seq.get(0).asName());
                    }
                    else if (seq.get(0).equals(decl.ref) && !seq.get(1).equals(decl.ref)) {
                        addLevel(LevelInfo.postfix, seq.get(1).asName());
                    }
                    else {
                        prim.add(ch);
                    }
                }
                else {
                    prim.add(ch);
                }
            }//for or
            HashMap<Integer, String> names = new HashMap<>();
            for (Integer level : levels.keySet()) {
                names.put(level, decl.baseName() + level);
            }
            RuleDecl primRule = new RuleDecl("PRIM", new Or(prim).normal());
            for (int i = 1; i <= lastLevel; i++) {
                List<Name> ops = levels.get(i);
                Node opNode;
                if (ops.size() == 1) {
                    opNode = ops.get(0);
                }
                else {
                    opNode = new Group(new Or(ops.toArray(new Node[0])));
                }
                String name = names.get(i);
                String higher = i == lastLevel ? primRule.baseName() : names.get(i + 1);

                LevelInfo info = levelInfos.get(i);
                Node rhs;
                if (info == LevelInfo.binary) {
                    Node right = new Name(name);
                    if (i == lastLevel) {
                        right = decl.ref;
                    }
                    if (isLeft(ops)) {
                        right = new Name(higher);
                    }
                    //A: B (op [B,A])*
                    rhs = new Sequence(new Name(higher), new Regex(new Group(new Sequence(opNode, right)), "*"));
                }
                else if (info == LevelInfo.unary) {
                    rhs = new Sequence(new Regex(opNode, "*"), new Name(higher));
                }
                else {
                    rhs = new Sequence(new Name(higher), new Regex(opNode, "*"));
                }

                if (i == 1) {
                    //no need to create
                    decl.rhs = rhs;
                }
                else {
                    tree.addRule(new RuleDecl(name, rhs));
                }
            }
            tree.addRule(primRule);
            System.out.println(names);
            break;
        }
    }

    boolean isLeft(List<Name> list) {
        if (list.size() == 1) {
            Assoc assoc = tree.getAssoc(list.get(0));
            return assoc != null && assoc.isLeft;
        }
        else {
            boolean hasRight = false;
            for (Name name : list) {
                Assoc assoc = tree.getAssoc(name);
                if (assoc != null) {
                    if (hasRight && assoc.isLeft) {
                        throw new RuntimeException("conflicting assoc");
                    }
                    hasRight = !assoc.isLeft;
                }
            }
            if (hasRight) return false;
            return isAssocLeft;
        }
    }

    enum LevelInfo {
        unary, binary, postfix;
    }
}
