package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class LrDFA {
    public List<LrItemSet> itemSets = new ArrayList<>();
    public LrItemSet first;
    public LrItemSet acc;
    public int lastId = -1;
    public List<Name> tokens = new ArrayList<>();
    public List<Name> rules = new ArrayList<>();
    public static boolean debugTransition = false;

    public void addTransition(LrItemSet from, LrItemSet to, Name symbol) {
        LrTransition t = new LrTransition(from, to, symbol);
        List<LrTransition> list = from.transitions;
        if (list.contains(t)) {
            return;
        }
        list.add(t);
        if (symbol.isToken && !tokens.contains(symbol)) {
            tokens.add(symbol);
        }
        if (symbol.isRule() && !rules.contains(symbol)) {
            rules.add(symbol);
        }
        if (debugTransition) {
            System.out.printf("%s -> %s by %s\n", from.stateId, to.stateId, symbol.name);
        }
    }

    //if there exist another transition from this
    public LrItemSet getTargetSet(LrItemSet from, Name symbol) {
        for (LrTransition tr : from.transitions) {
            if (tr.symbol.equals(symbol)) {
                return tr.to;
            }
        }
        return null;
    }

    public void addSet(LrItemSet set) {
        if (set.stateId != -1) {
            throw new RuntimeException("set already exists " + set);
        }
        set.stateId = ++lastId;
        itemSets.add(set);
    }


}
