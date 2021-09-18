package mesut.parserx.gen.lr;

import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Sequence;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

//lr table generator
public abstract class LRTableGen<T extends LrItemSet> {
    public static Name dollar = new Name("$", true);//eof
    public static String startName = "%start";
    public int acc = 1;
    public LrDFA<T> table = new LrDFA<>();
    public RuleDecl start;
    public boolean merge;
    Tree tree;
    LrItem first;

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

    public void writeTableDot() {
        DotWriter.table(this, false);
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
                                    if (item.isSame(toFirst)) {
                                        //can merge
                                        merged = true;
                                        item.lookAhead.addAll(toFirst.lookAhead);
                                        //todo carry la to closure
                                        System.out.println("lalr merged");
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
                        System.out.println("merge");
                        if (!targetSet.all.contains(toFirst)) {
                            targetSet.addCore(toFirst);
                        }
                    }
                    table.addTransition(curSet, targetSet, symbol);
                    queue.add(targetSet);
                    log(curItem, curSet, toFirst, targetSet, symbol);
                }
                else {
                    //check if item there
                    if (!targetSet.all.contains(toFirst)) {
                        if (merge) {
                            boolean merged = false;
                            for (LrItem item : targetSet.all) {
                                if (item.isSame(toFirst)) {
                                    item.lookAhead.addAll(toFirst.lookAhead);
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
            System.out.println("-----------------");
        }
        checkAll();
    }

    void log(LrItem from, LrItemSet curSet, LrItem toFirst, LrItemSet targetSet, Name symbol) {
        //System.out.printf("%s -> %s by %s\n", table.getId(curSet), table.getId(targetSet), symbol.name);
        //System.out.printf("%s (%d)%s to (%d)%s\n", symbol, table.getId(curSet), from, table.getId(targetSet), toFirst);
    }

    public void checkAll() {
        for (LrItemSet set : table.itemSets) {
            check(set);
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
                    if (i1.hasReduce() && !i2.hasReduce()) {
                        if (lr0 || i1.lookAhead.contains(i2.getDotNode())) {
                            throw new RuntimeException("shift/reduce conflict " + table.getId(set) + "\n" + set);
                        }
                    }
                    else if (!i1.hasReduce() && i2.hasReduce()) {
                        if (lr0 || i2.lookAhead.contains(i1.getDotNode())) {
                            throw new RuntimeException("shift/reduce conflict " + table.getId(set) + "\n" + set);
                        }
                    }
                }
            }
        }
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
        if (dotWriter == null) {
            try {
                dotWriter = new PrintWriter(new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dot")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
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
