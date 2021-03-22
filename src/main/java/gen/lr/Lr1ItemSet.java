package gen.lr;

import nodes.*;

import java.util.*;


public class Lr1ItemSet extends Lr0ItemSet {

    List<Lr1Item> kernel = new ArrayList<>();
    List<Lr1Item> all = new ArrayList<>();
    Set<Lr1Item> done = new HashSet<>();

    public Lr1ItemSet(List<Lr1Item> kernel, Tree tree) {
        this.kernel.addAll(kernel);
        this.tree = tree;
    }

    public Lr1ItemSet(Lr1Item kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
    }

    public List<Lr1Item> getAll() {
        List<Lr1Item> list = new ArrayList<>();
        for (Lr1Item item : all) {
            if (!done.contains(item)) {
                list.add(item);
            }
        }
        return list;
    }

    @Override
    public String toString() {
        //sort();
        if (all.isEmpty()) {//not processed yet
            return NodeList.join(kernel, "\n");
        }
        return NodeList.join(all, "\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lr1ItemSet that = (Lr1ItemSet) o;
        if (kernel.size() != that.kernel.size()) return false;

        ArrayList<Lr1Item> k1 = new ArrayList<>(kernel);
        k1.removeAll(that.kernel);
        return k1.isEmpty();
    }

    @Override
    public int hashCode() {
        return kernel.hashCode();
    }

    public void closure() {
        if (all.isEmpty()) {
            all.addAll(kernel);
            for (Lr0Item item : kernel) {
                closure((Lr1Item) item);
            }
        }
    }

    public void closure(Lr1Item it) {
        if (it.isDotNonTerminal()) {
            closure(it.getDotNode(), it);
        }
    }

    public void closure(NameNode node, Lr1Item it) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr1Item newItem = new Lr1Item(decl, 0);
                if (!all.contains(newItem)) {
                    all.add(newItem);
                    //lookahead
                    for (Lr1Item k : kernel) {
                        if (k.ruleDecl.name.equals(node.name)) {
                            newItem.lookAhead.addAll(k.lookAhead);
                        }
                    }
                    if (newItem.lookAhead.isEmpty()) {
                        for (Lr1Item i : all) {
                            if (i.getDotNode() != null && i.getDotNode().equals(node) && i.getDotNode2() != null) {
                                newItem.lookAhead.add(i.getDotNode2());
                            }
                        }
                        if (newItem.lookAhead.isEmpty()) {
                            newItem.lookAhead.add(it.lookAhead.get(0));
                        }
                        /*NameNode after = it.getDotNode2();
                        if (after != null) {
                            Set<NameNode> la = first(after);
                            newItem.lookAhead.addAll(la);
                        }
                        else {
                            newItem.lookAhead.add(it.lookAhead.get(0));
                        }*/
                    }

                    if (newItem.isDotNonTerminal() && !newItem.getDotNode().equals(node)) {
                        closure(newItem);
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

}
