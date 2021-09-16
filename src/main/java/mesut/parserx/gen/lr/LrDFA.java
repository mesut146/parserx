package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LrDFA<T extends LrItemSet> {
    public List<LrTransition<T>>[] map = new List[100];
    public List<Name> tokens = new ArrayList<>();
    public List<Name> rules = new ArrayList<>();
    int lastId = -1;
    List<T> itemSets = new ArrayList<>();
    Map<LrItemSet, Integer> idMap = new HashMap<>();//item set -> state id

    public void addTransition(T from, T to, Name symbol) {
        LrTransition<T> t = new LrTransition<>(from, to, symbol);
        List<LrTransition<T>> list = getTrans(from);
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
    }

    public LrItemSet getSet(int id) {
        for (Map.Entry<LrItemSet, Integer> entry : idMap.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("can't find set from id:" + id);
    }

    public List<LrTransition<T>> getTrans(LrItemSet set) {
        return getTrans(getId(set));
    }

    public List<LrTransition<T>> getTrans(int id) {
        List<LrTransition<T>> list = map[id];
        if (list == null) {
            list = new ArrayList<>();
            map[id] = list;
        }
        return list;
    }

    public void addId(T set) {
        if (idMap.containsKey(set)) {
            throw new RuntimeException("duplicate set " + set);
        }
        idMap.put(set, ++lastId);
        itemSets.add(set);
    }

    public void setId(LrItemSet set, int id) {
        if (getId(set) != -1) {
            throw new RuntimeException("can't set id of pre-existing set");
        }
        idMap.put(set, id);
    }

    public int getId(LrItemSet set) {
        for (Map.Entry<LrItemSet, Integer> s : idMap.entrySet()) {
            //== is needed bc kernel may change later so does hashcode
            if (s.getKey() == set) return s.getValue();
        }
        /*if (idMap.containsKey(set)) {
            return idMap.get(set);
        }*/
        return -1;
    }
}
