package gen.lr;

import nodes.*;

import java.util.*;

import static gen.lr.Lr1Generator.dollar;

public class Lr1ItemSet {
    List<Lr1Item> first;
    List<Lr1Item> all = new ArrayList<>();
    int curIndex = 0;//rule index
    Set<Lr1Item> done = new LinkedHashSet<>();
    Tree tree;

    public Lr1ItemSet(List<Lr1Item> first, Tree tree) {
        this.first = first;
        this.tree = tree;
        all.addAll(this.first);
    }

    public Lr1ItemSet(Lr1Item first, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(first)), tree);
    }

    @Override
    public String toString() {
        //sort();
        return NodeList.join(all, "\n");
    }

    //get first item that can transit
    public Lr1Item findTransitable() {
        for (int i = curIndex; i < all.size(); i++) {
            Lr1Item item = all.get(i);
            if (!done.contains(item)) {
                Node token = item.getDotNode();
                if (token != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public void closure() {
        if (all.size() > 1) {
            return;
        }
        for (Lr1Item item : first) {
            if (item.isDotTerminal()) {
                closure(item.getDotNode());
            }
        }

    }

    void closure(NameNode node) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr1Item item = new Lr1Item(decl, 0);
                if (!all.contains(item)) {
                    all.add(item);
                    if (item.isDotTerminal()) {
                        closure(item.getDotNode());
                    }
                }
            }

        }
        else {
            throw new RuntimeException("closure error on node: " + node);
        }
    }

    public void lookaheads() {
        for (Lr1Item item : all) {
            NameNode node = item.getDotNode();

        }
    }

    //first terminals of rule
    public Set<NameNode> first(NameNode nameNode) {
        //todo if first has epsilon look next
        Set<NameNode> list = new HashSet<>();
        for (RuleDecl decl : tree.getRules(nameNode.name)) {
            Node node = decl.rhs;
            if (node.isSequence()) {
                //NameNode n = (NameNode) node.asSequence().get(0);
                handleFirst(node.asSequence().get(0), list);
            }
            else if (node.isName()) {
                if (node.isEmpty()) {
                    //look after
                    throw new RuntimeException("first epsilon");
                }
                else {
                    handleFirst(node.asName(), list);
                }
            }
        }
        return list;
    }

    Set<NameNode> first(Node rhs) {
        Set<NameNode> list = new HashSet<>();
        if (rhs.isEmpty()) {
            list.add(dollar());
        }
        else if (rhs.isName()) {
            if (rhs.asName().isToken) {
                list.add(rhs.asName());
            }
            else {
                list.addAll(first(rhs.asName()));
            }
        }
        else {
            Sequence sequence = rhs.asSequence();
            for (int i = 0; i < sequence.size(); i++) {
                Node n = sequence.get(i);
            }
        }
        return list;
    }

    void handleFirst(Node node, Set<NameNode> list) {
        if (node.isName()) {
            if (node.asName().isToken) {
                list.add(node.asName());
            }
            else {
                list.addAll(first(node.asName()));
            }
        }
    }

    //get tokens after the symbol can appear in itemset
    public Set<NameNode> follow(NameNode nameNode, Lr1ItemSet itemSet) {
        Set<NameNode> followSet = new HashSet<>();
        for (Lr1Item item : itemSet.all) {
            Node rhs = item.ruleDecl.rhs;
            if (rhs.isName() && rhs.equals(nameNode)) {
                //rightmost so add $
                followSet.add(dollar());
            }
            else if (rhs.isSequence()) {
                Sequence sequence = rhs.asSequence();
                for (int i = 0; i < sequence.size(); i++) {
                    if (sequence.get(i).equals(nameNode)) {
                        if (i < sequence.size() - 1) {
                            NameNode next = (NameNode) sequence.get(i + 1);
                            if (next.isToken) {
                                followSet.add(next);
                            }
                            else {
                                followSet.addAll(first(next));
                            }
                        }
                        else {
                            //rightmost
                            followSet.add(dollar());
                        }
                    }
                }
            }
        }
        return followSet;
    }

}
