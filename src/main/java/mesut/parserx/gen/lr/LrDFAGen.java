package mesut.parserx.gen.lr;

import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//lr table generator
public abstract class LrDFAGen<T extends LrItemSet> {
    public static Name dollar = new Name("$", true);//eof
    public static String startName = "%start";
    public int acc = 1;
    public LrDFA<T> table = new LrDFA<>();
    public RuleDecl start;
    public boolean merge;//lalr
    Tree tree;
    LrItem first;
    boolean resolved;

    public void makeStart() {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is defined");
        }
        start = new RuleDecl(startName, new Sequence(tree.start));
        tree.addRule(start);
    }

    public File tableDotFile() {
        return new File(tree.options.outDir, Utils.newName(tree.file.getName(), "-table.dot"));
    }

    public void writeTableDot(PrintWriter writer) {
        DotWriter.table(writer, this, false);
    }

    void prepare() {
        EbnfToBnf.rhsSequence = true;
        tree = EbnfToBnf.transform(tree);
        tree.prepare();

        makeStart();
        first = new LrItem(start, 0);
    }

    public void writeGrammar() {
        File file = tree.file;
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if (i != -1) {
            s = s.substring(0, i);
        }
        writeGrammar(new File(file.getParent(), s + "-final.g"));
    }

    public void writeGrammar(File file) {
        try {
            RuleDecl.printIndex = true;
            Utils.write(tree.toString(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract T makeSet(LrItem item);


    public void generate() {
        prepare();

        Queue<T> queue = new LinkedList<>();//itemsets

        T firstSet = makeSet(first);
        table.addSet(firstSet);
        firstSet.closure();
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            T curSet = queue.poll();
            //iterate items
            while (true) {
                LrItem curItem = curSet.getItem();
                if (curItem == null) break;

                Name symbol = curItem.getDotNode();
                if (symbol == null) continue;
                LrItem toFirst = new LrItem(curItem, curItem.dotPos + 1);
                T targetSet = table.getTargetSet(curSet, symbol);
                if (targetSet == null) {
                    //find another set that has same core
                    targetSet = getSet(toFirst);
                    if (targetSet == null) {
                        if (merge) {
                            boolean merged = false;
                            for (T set : table.itemSets) {
                                for (LrItem item : set.kernel) {
                                    if (item.isSame(toFirst) && !item.equals(toFirst)) {
                                        //can merge
                                        merged = true;
                                        doMerge(toFirst, set);
                                        targetSet = set;
                                        break;
                                    }
                                }
                                if (merged) break;
                            }
                            if (!merged) {
                                targetSet = makeSet(toFirst);
                                table.addSet(targetSet);
                                targetSet.closure();
                            }
                        }
                        else {
                            targetSet = makeSet(toFirst);
                            table.addSet(targetSet);
                            targetSet.closure();
                        }
                    }
                    else {
                        //found a set that has same core
                        if (!targetSet.all.contains(toFirst)) {
                            targetSet.addCore(toFirst);
                        }
                    }
                    table.addTransition(curSet, targetSet, symbol);
                    queue.add(targetSet);
                    log(curItem, curSet, toFirst, targetSet, symbol);
                }
                else {
                    //has same symbol transition
                    //check if item there
                    if (!targetSet.all.contains(toFirst)) {
                        if (merge) {
                            boolean merged = false;
                            for (LrItem item : targetSet.all) {
                                if (item.isSame(toFirst) && !item.equals(toFirst)) {
                                    doMerge(toFirst, targetSet);
                                    merged = true;
                                    break;
                                }
                            }
                            if (!merged) {
                                targetSet.addCore(toFirst);
                                queue.add(targetSet);
                            }
                        }
                        else {
                            targetSet.addCore(toFirst);
                            queue.add(targetSet);
                            //table.addTransition(curSet, targetSet, symbol);
                        }
                        //merge
                        log(curItem, curSet, toFirst, targetSet, symbol);
                    }
                }
                if (curSet == firstSet && symbol.equals(tree.start)) {
                    acc = table.getId(targetSet);
                }
            }
        }
        //checkAll();
    }

    void doMerge(LrItem item, LrItemSet set) {
        System.out.println("lalr merged " + set.stateId);
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
            LrItemSet target = table.getTargetSet((T) set, item.getDotNode());
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

    void log(LrItem from, LrItemSet curSet, LrItem toFirst, LrItemSet targetSet, Name symbol) {
        //System.out.printf("%s -> %s by %s\n", table.getId(curSet), table.getId(targetSet), symbol.name);
        //System.out.printf("%s (%d)%s to (%d)%s\n", symbol, table.getId(curSet), from, table.getId(targetSet), toFirst);
    }

    public void checkAll() {
        resolved = false;
        for (LrItemSet set : table.itemSets) {
            check(set);
        }
        if (resolved) {
            checkAll();
        }
    }

    //check if two item has conflict
    void check(LrItemSet set) {
        boolean lr0 = this instanceof Lr0Generator;
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
                    if (shift.rule.equals(reduce.rule)) {
                        LrItemSet target = table.getTargetSet((T) set, shift.getDotNode());
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
                            System.out.println("assoc is used on " + set.stateId);
                            this.resolved = true;
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
                            System.out.println("prec used in " + set.stateId);
                            this.resolved = true;
                        }
                    }
                    if (!removed) {
                        throw new RuntimeException("shift/reduce conflict " + table.getId(set) + "\n" + set);
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
        List<LrTransition<?>> out = new ArrayList<>();
        for (LrTransition<?> tr : table.getTrans(set)) {
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
            for (LrTransition<?> tr : table.getTrans(from)) {
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

    //get itemSet that contains item
    T getSet(LrItem kernel) {
        for (T itemSet : table.itemSets) {
            for (LrItem item : itemSet.kernel) {
                if (item.equals(kernel)) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    public void writeDot(PrintWriter dotWriter) {
        DotWriter.writeDot(table, dotWriter);
    }

    public void genGoto() {
        for (LrItemSet set : table.itemSets) {
            for (LrItem item : set.all) {
                if (item.dotPos != 0 || item.hasReduce()) continue;
                //walk to reduce state of item and set goto
                LrItemSet curSet = set;
                LrItem curItem = item;
                while (true) {
                    curSet = table.getTargetSet((T) curSet, curItem.getDotNode());
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
                        System.out.println("set goto");
                        break;
                    }
                }
            }
        }
    }
}
