package mesut.parserx.gen.lr;

import mesut.parserx.nodes.NameNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LrDFA<T extends LrItemSet> {
    public List<LrTransition<T>>[] map = new List[100];
    public List<NameNode> tokens = new ArrayList<>();
    public List<NameNode> rules = new ArrayList<>();
    int lastId = -1;
    List<T> itemSets = new ArrayList<>();//todo idMap.keys()
    Map<T, Integer> idMap = new HashMap<>();//item set to state id

    public void addTransition(T from, T to, NameNode symbol) {
        LrTransition<T> t = new LrTransition<>(from, to, symbol);
        List<LrTransition<T>> list = getTrans(t.from);
        list.add(t);
        if (symbol.isToken && !tokens.contains(symbol)) {
            tokens.add(symbol);
        }
        if (symbol.isRule() && !rules.contains(symbol)) {
            rules.add(symbol);
        }
    }

    public List<LrTransition<T>> getTrans(T set) {
        List<LrTransition<T>> list = map[getId(set)];
        if (list == null) {
            list = new ArrayList<>();
            map[getId(set)] = list;
        }
        return list;
    }

    /*public List<LrTransition<T>> getTrans(T set, NameNode symbol) {
        for (LrTransition<Lr1ItemSet> transition : getTrans(set)) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
    }*/

    public void addId(T set) {
        idMap.put(set, ++lastId);
        itemSets.add(set);
        set.closure();
    }

    int getId(LrItem item) {
        for (Map.Entry<T, Integer> entry : idMap.entrySet()) {
            if (entry.getKey().kernel.contains(item)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    public int getId(T itemSet) {
        if (idMap.containsKey(itemSet)) {
            return idMap.get(itemSet);
        }
        for (LrItem kernel : itemSet.kernel) {
            int id = getId(kernel);
            if (id != -1) {
                return id;
            }
        }
        return -1;
    }
}
