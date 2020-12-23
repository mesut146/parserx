package gen.lr;

import nodes.*;

import java.util.*;


public class Lr1ItemSet extends Lr0ItemSet<Lr1Item> {

    public Lr1ItemSet(List<Lr1Item> kernel, Tree tree) {
        this.kernel.addAll(kernel);
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
            for (LrItem item : kernel) {
                if (item.isDotNonTerminal()) {
                    closure(item.getDotNode(), (Lr1Item) item);
                }
            }
        }
    }

    public void closure(NameNode node, Lr1Item it) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr1Item item = new Lr1Item(decl, 0);
                if (!all.contains(item)) {
                    all.add(item);
                    //lookahead
                    for (Lr1Item k : kernel) {
                        if (k.ruleDecl.name.equals(node.name)) {
                            item.lookAhead.addAll(k.lookAhead);
                        }
                    }
                    if (item.lookAhead.isEmpty()) {
                        NameNode after = it.getDotNode2();
                        if (after != null) {
                            Set<NameNode> la = first(after);
                            item.lookAhead.addAll(la);
                        }
                        else {
                            item.lookAhead.add(it.lookAhead.get(0));
                        }
                    }

                    if (item.isDotNonTerminal() && !item.getDotNode().equals(node)) {
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
