package mesut.parserx.gen.lr;

import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class LRGen<T extends LrItemSet> {
    public static NameNode dollar = new NameNode("$");
    Tree tree;
    RuleDecl start;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<T> table = new LrDFA<>();
    LrItem first;

    public static RuleDecl makeStart(Tree tree) {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is defined");
        }
        List<RuleDecl> list = tree.getRules(tree.start.name);
        boolean create = false;
        if (list.size() == 1) {
            //if start rule is already simple don't create new one
            Node rhs = list.get(0).rhs;
            if (rhs.isSequence()) {
                Node r = rhs.asSequence().normal();
                if (!r.isName()) {
                    create = true;
                }
            }
        }
        else {
            create = true;
        }
        if (create) {
            //regex start node,create new one
            String name = "s'";
            if (tree.start.name.equals(name)) {
                name = "s_";
            }
            RuleDecl start = new RuleDecl(name, Sequence.of(tree.start));
            tree.addRule(start);
            tree.start = start.ref();
            return start;
        }
        return list.get(0);
    }

    public File tableDotFile() {
        return new File(dir, tree.file.getName() + "-table.dot");
    }

    public void writeTableDot() {
        DotWriter.table(this, false);
    }

    void prepare() {
        PrepareTree.checkReferences(tree);//todo remove
        EbnfToBnf.rhsSequence = true;
        tree = EbnfToBnf.transform(tree);
        PrepareTree.checkReferences(tree);

        start = makeStart(tree);
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
            IOUtils.write(tree.toString(), file);
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
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            T curSet = queue.poll();
            //iterate items
            while (true) {
                LrItem from = curSet.getItem();
                if (from == null) break;

                NameNode symbol = from.getDotNode();
                if (symbol == null) continue;
                LrItem toFirst = new LrItem(from, from.dotPos + 1);
                toFirst.gotoSet = from.gotoSet == null ? curSet : from.gotoSet;
                T targetSet = getSet(curSet, symbol);
                if (targetSet == null) {
                    targetSet = getSet(toFirst);//find another set that has same core
                    if (targetSet == null) {
                        targetSet = makeSet(toFirst);
                        table.addId(targetSet);
                    }
                    else {
                        System.out.println("merge");
                        //merge
                        if (!targetSet.all.contains(toFirst)) {
                            targetSet.addItem(toFirst);
                        }
                        /*targetSet.all.add(toFirst);
                        targetSet.kernel.add(toFirst);
                        targetSet.closure(toFirst);*/
                        /*targetSet = new Lr1ItemSet(toFirst, tree);
                        table.addId(targetSet);*/
                    }
                    table.addTransition(curSet, targetSet, symbol);
                    queue.add(targetSet);
                    System.out.printf("%s %s to %s\n", symbol, printSet(curSet), printSet(targetSet));
                }
                else {
                    //check if item there
                    if (!targetSet.all.contains(toFirst)) {
                        //merge
                        targetSet.addItem(toFirst);
                        table.addTransition(curSet, targetSet, symbol);
                        queue.add(targetSet);
                    }
                }
            }
        }
    }

    //check if two item has conflict
    void check(LrItemSet set) {
        for (LrItem i1 : set.all) {
            for (LrItem i2 : set.all) {
                if (i1 == i2) continue;
                if (i1.rule.rhs.equals(i2.rule.rhs)) {
                    if (i1.hasReduce() && i2.hasReduce()) {
                        throw new RuntimeException("reduce/reduce conflict " + set);
                    }
                    else if (i1.hasReduce() || i2.hasReduce()) {
                        throw new RuntimeException("shift/reduce conflict " + set);
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
    T getSet(T from, NameNode symbol) {
        for (LrTransition<T> tr : table.getTrans(from)) {
            if (tr.from.equals(from) && tr.symbol.equals(symbol)) {
                return tr.to;
            }
        }
        return null;
    }

    String printSet(T set) {
        return String.format("(%d)%s", table.getId(set), set.kernel);
    }

    public void writeDot(PrintWriter dotWriter) {
        if (dotWriter == null) {
            try {
                dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        DotWriter.writeDot(table, dotWriter);
    }
}
