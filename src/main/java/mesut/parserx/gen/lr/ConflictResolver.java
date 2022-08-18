package mesut.parserx.gen.lr;

import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConflictResolver {

    LrDFAGen gen;
    Tree tree;
    public List<ConflictInfo> conflicts = new ArrayList<>();
    boolean isResolved;//is conflicts checked

    public static class ConflictInfo {
        public boolean rr;
        public LrItem shift;
        public LrItem reduce;
        public LrItem reduce2;
        public Name sym;
        int state;
    }

    public ConflictResolver(LrDFAGen gen) {
        this.gen = gen;
        tree = gen.tree;
    }


    public void checkAndReport() {
        checkAll();
        report();
    }

    public void checkAll() {
        isResolved = false;
        for (LrItemSet set : gen.table.itemSets) {
            check(set);
        }
        if (isResolved) {
            checkAll();
        }
    }

    private void report() {
        if (conflicts.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (ConflictInfo info : conflicts) {
            if (info.rr) {
                sb.append("reduce/reduce conflict in ").append(info.state).append("\n");
            }
            else {
                sb.append(String.format("shift/reduce conflict in %d sym=%s", info.state, info.sym)).append("\n");
            }
        }
        throw new RuntimeException(sb.toString());

    }

    //check if two items has conflict
    void check(LrItemSet set) {
        for (int i = 0; i < set.all.size(); i++) {
            LrItem i1 = set.all.get(i);
            for (int j = i + 1; j < set.all.size(); j++) {
                LrItem i2 = set.all.get(j);
                if (i1.isReduce(tree) && i2.isReduce(tree)) {
                    //if any lookahead conflict
                    HashSet<Name> la = new HashSet<>(i1.lookAhead);
                    la.retainAll(i2.lookAhead);
                    if (!la.isEmpty()) {
                        ConflictInfo info = new ConflictInfo();
                        info.rr = true;
                        info.state = set.stateId;
                        info.reduce = i1;
                        info.reduce2 = i2;
                        conflicts.add(info);
                    }
                }
                else {
                    if (i2.isReduce(tree)) {
                        for (var e1 : i1.getSyms(tree)) {
                            var sym = ItemSet.sym(e1.getKey());
                            if (i2.lookAhead.contains(sym)) {
                                handleSR(i1, sym, i2, set);
                            }
                        }
                    }
                    if (i1.isReduce(tree)) {
                        for (var e2 : i2.getSyms(tree)) {
                            var sym = ItemSet.sym(e2.getKey());
                            if (i1.lookAhead.contains(sym)) {
                                handleSR(i2, sym, i1, set);
                            }
                        }
                    }
                }
            }
        }
    }

    void handleSR(LrItem shift, Name sym, LrItem reduce, LrItemSet set) {
        boolean removed = false;
        //if same rule,check assoc
        if (shift.rule.equals(reduce.rule)) {
            boolean l = shift.rule.rhs.asSequence().assocLeft;
            boolean r = shift.rule.rhs.asSequence().assocRight;
            if (l) {
                //keep reduce,remove shift
                removeItem(set, shift);
                removed = true;
            }
            else if (r) {
                //keep shift,remove reduce
                reduce.lookAhead.remove(sym);
                if (reduce.lookAhead.isEmpty()) {
                    removeItem(set, reduce);
                }
                removed = true;
            }
            //no assoc
            //prefer shift
            if (removed) {
                if (LrDFAGen.debug) System.out.println("assoc is used on " + set.stateId + " sym=" + sym);
                this.isResolved = true;
            }
        }
        else {
            //check prec
            if (shift.rule.ref.equals(reduce.rule.ref)) {
                if (reduce.rule.index < shift.rule.index) {
                    //prefer reduce
                    removeItem(set, shift);
                    removed = true;
                }
                else {
                    //prefer shift
                    reduce.lookAhead.remove(sym);
                    if (reduce.lookAhead.isEmpty()) {
                        removeItem(set, reduce);
                    }
                    removed = true;
                }
            }
            if (removed) {
                if (LrDFAGen.debug) System.out.println("prec used in " + set.stateId + " sym=" + sym);
                this.isResolved = true;
            }
        }
        if (!removed) {
            ConflictInfo info = new ConflictInfo();
            info.rr = false;
            info.state = set.stateId;
            info.shift = shift;
            info.reduce = reduce;
            info.sym = sym;
            conflicts.add(info);
        }
    }


    void removeItem(LrItemSet set, LrItem item) {
        //remove incoming and outgoing transitions
        List<LrTransition> out = new ArrayList<>();
        for (LrTransition tr : set.transitions) {
            for (var e : item.getSyms(tree)) {
                var sym = ItemSet.sym(e.getKey());
                if (sym.equals(tr.symbol)) {
                    out.add(tr);
                }
            }
        }
        if (out.size() == 1) {
            //remove
            set.transitions.remove(out.get(0));
        }
        List<LrTransition> in = new ArrayList<>();
        for (LrItemSet from : gen.table.itemSets) {
            for (LrTransition tr : from.transitions) {
                if (tr.target == set) {
                    LrItem prev = new LrItem(item, item.dotPos - 1);
                    for (LrItem fromItem : from.all) {
                        if (fromItem.isSame(prev)) {
                            from.all.remove(fromItem);
                            //in.add(tr);
                            break;
                        }
                    }
                }
            }
        }
        set.all.remove(item);
    }
}
