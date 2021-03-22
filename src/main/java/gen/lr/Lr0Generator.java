package gen.lr;

import gen.parser.BnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

// lr(0)
public class Lr0Generator extends IndentWriter {
    public PrintWriter dotWriter;
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<Lr0ItemSet> table = new LrDFA<>();
    RuleDecl start;
    public static NameNode dollar = new NameNode("$");

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        start = new RuleDecl("s'", tree.start);
        tree.addRule(start);

        check();
        start = tree.getRule("s'");

        Queue<Lr0ItemSet> queue = new LinkedList<>();

        Lr0Item first = new Lr0Item(start, 0);
        Lr0ItemSet firstSet = new Lr0ItemSet(first, tree);
        table.addId(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr0ItemSet curSet = queue.poll();
            for (Lr0Item from : curSet.getAll()) {
                curSet.done.add(from);
                NameNode symbol = from.getDotNode();
                if (symbol != null) {
                    //goto
                    Lr0Item toFirst = new Lr1Item((Lr1Item) from, from.dotPos + 1);
                    Lr1ItemSet targetSet = getSet(toFirst);
                    if (targetSet == null) {
                        targetSet = getOldSet(curSet, symbol);
                        if (targetSet == null) {
                            targetSet = new Lr1ItemSet(toFirst, tree);
                            table.addId(targetSet);
                            queue.add(targetSet);
                            table.addTransition(curSet, targetSet, symbol);
                        }
                        else {
                            //merge
                            targetSet.all.add(toFirst);
                            targetSet.kernel.add(toFirst);
                            targetSet.closure(toFirst);
                            /*targetSet = new Lr1ItemSet(toFirst, tree);
                            table.addId(targetSet);*/
                            queue.add(targetSet);
                        }
                        System.out.printf("%s %s to %s\n", symbol, printSet(curSet), printSet(targetSet));
                    }
                    else {
                        table.addTransition(curSet, targetSet, symbol);
                    }
                    //throw new RuntimeException();
                }
            }//for
        }

        //merge();
        writeDot();
    }

    private void writeDot() {
        try {
            PrintWriter dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            dotWriter.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            dotWriter.println("rankdir = TD");
            dotWriter.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");

            //labels
            for (Lr0ItemSet set : table.itemSets) {
                dotWriter.printf("%s [shape=box xlabel=\"%s\" %s label=\"%s\"]\n", table.getId(set), table.getId(set), isFinal(set) ? "color=red " : "", set.toString().replace("\n", "\\l") + "\\l");
            }

            for (int i = 0; i <= table.lastId; i++) {
                for (LrTransition<Lr0ItemSet> t : table.map[i]) {
                    dotWriter.printf("%s -> %s [label=\"%s\"]\n",
                            table.getId(t.from), table.getId(t.to), t.symbol.name);
                }
            }

            dotWriter.println("}");
            dotWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get itemSet that contains item
    Lr0ItemSet getSet(Lr1Item kernel) {
        for (Lr0ItemSet itemSet : table.itemSets) {
            for (Lr0Item item : itemSet.kernel) {
                if (item.equals(kernel)) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr0ItemSet getOldSet(Lr0ItemSet from, NameNode symbol) {
        for (LrTransition<Lr0ItemSet> transition : table.getTrans(from)) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    String printSet(Lr0ItemSet set) {
        return String.format("(%d)%s", table.getId(set), set.kernel);
    }

    boolean isFinal(Lr0ItemSet set) {
        //has epsilon
        for (Lr0Item item : set.all) {
            if (hasEpsilon(item.ruleDecl.rhs.asSequence())) {
                return true;
            }
        }
        return table.getTrans(set).isEmpty();
    }

    boolean hasEpsilon(Sequence node) {
        for (Node c : node) {
            if (c.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    private void check() {
        PrepareTree.checkReferences(tree);
        BnfTransformer.rhsSequence = true;
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);
    }
}
