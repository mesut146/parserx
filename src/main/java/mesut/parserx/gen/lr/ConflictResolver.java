package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        @Override
        public String toString() {
            if (rr) {
                return String.format("reduce/reduce conflict in %s sym=%s\nitem1=%s\nitem1=%s\n", state, sym, shift, reduce);
            }
            else {
                return String.format("shift/reduce conflict in %s sym=%s\nitem1=%s\nitem2=%s\n", state, sym, shift, reduce);
            }
        }
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
        do {
            isResolved = false;
            for (var set : gen.itemSets) {
                check(set);
            }
        } while (isResolved);
    }

    private void report() {
        if (conflicts.isEmpty()) return;
        var sb = new StringBuilder();
        for (var info : conflicts) {
            sb.append(info.toString());
        }
        //throw new RuntimeException(sb.toString());
        System.err.println(sb);
    }

    //check if two items has conflict
    void check(LrItemSet set) {
        boolean any = false;
        for (int i = 0; i < set.all.size(); i++) {
            var i1 = set.all.get(i);
            for (int j = i + 1; j < set.all.size(); j++) {
                var i2 = set.all.get(j);
                if (i1.isReduce(tree) && i2.isReduce(tree)) {
                    //if any lookahead conflict
                    if (hasCommon(i1.lookAhead, i2.lookAhead)) {
                        var info = new ConflictInfo();
                        info.rr = true;
                        info.state = set.stateId;
                        info.reduce = i1;
                        info.reduce2 = i2;
                        conflicts.add(info);
                    }
                }
                else {
                    if (i2.isReduce(tree)) {
                        any = checkSR(i1, i2, set);
                    }
                    else if (i1.isReduce(tree)) {
                        any = checkSR(i2, i1, set);
                    }
                    if (any) {
                        //indexes are changed restart
                        check(set);
                        return;
                    }
                }
            }
        }
    }

    boolean checkSR(LrItem shift, LrItem reduce, LrItemSet set) {
        var removed = false;
        for (var e1 : shift.getSyms(tree)) {
            var sym = ItemSet.sym(e1.getKey());
            if (reduce.lookAhead.contains(sym)) {
                removed |= handleSR(shift, sym, reduce, set);
            }
            else if (sym.isRule()) {
                //expansion may conflict
                var firstSet = FirstSet.firstSet(sym, tree);
                if (hasCommon(firstSet, reduce.lookAhead)) {
                    removed |= handleSR(shift, sym, reduce, set);
                }
            }
        }
        return removed;
    }

    boolean hasCommon(Set<Name> s1, Set<Name> s2) {
        var newSet = new HashSet<>(s1);
        newSet.retainAll(s2);
        return !newSet.isEmpty();
    }

    boolean handleSR(LrItem shift, Name sym, LrItem reduce, LrItemSet set) {
        boolean removed = false;
        //if same rule,check assoc
        if (shift.rule.equals(reduce.rule)) {
            removed = tryAssoc(shift, reduce, sym, set);
        }
        else {
            removed = tryPrec(shift, reduce, sym, set);
        }
        if (!removed) {
            var info = new ConflictInfo();
            info.rr = false;
            info.state = set.stateId;
            info.shift = shift;
            info.reduce = reduce;
            info.sym = sym;
            conflicts.add(info);
        }
        return removed;
    }

    boolean tryAssoc(LrItem shift, LrItem reduce, Name sym, LrItemSet set) {
        boolean removed = false;
        if (shift.rule.rhs.asSequence().assocLeft) {
            //keep reduce,remove shift
            removeItem(set, shift);
            //closure too
            if (sym.isRule()) {
                List<LrItem> toRemove = set.all
                        .stream()
                        .filter(it -> it.rule.ref.equals(sym) && it.dotPos == 0)
                        .collect(Collectors.toList());

                for (var item : toRemove) {
                    removeItem(set, item);
                }
            }
            removed = true;
        }
        else if (shift.rule.rhs.asSequence().assocRight) {
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
            if (LrDFAGen.debug) {
                System.out.printf("assoc is used on %s sym=%s\nitem1=%s\nitem2=%s\n", set.stateId, sym, shift, reduce);
            }
            this.isResolved = true;
        }
        return removed;
    }

    private boolean tryPrec(LrItem shift, LrItem reduce, Name sym, LrItemSet set) {
        if (!shift.rule.ref.equals(reduce.rule.ref)) return false;
        //check prec
        int prefer1;
        if (reduce.rule.index < shift.rule.index) {
            //prefer reduce
            removeItem(set, shift);
            //also remove closure
            if (sym.isRule()) {
                List<LrItem> toRemove = set.all
                        .stream()
                        .filter(it -> it.rule.ref.equals(sym) && it.dotPos == 0)
                        .collect(Collectors.toList());

                for (var item : toRemove) {
                    removeItem(set, item);
                }
            }
            prefer1 = 2;
        }
        else {
            //prefer shift
            //removeItem(set, reduce);
            if (sym.isRule()) {
                for (var la : FirstSet.tokens(sym, tree)) {
                    reduce.lookAhead.remove(la);
                }
                if (reduce.lookAhead.isEmpty()) {
                    removeItem(set, reduce);
                }
            }
            else {
                //removeItem(set, reduce);
                reduce.lookAhead.remove(sym);
                if (reduce.lookAhead.isEmpty()) {
                    removeItem(set, reduce);
                }
            }
            prefer1 = 1;
        }

        if (LrDFAGen.debug) {
            System.out.printf("prec is used on %s sym=%s preferred item%d\nitem1=%s\nitem2=%s\n", set.stateId, sym, prefer1, shift, reduce);
        }
        this.isResolved = true;
        return true;
    }


    void removeItem(LrItemSet set, LrItem item) {
        //remove outgoing transitions
        List<LrTransition> out = new ArrayList<>();
        for (var tr : set.transitions) {
            for (var e : item.getSyms(tree)) {
                var sym = ItemSet.sym(e.getKey());
                if (sym.equals(tr.symbol)) {
                    out.add(tr);
                }
            }
        }
        if (out.size() == 1) {
            //remove
            //set.transitions.remove(out.get(0));
        }
//        for (var from : gen.table.itemSets) {
//            for (var tr : from.transitions) {
//                if (tr.target == set) {
//                    var prev = new LrItem(item, item.dotPos - 1);
//                    for (var fromItem : from.all) {
//                        if (fromItem.isSame(prev)) {
//                            from.all.remove(fromItem);
//                            //in.add(tr);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
        set.all.remove(item);
    }
}
