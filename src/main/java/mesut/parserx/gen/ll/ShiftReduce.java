package mesut.parserx.gen.ll;

import mesut.parserx.gen.lr.LrDFA;
import mesut.parserx.gen.lr.LrItem;
import mesut.parserx.gen.lr.LrItemSet;
import mesut.parserx.nodes.*;

import java.util.Stack;

public class ShiftReduce extends Transformer {
    Tree tree;
    Stack<LrItemSet> queue = new Stack<>();

    public ShiftReduce(Tree tree) {
        super(tree);
    }

    public void findAndSub(RuleDecl decl) {
        LrItem startItem = new LrItem(decl, 0);
        LrItemSet startSet = new LrItemSet(startItem, tree, "lr1");
        queue.add(startSet);
        LrDFA dfa = new LrDFA();
        while (!queue.isEmpty()) {
            LrItemSet curSet = queue.pop();
            while (true) {
                LrItem curItem = curSet.getItem();
                Name symbol = curItem.getDotNode();
                if (symbol == null) continue;
                LrItem toFirst = new LrItem(curItem, curItem.dotPos + 1);
                LrItemSet targetSet = dfa.getTargetSet(curSet, symbol);
                if (targetSet == null) {
                    targetSet = new LrItemSet(toFirst, tree, "lr1");
                    dfa.addTransition(curSet, targetSet, symbol);
                }
                else {
                    boolean has = false;
                    for (LrItem item : targetSet.all) {
                        if (item.equals(toFirst)) {
                            has = true;
                        }
                    }
                    if (!has) {
                        targetSet.addCore(toFirst);
                        addQueue(targetSet);
                    }
                }
            }
        }

    }

    void addQueue(LrItemSet set) {
        if (!queue.contains(set)) {
            queue.add(set);
        }
    }

}
