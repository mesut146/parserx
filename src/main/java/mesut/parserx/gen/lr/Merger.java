package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

import java.util.*;

public class Merger {
    LrDFA<Lr1ItemSet> table;
    LrDFA<Lr1ItemSet> res = new LrDFA<>();
    Set<Integer> done = new HashSet<>();
    //state -> merged states
    HashMap<Integer, TreeSet<Integer>> merged = new HashMap<>();
    //old state -> new state
    HashMap<Integer, Integer> newStates = new HashMap<>();

    public Merger(LrDFA<Lr1ItemSet> table) {
        this.table = table;
    }


    //lalr merger
    //merge sets that have same kernel
    public LrDFA<Lr1ItemSet> merge() {
        System.out.println("before merging " + table.itemSets.size());
        //first pass will merge states
        for (int i = 0; i < table.itemSets.size(); i++) {
            Lr1ItemSet from = table.itemSets.get(i);
            if (done.contains(from.stateId)) continue;
            Integer newId = newStates.get(from.stateId);
            if (newId == null) {
                newId = from.stateId;
            }
            if (!res.exist(from)) {
                res.addSet(from);
            }
            for (int j = i + 1; j < table.itemSets.size(); j++) {
                LrItemSet other = table.itemSets.get(j);
                if (from == other) continue;
                List<LrItem> k1 = new ArrayList<>(from.kernel);
                List<LrItem> k2 = new ArrayList<>(other.kernel);
                if (!isSameKernel(k1, k2)) continue;
                done.add(other.stateId);
                TreeSet<Integer> treeSet = merged.get(from.stateId);
                /*if () {

                }*/
                System.out.printf("merging %d with %d\n", table.getId(from), table.getId(other));
                if (!res.exist(other)) {
                    //merge id if not done yet
                    res.setId(other, res.getId(from));
                }
                //merge la
                for (int k = 0; k < k1.size(); k++) {
                    LrItem i1 = k1.get(k);
                    LrItem i2 = k2.get(k);
                    Set<Name> set = new HashSet<>(i1.lookAhead);
                    set.addAll(i2.lookAhead);
                    i1.lookAhead = set;
                    i2.lookAhead = set;
                    //goto sets
                    //i1.gotoSet.addAll(i2.gotoSet);
                }
                break;
            }
        }
        //second pass will make transitions with new ids
        for (Lr1ItemSet from : table.itemSets) {
            for (LrTransition<?> tr : table.getTrans(from)) {
                boolean ismerged = false;
                //omit merged's transition
                for (LrTransition<?> old : res.getTrans(from)) {
                    if (old.symbol.equals(tr.symbol)) {
                        //merged transition
                        ismerged = true;
                        break;
                    }
                }
                if (!ismerged) {
                    res.addTransition(from, (Lr1ItemSet) tr.to, tr.symbol);
                }
            }
        }
        System.out.println("after merging " + res.itemSets.size());
        return res;
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
}
