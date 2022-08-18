package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

import java.util.ArrayList;
import java.util.List;

public class LrDFA {
    public List<LrItemSet> itemSets = new ArrayList<>();
    public LrItemSet first;
    public LrItemSet acc;
    public int lastId = -1;
    public List<Name> tokens = new ArrayList<>();
    public List<Name> rules = new ArrayList<>();


    //if there exist another transition from this
    public LrItemSet getTargetSet(LrItemSet from, Name symbol) {
        for (LrTransition tr : from.transitions) {
            if (tr.symbol.equals(symbol)) {
                return tr.target;
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
