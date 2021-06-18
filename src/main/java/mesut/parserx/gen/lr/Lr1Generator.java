package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.Tree;

import java.util.*;


public class Lr1Generator extends LRGen<Lr1ItemSet> {

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    @Override
    void prepare() {
        super.prepare();
        first.lookAhead.add(dollar);
    }

    @Override
    public Lr1ItemSet makeSet(LrItem item) {
        return new Lr1ItemSet(item, tree);
    }


    void sort(List<LrItem> list) {
        Collections.sort(list, new Comparator<LrItem>() {
            @Override
            public int compare(LrItem o1, LrItem o2) {
                int c = Integer.compare(o1.dotPos, o2.dotPos);
                if (c == 0) {
                    return o1.rule.equals(o2.rule) ? 0 : -1;
                }
                return c;
            }
        });
    }

    boolean isSameKernel(List<LrItem> k1, List<LrItem> k2) {
        if (k1.size() != k2.size()) {
            return false;
        }
        sort(k1);
        sort(k2);
        for (int i = 0; i < k1.size(); i++) {
            LrItem i1 = k1.get(i);
            LrItem i2 = k2.get(i);
            if (!i1.isSame(i2)) {//same without la
                return false;
            }
        }
        return true;
    }

    //lalr merger
    //merge sets that have same kernel
    public void merge() {
        System.out.println("before merging " + table.itemSets.size());
        LrDFA<Lr1ItemSet> res = new LrDFA<>();
        //first pass will merge states
        Set<LrItemSet> done = new HashSet<>();
        for (Lr1ItemSet from : table.itemSets) {
            if (done.contains(from)) continue;
            if (res.getId(from) == -1) {
                res.addId(from);
            }
            for (LrItemSet other : table.itemSets) {
                if (from == other) continue;
                List<LrItem> k1 = new ArrayList<>(from.kernel);
                List<LrItem> k2 = new ArrayList<>(other.kernel);
                if (isSameKernel(k1, k2)) {
                    done.add(other);
                    System.out.println("merging " + table.getId(from) + " with " + table.getId(other));
                    if (res.getId(other) == -1) {
                        //merge id if not done yet
                        res.setId(other, res.getId(from));
                    }
                    //merge la
                    for (int i = 0; i < k1.size(); i++) {
                        LrItem i1 = k1.get(i);
                        LrItem i2 = k2.get(i);
                        Set<NameNode> set = new HashSet<>(i1.lookAhead);
                        set.addAll(i2.lookAhead);
                        i1.lookAhead = set;
                        i2.lookAhead = set;
                    }
                    break;
                }
            }
        }
        //second pass will make transitions with new ids
        for (Lr1ItemSet from : table.itemSets) {
            for (LrTransition<?> tr : table.getTrans(from)) {
                boolean merged = false;
                //omit merged's transition
                for (LrTransition<?> old : res.getTrans(from)) {
                    if (old.symbol.equals(tr.symbol)) {
                        //merged transition
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    res.addTransition(from, (Lr1ItemSet) tr.to, tr.symbol);
                }
            }
        }
        table = res;
        System.out.println("after merging " + table.itemSets.size());
    }


}
