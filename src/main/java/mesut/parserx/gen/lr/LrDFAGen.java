package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.gen.lldfa.LLDFAGen;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.gen.transform.LrUtils;
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
    public TreeInfo treeInfo;
    public LrType type;
    LrItem first;
    public LrItemSet acc;
    ConflictResolver conflictResolver;
    Queue<LrItemSet> queue = new LinkedList<>();//itemsets
    public int lastId = -1;
    public List<LrItemSet> itemSets = new ArrayList<>();
    public static boolean debug = false;
    public static Name dollar = new Name("$", true);//eof
    public static String startName = "%start";

    public LrDFAGen(Tree tree, LrType type) {
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
        DotWriter.writeDot(this, dotWriter);
    }

    public void checkAndReport() {
        conflictResolver = new ConflictResolver(this);
        conflictResolver.checkAndReport();
    }

    void prepare() {
        tree.prepare();
        new Normalizer(tree).normalize();
        LrUtils.epsilon(tree);
        LrUtils.plus(tree, true);

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
        addSet(set);
        return set;
    }

    public void addSet(LrItemSet set) {
        set.stateId = ++lastId;
        itemSets.add(set);
    }

    public void generate() {
        prepare();
        writeGrammar();

        var firstSet = makeSet(first);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            var curSet = queue.poll();
            if (debug) System.out.println("set=" + curSet.stateId);
            curSet.closure();
            Map<Name, List<LrItem>> map = new HashMap<>();
            for (var item : curSet.all) {
                if (item.isEpsilon() || item.dotPos == item.rhs.size()) {
                    continue;
                }
                var node = item.getNode(item.dotPos);
                var symbol = ItemSet.sym(node);
                var targetItem = new LrItem(item, item.dotPos + 1);
                map.computeIfAbsent(symbol, s -> new ArrayList<>()).add(targetItem);
                //below without epsilon
//                for (int i = item.dotPos; i < item.rhs.size(); i++) {
//                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
//                    var node = item.getNode(i);
//                    var symbol = ItemSet.sym(node);
//                    var targetItem = new LrItem(item, i + 1);
//                    map.computeIfAbsent(symbol, s -> new ArrayList<>()).add(targetItem);
//                }
            }
            makeTrans(curSet, map);
        }
        acc = getTargetSet(firstSet, tree.start);
    }

    public LrItemSet getTargetSet(LrItemSet from, Name symbol) {
        for (LrTransition tr : from.transitions) {
            if (tr.symbol.equals(symbol)) {
                return tr.target;
            }
        }
        return null;
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
                addSet(targetSet);
                if (!queue.contains(targetSet)) {
                    queue.add(targetSet);
                }
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
        for (var set : itemSets) {
            if (target.size() != set.kernel.size()) continue;
            var l2 = new ArrayList<>(set.kernel);
            sort(l2);
            var same = true;
            for (int i = 0; i < target.size(); i++) {
                var c = type == LrType.LR1 && target.get(i).equals(l2.get(i)) ||
                        type == LrType.LALR1 && target.get(i).isSame(l2.get(i));
                if (!c) {
                    same = false;
                    break;
                }
            }
            if (same) return set;
        }
        return null;
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
            var item = set.all.get(i);
            for (var e : item.getSyms(tree)) {
                var target = getTargetSet(set, ItemSet.sym(e.getKey()));
                if (target == null) continue;
                var newItem = new LrItem(item, e.getValue() + 1);
                for (var kernel : target.kernel) {
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
    }


    public void genGoto() {
        for (var set : itemSets) {
            for (var item : set.all) {
                if (item.dotPos != 0 || item.isReduce(tree)) continue;
                //walk to reduce state of item and set goto

                var curItem = item;
                var any = true;
                while (any) {
                    any = false;
                    //transitions of item
                    for (var next : curItem.next) {
                        if (next.isReduce(tree)) {
                            //set goto
                            next.gotoSet.add(set);
                            if (debug) System.out.println("set goto");
                        }
                        else {
                            curItem = next;
                            any = true;
                        }
                    }
                }
            }
        }
    }

}
