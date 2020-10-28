package gen.lr;

import gen.EbnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.NameNode;
import nodes.Node;
import nodes.Tree;
import nodes.RuleDecl;

import java.io.PrintWriter;
import java.util.*;

// lr(0)
public class Lr0Generator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    List<LrTransition> transitions;
    List<Lr0ItemSet> itemSets = new ArrayList<>();
    public PrintWriter dotWriter;
    Map<Lr0ItemSet, Integer> idMap = new HashMap<>();
    int lastId = -1;

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        EbnfTransformer.expand_or = true;
        check();
        //System.out.println(tree);
        dotBegin();

        transitions = new ArrayList<>();
        Queue<Lr0ItemSet> queue = new LinkedList<>();

        RuleDecl start = new RuleDecl("s'", tree.start);
        Lr0Item first = new Lr0Item(start, 0);
        Lr0ItemSet firstSet = new Lr0ItemSet(Collections.singletonList(first), tree);
        idMap.put(firstSet, ++lastId);
        itemSets.add(firstSet);
        firstSet.closure();
        //System.out.println("\nset = " + firstSet);

        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr0ItemSet curSet = queue.peek();
            Lr0Item from = curSet.findTransitable();
            if (from != null) {
                NameNode symbol = from.getDotNode();
                Lr0Item toFirst = new Lr0Item(from.ruleDecl, from.dotPos2 + 1);
                curSet.curIndex++;
                Lr0ItemSet targetSet;
                targetSet = getSet(toFirst);
                if (targetSet == null) {
                    targetSet = getTransition(curSet, symbol);
                    if (targetSet == null) {//new transition
                        targetSet = new Lr0ItemSet(toFirst, tree);
                        idMap.put(targetSet, ++lastId);
                        itemSets.add(targetSet);
                        targetSet.closure();
                        System.out.printf("transition(%d) by %s (%s)%s to (%s)%s\n", transitions.size(), symbol, getId(curSet), curSet.first, getId(targetSet), targetSet.first);
                        transitions.add(new LrTransition(curSet, targetSet, symbol));
                    }
                    else {
                        //merge and update transition
                        if (!targetSet.first.contains(toFirst)) {
                            targetSet.first.add(toFirst);
                            targetSet.all.clear();
                            targetSet.all.addAll(targetSet.first);
                            targetSet.closure();

                            targetSet.curIndex = targetSet.first.size() - 1;
                        }
                    }
                    queue.add(targetSet);
                }
            }
            else {
                queue.poll();
            }
            curSet.done.add(from);
        }
        dotLabels();
        for (LrTransition transition : transitions) {
            dot(transition.from, transition.to, transition.symbol);
        }
        dotEnd();
    }

    //get itemSet that contains item
    Lr0ItemSet getSet(Lr0Item first) {
        for (Lr0ItemSet itemSet : itemSets) {
            if (itemSet.first.equals(first)) {
                return itemSet;
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr0ItemSet getTransition(Lr0ItemSet from, Node symbol) {
        for (LrTransition transition : transitions) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    private void dotBegin() {
        if (dotWriter != null) {
            dotWriter.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            dotWriter.println("rankdir = TD");
            dotWriter.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");
        }
    }

    void dotEnd() {
        if (dotWriter != null) {
            dotWriter.println("}");
            dotWriter.flush();
            dotWriter.close();
        }
    }

    private void dot(Lr0ItemSet curSet, Lr0ItemSet toSet, NameNode symbol) {
        if (dotWriter != null) {
            dotWriter.printf("%s -> %s [label=\"%s\"]\n", getId(curSet), getId(toSet), symbol.name);
        }
    }

    int getId(Lr0ItemSet itemSet) {
        for (Lr0Item kernel : itemSet.first) {
            int id = getId(kernel);
            if (id != -1) {
                return id;
            }
        }
        return -1;
    }

    int getId(Lr0Item item) {
        for (Map.Entry<Lr0ItemSet, Integer> entry : idMap.entrySet()) {
            if (entry.getKey().first.contains(item)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    void dotLabels() {
        if (dotWriter != null) {
            for (Lr0ItemSet set : itemSets) {
                if (isFinal(set)) {
                    dotWriter.printf("%s [color=red shape=box xlabel=\"%s\" label=\"%s\"]\n", getId(set), getId(set), set.toString().replace("\n", "\\n"));
                }
                else {
                    dotWriter.printf("%s [shape=box xlabel=\"%s\" label=\"%s\"]\n", getId(set), getId(set), set.toString().replace("\n", "\\n"));
                }

            }
        }
    }

    boolean isFinal(Lr0ItemSet set) {
        //has epsilon
        for (Lr0Item item : set.all) {
            if (hasEpsilon(item.ruleDecl.rhs)) {
                return true;
            }
        }
        for (LrTransition transition : transitions) {
            if (set.equals(transition.from)) {
                return false;
            }
        }
        return true;
    }

    boolean hasEpsilon(Node node) {
        if (node.isEmpty()) {
            return true;
        }
        if (node.isSequence()) {
            for (Node c : node.asSequence()) {
                if (c.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void check() {
        PrepareTree.checkReferences(tree);
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
    }
}
