package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//lr table generator
public class LrDFAGen {
    public Tree tree;
    public RuleDecl start;
    public LrDFA table = new LrDFA();
    public TreeInfo treeInfo;
    public String type;
    public List<ConflictInfo> conflicts = new ArrayList<>();
    LrItem first;
    boolean isResolved;//is conflicts checked
    Queue<LrItemSet> queue = new LinkedList<>();//itemsets
    public static boolean debug = false;
    public static Name dollar = new Name("$", true);//eof
    public static String startName = "%start";

    public LrDFAGen(Tree tree, String type) {
        this.tree = tree;
        this.type = type;
    }

    public void makeStart() {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is declared");
        }
        start = new RuleDecl(startName, new Sequence(tree.start));
        //tree.addRule(start);
    }

    public void writeTableDot(PrintWriter writer) {
        DotWriter.table(writer, this, false);
    }

    public void writeDot(PrintWriter dotWriter) {
        DotWriter.writeDot(table, dotWriter);
    }

    void prepare() {
        tree.prepare();
        new Normalizer(tree).normalize();

        for (var rd : tree.rules) {
            if (rd.rhs.isOr()) {
                List<Node> list = new ArrayList<>();
                for (var ch : rd.rhs.asOr()) {
                    list.add(LLDfaBuilder.expandPlus(ch));
                }
                rd.rhs = new Or(list);
            }
            else {
                rd.rhs = LLDfaBuilder.expandPlus(rd.rhs);
            }
        }

        makeStart();
        treeInfo = TreeInfo.make(tree);

        first = new LrItem(start, 0);
        first.lookAhead.add(dollar);
    }

    public void writeGrammar() {
        String out = tree.options.outDir == null ? tree.file.getParent() : tree.options.outDir;
        writeGrammar(new File(out, Utils.newName(tree.file.getName(), "-final.g")));
    }

    public void writeGrammar(File file) {
        try {
            RuleDecl.printIndex = true;
            Utils.write(tree.toString(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LrItemSet makeSet(LrItem item) {
        LrItemSet set = new LrItemSet(treeInfo, type);
        set.addItem(item);
        table.addSet(set);
        return set;
    }

    public void generate() {
        prepare();
        writeGrammar();

        LrItemSet firstSet = makeSet(first);
        table.first = firstSet;
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            LrItemSet curSet = queue.poll();
            if (debug) System.out.println("set=" + curSet.stateId);
            curSet.closure();
            Map<Name, List<LrItem>> map = new HashMap<>();
            //iterate items
            for (var item : curSet.all) {
                for (int i = item.dotPos; i < item.rhs.size(); i++) {
                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    Node node = item.getNode(i);
                    Name symbol = ItemSet.sym(node);

                    var targetItem = new LrItem(item, i + 1);
                    map.computeIfAbsent(symbol, s -> new ArrayList<>()).add(targetItem);
                }
            }
            makeTrans(curSet, map);
        }
        table.acc = table.getTargetSet(firstSet, tree.start);
    }

    void makeTrans(LrItemSet curSet, Map<Name, List<LrItem>> map) {
        for (var e : map.entrySet()) {
            var sym = e.getKey();
            var list = e.getValue();
            LrItemSet targetSet;

            var sim = findSimilar(new ArrayList<>(list));
            if (sim != null) {
                //merge lookaheads
                for (var it : list) {
                    sim.update(it);
                }
                targetSet = sim;
            }
            else {
                targetSet = new LrItemSet(treeInfo, type);
                targetSet.addAll(list);
                table.addSet(targetSet);
                addQueue(targetSet);
            }
            curSet.addTransition(sym, targetSet);
        }
    }


    void sort(List<LrItem> list) {
        list.sort((i1, i2) -> {
            if (i1.rule.index == i2.rule.index) {
                return Integer.compare(i1.dotPos, i2.dotPos);
            }
            return Integer.compare(i1.rule.index, i2.rule.index);
        });
    }

    LrItemSet findSimilar(List<LrItem> target) {
        sort(target);
        for (var set : table.itemSets) {
            if (target.size() != set.kernel.size()) continue;
            var l2 = new ArrayList<>(set.kernel);
            sort(l2);
            var same = true;
            for (int i = 0; i < target.size(); i++) {
                if (!target.get(i).isSame(l2.get(i))) {
                    same = false;
                    break;
                }
            }
            if (same) return set;
        }
        return null;
    }

    void addQueue(LrItemSet set) {
        if (!queue.contains(set)) {
            queue.add(set);
        }
    }

    void doMerge(LrItem item, LrItemSet set) {
        if (debug) System.out.println("lalr merged " + set.stateId);
        for (LrItem old : set.all) {
            if (old.isSame(item) && !old.equals(item)) {
                //if my la is subset of old one don't do anything
                Set<Name> la = new HashSet<>(old.lookAhead);
                la.retainAll(item.lookAhead);
                if (la.size() == item.lookAhead.size()) {
                    //no need to
                }
                else {
                    old.lookAhead.addAll(item.lookAhead);
                    set.all.clear();
                    set.closure();
                    doTrans(set, new HashSet<LrItemSet>());
                }
                break;
            }
        }
    }

    //trace transitions and merge la
    void doTrans(LrItemSet set, Set<LrItemSet> done) {
        if (done.contains(set)) return;
        done.add(set);
        for (int i = 0; i < set.all.size(); i++) {
            LrItem item = set.all.get(i);
            if (item.getDotSym() == null) continue;
            LrItemSet target = table.getTargetSet(set, item.getDotSym());
            if (target == null) continue;
            LrItem newItem = new LrItem(item, item.dotPos + 1);
            for (LrItem kernel : target.kernel) {
                if (kernel.isSame(newItem)) {
                    kernel.lookAhead.addAll(item.lookAhead);
                    target.all.clear();
                    target.closure();
                    doTrans(target, done);
                    break;
                }
            }
            //doMerge(newItem, target);
        }
    }

    public void checkAndReport() {
        checkAll();
        report();
    }

    public void checkAll() {
        isResolved = false;
        for (LrItemSet set : table.itemSets) {
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
                sb.append(String.format("shift/reduce conflict in %d sym=%s", info.state, info.shift.getDotSym())).append("\n");
            }
        }
        throw new RuntimeException(sb.toString());

    }

    //check if two item has conflict
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
                    LrItem shift;
                    LrItem reduce;
                    if (i1.isReduce(tree) && !i2.isReduce(tree) && (i1.lookAhead.contains(i2.getDotSym()))) {
                        shift = i2;
                        reduce = i1;
                    }
                    else if (!i1.isReduce(tree) && i2.isReduce(tree) && i2.lookAhead.contains(i1.getDotSym())) {
                        shift = i1;
                        reduce = i2;
                    }
                    else {
                        continue;
                    }
                    boolean removed = false;
                    //if same rule,check assoc
                    if (shift.rule.equals(reduce.rule)) {//todo isSame?
                        LrItemSet target = table.getTargetSet(set, shift.getDotSym());
                        LrItem newItem = new LrItem(shift, shift.dotPos + 1);
                        for (LrItem targetItem : target.all) {
                            if (targetItem.isSame(newItem)) {
                                boolean l = shift.rule.rhs.asSequence().assocLeft;
                                boolean r = shift.rule.rhs.asSequence().assocRight;
                                if (l) {
                                    //keep reduce,remove shift
                                    removeItem(set, shift);
                                    removed = true;
                                }
                                else if (r) {
                                    //keep shift,remove reduce
                                    reduce.lookAhead.remove(shift.getDotSym());
                                    if (reduce.lookAhead.isEmpty()) {
                                        removeItem(set, reduce);
                                    }
                                    removed = true;
                                }
                                else {
                                    //no assoc
                                    //prefer shift
                                }
                                break;
                            }
                        }
                        if (removed) {
                            if (debug) System.out.println("assoc is used on " + set.stateId);
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
                                reduce.lookAhead.remove(shift.getDotSym());
                                if (reduce.lookAhead.isEmpty()) {
                                    removeItem(set, reduce);
                                }
                                removed = true;
                            }
                        }
                        if (removed) {
                            if (debug) System.out.println("prec used in " + set.stateId);
                            this.isResolved = true;
                        }
                    }
                    if (!removed) {
                        ConflictInfo info = new ConflictInfo();
                        info.rr = false;
                        info.state = set.stateId;
                        info.shift = shift;
                        info.reduce = reduce;
                        conflicts.add(info);
                    }
                }
            }
        }
    }


    void removeItem(LrItemSet set, LrItem item) {
        //remove incoming and outgoing transitions
        List<LrTransition> out = new ArrayList<>();
        for (LrTransition tr : set.transitions) {
            if (tr.symbol.equals(item.getDotSym())) {
                out.add(tr);
            }
        }
        if (out.size() == 1) {
            //remove
            set.transitions.remove(out.get(0));
        }
        List<LrTransition> in = new ArrayList<>();
        for (LrItemSet from : table.itemSets) {
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

    public void genGoto() {
        for (LrItemSet set : table.itemSets) {
            for (LrItem item : set.all) {
                if (item.dotPos != 0 || item.isReduce(tree)) continue;
                //walk to reduce state of item and set goto
                LrItemSet curSet = set;
                LrItem curItem = item;
                while (true) {
                    curSet = table.getTargetSet(curSet, curItem.getDotSym());
                    LrItem tmpItem = new LrItem(curItem.rule, curItem.dotPos + 1);
                    //use tmp to get original item
                    for (LrItem tmp : curSet.all) {
                        if (tmp.isSame(tmpItem)) {
                            curItem = tmp;
                            break;
                        }
                    }
                    if (curItem.isReduce(tree)) {
                        //set goto
                        curItem.gotoSet.add(set);
                        if (debug) System.out.println("set goto");
                        break;
                    }
                }
            }
        }
    }

    static class ConflictInfo {
        public boolean rr;
        public LrItem shift;
        public LrItem reduce;
        public LrItem reduce2;
        int state;

    }
}
