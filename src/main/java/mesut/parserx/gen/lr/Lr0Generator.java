package mesut.parserx.gen.lr;

import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.nodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

// lr(0)
public class Lr0Generator {
    public static NameNode dollar = new NameNode("$");
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<Lr0ItemSet> table = new LrDFA<>();
    Map<RuleDecl, Integer> itemIds = new HashMap<>();
    RuleDecl start;

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public static RuleDecl makeStart(Tree tree) {
        RuleDecl start = tree.getRule(tree.start.name);
        Node rhs = start.rhs;
        if (!rhs.isName()) {
            if (rhs.isSequence()) {
                Node r = rhs.asSequence().normal();
                if (!r.isName()) {
                    //regex start node,create new one
                    String name = "s'";
                    if (tree.start.name.equals(name)) {
                        name = "s_";
                    }
                    start = new RuleDecl(name, Sequence.of(tree.start));
                    tree.addRule(start);
                }
            }
        }
        return start;
    }

    public void generate() {
        check();

        Queue<Lr0ItemSet> queue = new LinkedList<>();

        LrItem first = new LrItem(start, 0);
        Lr0ItemSet firstSet = new Lr0ItemSet(first, tree);
        table.addId(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr0ItemSet curSet = queue.poll();
            for (LrItem from : curSet.all) {
                //curSet.done.add(from);
                NameNode symbol = from.getDotNode();
                if (symbol != null) {
                    //goto
                    LrItem toFirst = new LrItem(from.ruleDecl, from.dotPos + 1);
                    Lr0ItemSet targetSet = getSet(toFirst);
                    if (targetSet == null) {
                        targetSet = getOldSet(curSet, symbol);
                        if (targetSet == null) {
                            targetSet = new Lr0ItemSet(toFirst, tree);
                            table.addId(targetSet);
                            queue.add(targetSet);
                            table.addTransition(curSet, targetSet, symbol);
                        }
                        else {
                            System.out.println("merge " + curSet);
                            //merge
                            //targetSet.all.add(toFirst);
                            targetSet.kernel.add(toFirst);
                            targetSet.all.clear();
                            targetSet.closure();
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
    }

    void writeSource() {

    }

    public void makeTable() {
        DotWriter.lr0Table(this);
    }

    public void writeDot(PrintWriter dotWriter) {
        if (dotWriter == null && dir != null) {
            try {
                dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        DotWriter.writeDot(table, dotWriter);
    }

    //get itemSet that contains item
    Lr0ItemSet getSet(LrItem kernel) {
        for (Lr0ItemSet itemSet : table.itemSets) {
            for (LrItem item : itemSet.kernel) {
                if (item.equals(kernel)) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    //if there exist another transition from this set
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

    private void check() {
        PrepareTree.checkReferences(tree);
        EbnfToBnf.rhsSequence = true;
        tree = EbnfToBnf.transform(tree);
        PrepareTree.checkReferences(tree);

        int id = 0;
        for (RuleDecl rule : tree.rules) {
            itemIds.put(rule, id++);
        }

        start = Lr0Generator.makeStart(tree);
    }
}
