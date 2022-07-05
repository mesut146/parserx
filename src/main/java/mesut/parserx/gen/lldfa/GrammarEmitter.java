package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.nodes.*;

import java.util.*;

public class GrammarEmitter {
    LLDfaBuilder builder;
    Set<ItemSet> all;
    Tree tree;
    Set<ItemSet> inlined = new HashSet<>();

    public GrammarEmitter(LLDfaBuilder builder) {
        this.builder = builder;
        this.tree = builder.tree;
    }

    public void emit() {
        for (var e : builder.rules.entrySet()) {
            if (!e.getKey().equals("E")) continue;
            all = e.getValue();
            moveReductions();
            //mergeFinals();
            //eliminate();
            e.setValue(all);
        }
        //File file = new File(tree.options.outDir, Utils.newName(tree.file.getName(), "-emit.g"));
        //builder.dot(null);
    }

    public void moveReductions() {
        Queue<ItemSet> q = new LinkedList<>(all);
        while (!q.isEmpty()) {
            Set<Item> toRemove = new HashSet<>();
            ItemSet set = q.poll();
            //clear sub reductions, they only needed for reducers
            for (Iterator<Item> it = set.all.iterator(); it.hasNext(); ) {
                Item item = it.next();
                if (!item.isReduce(tree)) continue;
                if (item.lookAhead.contains(LrDFAGen.dollar)) continue;
                if (!item.reduceParent.isEmpty()) {
                    it.remove();
                    System.out.println("rem sub = " + item);
                }
            }
            //move parent reductions
            for (Item it : set.all) {
                if (!it.isReduce(tree)) continue;
                if (it.lookAhead.contains(LrDFAGen.dollar)) continue;
                moveReduction(set, it, q);
                if (it.lookAhead.isEmpty()) toRemove.add(it);
            }
            for (Item it : toRemove) {
                for (int i = 0; i < set.all.size(); i++) {
                    if (it.isSame(set.all.get(i))) {
                        set.all.remove(i);
                        set.kernel.remove(it);
                        System.out.println("deleted " + set.stateId + " " + it);
                        break;
                    }
                }
            }

        }
    }

    public List<Item> findParents(Item it) {
        List<Item> parents = new ArrayList<>();
        if (it.reduceParent.isEmpty()) {
            parents.add(it);
            return parents;
        }
        for (Item p : it.reduceParent) {
            parents.addAll(findParents(p));
        }
        return parents;
    }

    //move reduction into transition symbol
    public void moveReduction(ItemSet set, Item it, Queue<ItemSet> q) {
        //System.out.println("trace "+it);
        for (Transition tr : set.transitions) {
            if (tr.target == set) continue;
            //if(!tr.symbol.isName()) continue;
            //System.out.println("sym "+tr.symbol.debug());
            //copy for each symbol
            //Item cur = new Item(it, it.dotPos);
            if (tr.symbol.astInfo.isFactor) {
                ItemSet target = tr.target;
                System.out.printf("moved %d -> %d %s\n", set.stateId, target.stateId, it);
                Item it2 = new Item(it, it.dotPos);
                it2.senders.addAll(it.senders);
                it.lookAhead.remove(tr.symbol.asName());
                it2.lookAhead.clear();
                for (Transition tr2 : target.transitions) {
                    //todo not all
                    Name sym = tr2.symbol.isSequence() ? tr2.symbol.asSequence().last().asName() : tr2.symbol.asName();
                    it2.lookAhead.add(sym);
                }
                target.addItem(it2);
                if (!q.contains(target)) q.add(target);
                System.out.printf("new = %s\n", it2);
            }
            else {
                Name sym = tr.symbol.isSequence() ? tr.symbol.asSequence().last().asName() : tr.symbol.asName();
                it.lookAhead.remove(sym);
                Name s = it.rule.ref.copy();
                s.name += "()";
                tr.symbol = seq(s, tr.symbol);
            }
        }
    }

    Sequence seq(Node a, Node b) {
        if (b.isSequence()) {
            Sequence s = b.asSequence();
            s.list.add(0, a);
            return s;
        }
        else {
            return new Sequence(a, b);
        }
    }

    public void eliminate() {
        mergeFinals();
        List<ItemSet> toRemove = new ArrayList<>();
        while (all.size() > 2) {
            for (ItemSet set : all) {
                if (set.isStart) continue;
                if (set.stateId == 4) continue;
                if (canBeRemoved2(set)) {
                    eliminate(set);
                    toRemove.add(set);
                }
            }
            if (toRemove.isEmpty()) break;
            toRemove.forEach(all::remove);
            toRemove.clear();
        }
    }

