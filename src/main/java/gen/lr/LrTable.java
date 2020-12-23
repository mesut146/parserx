package gen.lr;

import nodes.NameNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LrTable<V extends LrItem, T extends Lr0ItemSet<V>> {
    public List<LrTransition<T>>[] map = new List[100];
    int lastId = -1;
    List<T> itemSets = new ArrayList<>();
    Map<T, Integer> idMap = new HashMap<>();


    public void addTransition(T from, T to, NameNode symbol) {
        LrTransition<T> t = new LrTransition<>(from, to, symbol);
        List<LrTransition<T>> list = getTrans(t.from);
        list.add(t);
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

    int getId(T itemSet) {
        for (LrItem kernel : itemSet.kernel) {
            int id = getId(kernel);
            if (id != -1) {
                return id;
            }
        }
        return -1;
    }
}
