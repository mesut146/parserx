package mesut.parserx.gen.lr;

import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//lr table generator
public class LrDFAGen {
    public static boolean debug = false;
    public static Name dollar = new Name("$", true);//eof
    public static String startName = "%start";
    private final boolean merge;//lalr
    public int acc = 1;
    public LrDFA table = new LrDFA();
    public RuleDecl start;
    public Tree tree;
    public String type;
    LrItem first;
    boolean isResolved;//is conflicts checked
    Queue<LrItemSet> queue = new LinkedList<>();//itemsets

    public LrDFAGen(Tree tree, String type) {
        this.tree = tree;
        this.type = type;
        merge = type.equals("lalr");
    }

    public void makeStart() {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is defined");
        }
        start = new RuleDecl(startName, new Sequence(tree.start));
        tree.addRule(start);
    }

    public void writeTableDot(PrintWriter writer) {
        DotWriter.table(writer, this, false);
    }

    public void writeDot(PrintWriter dotWriter) {
        DotWriter.writeDot(table, dotWriter);
    }

    void prepare() {
        EbnfToBnf.rhsSequence = true;
        tree = EbnfToBnf.transform(tree);
        tree.prepare();

        makeStart();
        first = new LrItem(start, 0);
        if (!type.equals("lr0")) {
            first.lookAhead.add(dollar);
        }
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
        LrItemSet set = new LrItemSet(item, tree, type);
        set.closure();
        table.addSet(set);
        return set;
    }

    public void generate() {
        prepare();
        writeGrammar();

        LrItemSet firstSet = makeSet(first);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            LrItemSet curSet = queue.poll();
            if (debug) System.out.println("set=" + curSet.stateId);
            //iterate items
            while (true) {
                LrItem curItem = curSet.getItem();
                if (curItem == null) break;//already done

                Name symbol = curItem.getDotNode();
                if (symbol == null) continue;//dot end

                LrItem toFirst = new LrItem(curItem, curItem.dotPos + 1);
                LrItemSet targetSet = table.getTargetSet(curSet, symbol);

                if (targetSet == null) {
                    //find another set that has same core
                    HashSet<LrItemSet> similars = new HashSet<>();
                    for (LrItemSet set : table.itemSets) {
                        for (LrItem item : set.kernel) {
                            if (item.equals(toFirst)) {
                                targetSet = set;
                                break;
                            }
                            else if (item.isSame(toFirst)) {
                                similars.add(set);
                            }
                        }
                        if (targetSet != null) break;
                    }
                    //or another set that has similar core(merge-able)
                    if (targetSet == null) {
                        if (merge && !similars.isEmpty()) {
                            targetSet = similars.iterator().next();
                            doMerge(toFirst, targetSet);
                        }
                        if (targetSet == null) {
                            //create new set
                            targetSet = makeSet(toFirst);
                        }
                    }
                    table.addTransition(curSet, targetSet, symbol);
                    addQueue(targetSet);
                }
                else {
                    //has same symbol transition
                    //check if item there
                    boolean has = false;
                    for (LrItem item : targetSet.kernel) {
                        if (item.isSame(toFirst)) {
                            if (!item.equals(toFirst)) {
                                //has similar so merge
                                doMerge(toFirst, targetSet);
                            }
                            has = true;
                            break;
                        }
                    }
                    if (!has) {
                        //doesn't have so add
                        targetSet.addCore(toFirst);
                        addQueue(targetSet);
                    }
                }
                if (curSet == firstSet && symbol.equals(tree.start)) {
                    acc = table.getId(targetSet);
                }
            }
        }
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
            if (item.getDotNode() == null) continue;
            LrItemSet target = table.getTargetSet(set, item.getDotNode());
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

    public void checkAll() {
        isResolved = false;
        for (LrItemSet set : table.itemSets) {
            check(set);
        }
        if (isResolved) {
            checkAll();
        }
    }

    //check if two item has conflict
    void check(LrItemSet set) {
        boolean lr0 = type.equals("lr0");
        for (int i = 0; i < set.all.size(); i++) {
            LrItem i1 = set.all.get(i);
            for (int j = i + 1; j < set.all.size(); j++) {
                LrItem i2 = set.all.get(j);
                if (i1.hasReduce() && i2.hasReduce()) {
                    if (lr0) {
                        throw new RuntimeException("reduce/reduce conflict " + table.getId(set) + "\n" + set);
                    }
                    else {
                        //if any lookahead conflict
                        HashSet<Name> la = new HashSet<>(i1.lookAhead);
                        la.retainAll(i2.lookAhead);
                        if (!la.isEmpty()) {
                            throw new RuntimeException("reduce/reduce conflict " + table.getId(set) + "\n" + set);
                        }
                    }
                }
                else {
                    LrItem shift;
                    LrItem reduce;
                    if (i1.hasReduce() && !i2.hasReduce() && (lr0 || i1.lookAhead.contains(i2.getDotNode()))) {
                        shift = i2;
                        reduce = i1;
                    }
                    else if (!i1.hasReduce() && i2.hasReduce() && i2.lookAhead.contains(i1.getDotNode())) {
                        shift = i1;
                        reduce = i2;
                    }
                    else {
                        continue;
                    }
                    boolean removed = false;
                    //if same rule,check assoc
                    if (shift.rule.equals(reduce.rule)) {//todo isSame?
                        LrItemSet target = table.getTargetSet(set, shift.getDotNode());
                        LrItem newItem = new LrItem(shift, shift.dotPos + 1);
                        for (LrItem targetItem : target.all) {
                            if (targetItem.isSame(newItem)) {
                                Assoc assoc = getAssoc(shift.getDotNode());
                                if (assoc == null) {
                                    //prefer shift
                                }
                                else if (assoc.isLeft) {
                                    //keep reduce,remove shift
                                    removeItem(set, shift);
                                    removed = true;
                                }
                                else {
                                    //keep shift,remove reduce
                                    reduce.lookAhead.remove(shift.getDotNode());
                                    if (reduce.lookAhead.isEmpty()) {
                                        removeItem(set, reduce);
                                    }
                                    removed = true;
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
                        if (shift.rule.name.equals(reduce.rule.name)) {
                            if (reduce.rule.index < shift.rule.index) {
                                //prefer reduce
                                removeItem(set, shift);
                                removed = true;
                            }
                            else {
                                //prefer shift
                                reduce.lookAhead.remove(shift.getDotNode());
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
                        throw new RuntimeException(String.format("shift/reduce conflict %d sym=%s\n%s", table.getId(set), shift.getDotNode(), set));
                    }

                }
            }
        }
    }

    void handleAssoc() {

    }

    Assoc getAssoc(Name sym) {
        for (Assoc assoc : tree.assocList) {
            if (assoc.list.contains(sym)) {
                return assoc;
            }
        }
        return null;
    }

    void removeItem(LrItemSet set, LrItem item) {
        //remove incoming and outgoing transitions
        List<LrTransition> out = new ArrayList<>();
        for (LrTransition tr : table.getTrans(set)) {
            if (tr.symbol.equals(item.getDotNode())) {
                out.add(tr);
            }
        }
        if (out.size() == 1) {
            //remove
            table.getTrans(set).remove(out.get(0));
        }
        List<LrTransition> in = new ArrayList<>();
        for (LrItemSet from : table.itemSets) {
            for (LrTransition tr : table.getTrans(from)) {
                if (tr.to == set) {
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
                if (item.dotPos != 0 || item.hasReduce()) continue;
                //walk to reduce state of item and set goto
                LrItemSet curSet = set;
                LrItem curItem = item;
                while (true) {
                    curSet = table.getTargetSet(curSet, curItem.getDotNode());
                    LrItem tmpItem = new LrItem(curItem.rule, curItem.dotPos + 1);
                    //use tmp to get original item
                    for (LrItem tmp : curSet.all) {
                        if (tmp.isSame(tmpItem)) {
                            curItem = tmp;
                            break;
                        }
                    }
                    if (curItem.hasReduce()) {
                        //set goto
                        curItem.gotoSet.add(set);
                        if (debug) System.out.println("set goto");
                        break;
                    }
                }
            }
        }
    }
}
