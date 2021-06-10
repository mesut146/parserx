package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.Tree;

import java.util.HashMap;
import java.util.Map;


public class Lr1Generator extends LRGen<Lr1ItemSet> {

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    @Override
    void prepare() {
        super.prepare();
        first.lookAhead.add(dollar);
    }


    @Override
    public Lr1ItemSet makeSet(LrItem item) {
        return new Lr1ItemSet(item, tree);
    }

    //merge same kernel states with different lookaheads
    public void merge() {
        LrDFA<Lr1ItemSet> table2 = new LrDFA<>();
        Map<Integer, Map<NameNode, Lr1ItemSet>> map = new HashMap<>();
        for (Lr1ItemSet from : table.itemSets) {
            int id = table.getId(from);
            Map<NameNode, Lr1ItemSet> m = map.get(id);
            if (m == null) {
                m = new HashMap<>();
                map.put(id, m);
            }

            for (LrTransition<Lr1ItemSet> t : table.getTrans(from)) {
                /** if(table2.getTrans(from,t.symbol)){
                 }*/

                if (m.containsKey(t.symbol)) {
                    //merge
                    m.get(t.symbol).all.addAll(t.to.all);
                    //remove t
                    table2.addTransition(from, t.to, t.symbol);
                    table.getTrans(from).remove(t);
                }
                else {
                    m.put(t.symbol, t.to);
                }
            }
        }

    }


}