    public boolean canBeRemoved2(ItemSet set) {
        System.out.println("canBeRemoved2 " + set.stateId);
        if (countOutgoings(set) != 1) return false;

        if (hasFinal(set)) return false;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for (Transition tr : set.transitions) {
            if (tr.target != set) {
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            boolean r = reachFinal(cur, set);
            for (Transition tr : cur.transitions) {
                if (tr.target == set && r) return false;
                if (tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }

    public boolean reachFinal(ItemSet from, ItemSet except) {
        //System.out.printf("reachFinal %d -> %d\n", from.stateId, except.stateId);
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.stateId);
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            if (hasFinal(cur)) return true;
            for (Transition tr : cur.transitions) {
                ItemSet trg = tr.target;
                if (trg == except) continue;
                if (visited.add(trg.stateId)) queue.add(trg);
            }
        }
        return false;
    }

    public boolean hasFinal(ItemSet set) {
        for (Item it : set.all) {
            if (it.isReduce(tree) && it.lookAhead.contains(LrDFAGen.dollar)) return true;
        }
        return false;
    }

    public void eliminate(ItemSet set) {
        System.out.println("eliminate " + set.stateId);
        Node loop = null;
        Transition out = null;
        for (Transition tr : set.transitions) {
            if (tr.target == set) loop = tr.symbol;
            else out = tr;
        }
        for (Transition in : set.incomings) {
            in.target = out.target;
            out.target.incomings.remove(out);
            if (loop == null) {
                in.symbol = new Sequence(wrapOr(in.symbol), wrapOr(out.symbol));
            }
            else {
                in.symbol = new Sequence(wrapOr(in.symbol), new Regex(new Group(wrapOr(loop)), RegexType.STAR), wrapOr(out.symbol));
            }
        }
        combine();
    }


    Node wrapOr(Node sym) {
        if (sym.isOr()) return new Group(sym);
        return sym;
    }

    Node loopSym(ItemSet set) {
        for (Transition tr : set.transitions) {
            if (tr.target == set) return tr.symbol;
        }
        return null;
    }

    void mergeFinals() {
        ItemSet ns = new ItemSet(tree, builder.type);
        for (Iterator<ItemSet> it = all.iterator(); it.hasNext(); ) {
            ItemSet set = it.next();
            for (Item item : set.all) {
                if (item.lookAhead.contains(LrDFAGen.dollar) && item.isReduce(tree)) {
                    ns.addAll(set.all);
                    for (Transition in : set.incomings) {
                        in.target = ns;
                        System.out.printf("new1 %d -> %d\n", in.from.stateId, ns.stateId);
                    }
                    for (Transition tr : set.transitions) {
                        tr.from = ns;
                        ns.addTransition(tr.symbol, tr.target);
                        System.out.printf("new2 %d -> %d\n", ns.stateId, tr.target.stateId);
                    }
                    it.remove();
                    System.out.printf("final %d\n", set.stateId);
                    break;
                }
            }
        }
        combine();
        System.out.printf("new final = %d\n", ns.stateId);
        all.add(ns);
    }

    void combine() {
        for (ItemSet set : all) {
            //target -> ors
            Map<ItemSet, List<Node>> map = new HashMap<>();
            for (Transition tr : set.transitions) {
                List<Node> list = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                list.add(tr.symbol);
                tr.target.incomings.remove(tr);
            }
            set.transitions.clear();
            for (ItemSet trg : map.keySet()) {
                List<Node> list = map.get(trg);
                Node sym = list.size() == 1 ? list.get(0) : new Or(list);
                set.addTransition(sym, trg);
            }
        }
    }

    public void findInlined() {
        for (ItemSet set : all) {
            if (canBeInlined(set)) {
                inlined.add(set);
            }
        }
    }

    public boolean canBeInlined(ItemSet set) {
        if (countOutgoings(set) == 1) return true;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for (Transition tr : set.transitions) {
            if (tr.target != set) {
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            boolean r = reachFinal(cur, set);
            for (Transition tr : cur.transitions) {
                if (tr.target == set && r) return false;
                if (tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }


    public static int countOutgoings(ItemSet set) {
        int cnt = 0;
        for (Transition tr : set.transitions) {
            if (tr.target.stateId != set.stateId) cnt++;
        }
        return cnt;
    }


}
