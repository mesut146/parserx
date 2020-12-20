package gen.lr;

import nodes.*;

import java.util.*;


public class Lr1ItemSet {
    List<Lr1Item> kernel;
    List<Lr1Item> all = new ArrayList<>();
    int curIndex = 0;//rule index
    Set<Lr1Item> done = new LinkedHashSet<>();
    Tree tree;

    public Lr1ItemSet(List<Lr1Item> kernel, Tree tree) {
        this.kernel = kernel;
        this.tree = tree;
    }

    public Lr1ItemSet(Lr1Item kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
    }

    @Override
    public String toString() {
        //sort();
        if (all.isEmpty()) {//not processed yet
            return NodeList.join(kernel, "\n");
        }
        return NodeList.join(all, "\n");
    }

    //get first item that can transit
    public Lr1Item findTransitable() {
        for (int i = curIndex; i < all.size(); i++) {
            Lr1Item item = all.get(i);
            if (!done.contains(item)) {
                NameNode token = item.getDotNode();
                if (token != null) {
                    return item;
                }
                //todo add item to done,
            }
        }
        return null;
    }

    public void closure() {
        if (all.isEmpty()) {
            all.addAll(kernel);
            for (Lr1Item item : kernel) {
                if (item.isDotNonTerminal()) {
                    closure(item.getDotNode(), item);
                }
            }
        }
    }

    void closure(NameNode node, Lr1Item it) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr1Item item = new Lr1Item(decl, 0);
                if (!all.contains(item)) {
                    all.add(item);
                    //la
                    NameNode after = it.getDotNode2();
                    if (after != null) {
                        Set<NameNode> la = first(after);
                        item.lookAhead.addAll(la);
                    }else {
                        item.lookAhead.add(it.lookAhead.get(0));
                    }
                    if (item.isDotNonTerminal()) {
                        closure(item.getDotNode(), item);
                    }
                }
            }

        }
        else {
            throw new RuntimeException("closure error on node: " + node);
        }
    }

    //first terminals of rule
    public Set<NameNode> first(NameNode nameNode) {
        //todo if first has epsilon look next
        Set<NameNode> list = new HashSet<>();
        for (RuleDecl decl : tree.getRules(nameNode.name)) {
            Sequence node = decl.rhs.asSequence();
            handleFirst(node.get(0).asName(), list);

            /*if (node.isSequence()) {
                //NameNode n = (NameNode) node.asSequence().get(0);

            }
            else if (node.isName()) {
                if (node.isEmpty()) {
                    //look after
                    throw new RuntimeException("first epsilon");
                }
                else {
                    handleFirst(node.asName(), list);
                }
            }*/
        }
        return list;
    }

    void handleFirst(NameNode node, Set<NameNode> list) {
        if (node.asName().isToken) {
            list.add(node);
        }
        else {
            //todo prevent left recursion
            list.addAll(first(node));
        }
    }

    //get tokens after the symbol can appear in itemset
    public Set<NameNode> follow(NameNode nameNode, Lr1ItemSet itemSet) {
        Set<NameNode> followSet = new HashSet<>();
        for (Lr1Item item : itemSet.all) {
            Node rhs = item.ruleDecl.rhs;
            if (rhs.isName() && rhs.equals(nameNode)) {
                //rightmost so add $
                //followSet.add(dollar());
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
                            //followSet.add(dollar());
                        }
                    }
                }
            }
        }
        return followSet;
    }

}
