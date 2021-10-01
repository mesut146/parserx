package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LrDFA {
    public static boolean debugTransition = false;
    public List<LrTransition>[] map = new List[100];
    public List<Name> tokens = new ArrayList<>();
    public List<Name> rules = new ArrayList<>();
    int lastId = -1;
    List<LrItemSet> itemSets = new ArrayList<>();
    HashMap<Integer, LrItemSet> idMap = new HashMap<>();//state id -> item set

    public void addTransition(LrItemSet from, LrItemSet to, Name symbol) {
        LrTransition t = new LrTransition(from, to, symbol);
        List<LrTransition> list = getTrans(from);
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
            System.out.printf("%s -> %s by %s\n", getId(from), getId(to), symbol.name);
        }
    }

    void expand(int id) {
        if (id >= map.length) {
            List<LrTransition>[] tmp = new List[map.length * 2];
            System.arraycopy(map, 0, tmp, 0, map.length);
            map = tmp;
        }
    }

    public LrItemSet getSet(int id) {
        if (idMap.containsKey(id)) {
            return idMap.get(id);
        }
        throw new RuntimeException("can't find set from id:" + id);
    }

    public List<LrTransition> getTrans(LrItemSet set) {
        return getTrans(getId(set));
    }

    public List<LrTransition> getTrans(int id) {
        expand(id);
        List<LrTransition> list = map[id];
        if (list == null) {
            list = new ArrayList<>();
            map[id] = list;
        }
        return list;
    }

    //if there exist another transition from this
    public LrItemSet getTargetSet(LrItemSet from, Name symbol) {
        for (LrTransition tr : getTrans(from)) {
            if (tr.symbol.equals(symbol)) {
                return tr.to;
            }
        }
        return null;
    }

    public void addSet(LrItemSet set) {
        if (getId(set) != -1) {
            throw new RuntimeException("set already exists " + set);
        }
        set.stateId = ++lastId;
        idMap.put(set.stateId, set);
        itemSets.add(set);
    }

    public void setId(LrItemSet set, int id) {
        if (getId(set) != -1) {
            throw new RuntimeException("can't set id of already-existing set");
        }
        set.stateId = id;
        idMap.put(id, set);
    }

    public int getId(LrItemSet set) {
        return set.stateId;
    }

    public boolean exist(LrItemSet set) {
        for (LrItemSet other : itemSets) {
            if (other == set) {
                return true;
            }
        }
        return false;
    }

}
