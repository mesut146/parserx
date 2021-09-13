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
    public int acc = 1;
    Tree tree;
    RuleDecl start;
    String dir;
    LrDFA<T> table = new LrDFA<>();
    LrItem first;

    public void makeStart() {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is defined");
        }
        start = new RuleDecl("@start", new Sequence(tree.start));
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
        table.addId(firstSet);
        firstSet.closure();
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            T curSet = queue.poll();
            //iterate items
            while (true) {
                LrItem from = curSet.getItem();
                if (from == null) break;

                Name symbol = from.getDotNode();
                if (symbol == null) continue;
                LrItem toFirst = new LrItem(from, from.dotPos + 1);
                toFirst.gotoSet = from.gotoSet == null ? curSet : from.gotoSet;
                T targetSet = getSet(curSet, symbol);
                if (targetSet == null) {
                    targetSet = getSet(toFirst);//find another set that has same core
                    if (targetSet == null) {
                        targetSet = makeSet(toFirst);
                        table.addId(targetSet);
                        targetSet.closure();
                    }
                    else {
                        System.out.println("merge");
                        if (!targetSet.all.contains(toFirst)) {
                            targetSet.addCore(toFirst);
                            //targetSet.addItem(toFirst);
                        }
                    }
                    table.addTransition(curSet, targetSet, symbol);
                    queue.add(targetSet);
                    log(from, curSet, toFirst, targetSet, symbol);
                }
                else {
                    //check if item there
                    if (!targetSet.all.contains(toFirst)) {
                        //merge
                        //targetSet.addItem(toFirst);
                        targetSet.addCore(toFirst);
                        table.addTransition(curSet, targetSet, symbol);
                        queue.add(targetSet);
                        log(from, curSet, toFirst, targetSet, symbol);
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
        System.out.printf("%s (%d)%s to (%d)%s\n", symbol, table.getId(curSet), from, table.getId(targetSet), toFirst);
    }

    void checkAll() {
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

    //if there exist another transition from this
    T getSet(T from, Name symbol) {
        for (LrTransition<T> tr : table.getTrans(from)) {
            if (tr.from.equals(from) && tr.symbol.equals(symbol)) {
                return tr.to;
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
}
