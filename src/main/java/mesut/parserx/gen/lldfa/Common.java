package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Common {
    LLDfaBuilder builder;
    RuleDecl rule;
    Tree tree;
    Options options;
    Map<Integer, Map<Node, List<Variable>>> localMap = new HashMap<>();
    Map<Integer, List<Variable>> paramMap = new HashMap<>();
    ItemSet curSet;

    public Common(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        tree = builder.tree;
        initLocals();
        initParams();
    }

    void initLocals() {
        for (var entry : builder.rules.entrySet()) {
            rule = tree.getRule(entry.getKey());
            for (var set : entry.getValue()) {
                initLocals(set);
            }
        }
    }

    private void initLocals(ItemSet set) {
        curSet = set;
        for (var tr : set.transitions) {
            if (tr.symbol.isName()) {
                var sym = tr.symbol.asName();
                var items = tr.pairs.stream().map(pair -> pair.origin).collect(Collectors.toList());
                //var group = groupByToken();
                var group = groupBy();
                var locals = genLocals(group.get(sym));
                //var locals = genLocals(items);
                localMap.computeIfAbsent(set.stateId, k -> new HashMap<>()).put(sym, locals);
            }
            else {
                //multi
                Item item = null;
                for (var it : set.all) {
                    if (it.dotPos != 0) continue;
                    var rest = Sequence.make(it.rhs.list.subList(it.dotPos, it.rhs.size()));
                    if (hasCommon(rest, tr.symbol)) {
                        item = it;
                        break;
                    }
                }
                if (item == null) continue;
                var locals = new ArrayList<Variable>();
                if (!item.isAlt()) {
                    locals.add(new Variable(item.rule.retType, "v0", item));
                }
                else {
                    //holder
                    var holder = new Variable(item.rule.retType, "v0", item.siblings);
                    locals.add(holder);
                    //alt
                    locals.add(new Variable(item.rule.rhs.astInfo.nodeType, "v1", item, holder));
                }
                localMap.computeIfAbsent(set.stateId, k -> new HashMap<>()).put(tr.symbol, locals);
            }
        }
    }

    boolean hasCommon(Node n1, Node n2) {
        Set<Name> s1 = FirstSet.tokens(n1, tree);
        Set<Name> s2 = FirstSet.tokens(n2, tree);
        Set<Name> set = new HashSet<>(s1);
        set.retainAll(s2);
        return !set.isEmpty();
    }

    //group items by la
    private Map<Name, List<Item>> groupByToken() {
        var groups = new HashMap<Name, List<Item>>();
        for (var item : curSet.all) {
            if (item.dotPos == item.rhs.size()) continue;
            var rest = Sequence.make(item.rhs.list.subList(item.dotPos, item.rhs.size()));
            var tokens = FirstSet.tokens(rest, tree);
            for (var token : tokens) {
                var list = groups.getOrDefault(token, new ArrayList<>());
                list.add(item);
                groups.put(token, list);
            }
        }
        return groups;
    }

    //group items by la
    Map<Name, List<Item>> groupBy() {
        var groups = new HashMap<Name, List<Item>>();
        for (var item : curSet.all) {
            if (item.dotPos == item.rhs.size()) continue;
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                var sym = ItemSet.sym(item.getNode(i));
                if (sym.isToken || !item.closured[i]) {
                    //common rule or token
                    var list = groups.getOrDefault(sym, new ArrayList<>());
                    list.add(item);
                    groups.put(sym, list);
                }
                else {
                    var rest = Sequence.make(item.rhs.list.subList(item.dotPos, item.rhs.size()));
                    var tokens = FirstSet.tokens(rest, tree);
                    //all token
                    for (var token : tokens) {
                        var list = groups.getOrDefault(token, new ArrayList<>());
                        list.add(item);
                        groups.put(token, list);
                    }
                }
                if (!FirstSet.canBeEmpty(item.getNode(i), tree)) {
                    break;
                }
            }
        }
        return groups;
    }

    private List<Variable> genLocals(List<Item> items) {
        var vars = new ArrayList<Variable>();
        var done = new HashSet<String>();
        int cnt = 0;
        var holderMap = new HashMap<Type, Variable>();
        for (var item : items) {
            if (item.dotPos > 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                var name = "v" + cnt++;
                vars.add(new Variable(item.rule.retType, name, item));
            }
            else {
                //holder
                if (!done.contains(item.rule.baseName())) {
                    var parentName = "v" + cnt++;
                    var holderVar = new Variable(item.rule.retType, parentName, item.siblings);
                    vars.add(holderVar);
                    done.add(item.rule.baseName());
                    holderMap.put(holderVar.type, holderVar);
                }
                //alt
                var name = "v" + cnt++;
                var holderVar = holderMap.get(item.rule.retType);
                vars.add(new Variable(item.rule.rhs.astInfo.nodeType, name, item, holderVar));

            }
        }
        return vars;
    }

    void initParams() {
        for (var entry : builder.rules.entrySet()) {
            rule = tree.getRule(entry.getKey());
            for (var set : entry.getValue()) {
                initParam(set);
            }
        }
    }

    void initParam(ItemSet set) {
        curSet = set;
        for (var tr : set.transitions) {
            if (tr.target.transitions.isEmpty()) continue;//inlined no params
            //if (paramMap.containsKey(tr.target.stateId)) continue;//already done
            if (tr.symbol.isName()) {
                initParamNormal(set, tr);
            }
            else {
                initParamMultiSym(set, tr);
            }
        }
    }

    private void initParamNormal(ItemSet set, LLTransition tr) {
        var sym = tr.symbol.asName();
        var locals = localMap.get(set.stateId).get(sym);
        locals = locals.stream().filter(v -> !v.isHolder()).collect(Collectors.toList());

        if (!paramMap.containsKey(tr.target.stateId)) {
            //check local overlaps param
            //check cur param overlaps param
            //transfer
            //init target params
            var params = new ArrayList<Variable>();
            //cur params+locals
            int cnt = 0;
            if (paramMap.containsKey(set.stateId)) {
                for (var param : paramMap.get(set.stateId)) {
                    if (isReduced(param)) continue;
                    params.add(makeParam(param, "p" + cnt++));
                }
            }
            for (var local : locals) {
                //overlap
                params.add(makeParam(local, "p" + cnt++));
            }
            paramMap.put(tr.target.stateId, params);
        }
    }

    private void initParamMultiSym(ItemSet set, LLTransition tr) {
        var params = new ArrayList<Variable>();
        //cur params+locals
        if (paramMap.containsKey(set.stateId)) {
            int cnt = 0;
            for (var param : paramMap.get(set.stateId)) {
                if (isReduced(param)) continue;
                params.add(makeParam(param, "p" + cnt++));
            }
        }
        paramMap.put(tr.target.stateId, params);
    }

    Variable makeParam(Variable from, String name) {
        Variable res;
        if (from.item == null) {
            res = new Variable(from.type, name, from.children);
        }
        else {
            res = new Variable(from.type, name, from.item, from.holder);
        }
        res.prevs.add(from);
        return res;
    }

    boolean isReduced(Variable param) {
        if (param.isHolder()) {
            //throw new IllegalStateException("prm can never be holder,it has to be an alt or normal item");
            for (var item : curSet.all) {
                if (!item.isReduce(tree)) continue;
                //prm is holder of local item
                for (var ch : param.children) {
                    if (sameItem(item, ch)) return true;
                }
            }
            return false;
        }
        for (var item : curSet.all) {
            if (!item.isReduce(tree)) continue;
            if (sameItem(item, param.item)) return true;//recursion forces reduce
        }
        return false;
    }

    boolean sameItem(Item i1, Item i2) {
        return i1.equals(i2) || hasCommonId(i1.ids, i2.ids);
    }

    boolean hasCommonId(Set<Integer> s1, Set<Integer> s2) {
        var tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        return !tmp.isEmpty();
    }

    Map<Set<Name>, List<Item>> collectReduces(ItemSet set) {
        var map = new HashMap<Set<Name>, List<Item>>();
        for (var item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (!item.isAlt()) continue;//skip normal
            var la = item.lookAhead;
            var list = map.getOrDefault(la, new ArrayList<>());
            list.add(item);
            map.put(la, list);
        }
        return map;
    }

    Name findSym(Item item, Name sym) {
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            var ch = item.getNode(i);
            if (ItemSet.sym(ch).equals(sym)) {
                return ItemSet.sym(ch);
            }
            if (!FirstSet.canBeEmpty(ch, tree)) {
                break;
            }
        }
        return null;
    }

    Variable getLocal(Item item, Node sym) {
        if (!localMap.containsKey(curSet.stateId)) return null;
        var map = localMap.get(curSet.stateId);
        if (map.containsKey(sym)) {
            for (var v : map.get(sym)) {
                if (v.isHolder() || !v.item.rule.equals(item.rule)) continue;
                if (sameItem(v.item, item)) {
                    return v;
                }
            }
        }
        return null;
    }

    Variable getParam(Item item) {
        if (!paramMap.containsKey(curSet.stateId)) return null;
        for (var v : paramMap.get(curSet.stateId)) {
            if (!v.isHolder() && v.item.rule.equals(item.rule) && sameItem(v.item, item)) {
                return v;
            }
        }
        return null;
    }

    String getBoth(Item item, Node sym) {
        return getBothVar(item, sym).name;
    }

    Variable getBothVar(Item item, Node sym) {
        var res = getLocal(item, sym);
        if (res == null) res = getParam(item);
        if (res == null) {
            throw new RuntimeException();
        }
        return res;
    }

    Variable holderVar(Item item, Node sym) {
        if (localMap.containsKey(curSet.stateId)) {
            for (var v : localMap.get(curSet.stateId).get(sym)) {
                if (v.isHolder() && v.children.contains(item)) {
                    return v;
                }
            }
        }
        if (paramMap.containsKey(curSet.stateId)) {
            for (var v : paramMap.get(curSet.stateId)) {
                if (v.isHolder() && v.children.contains(item)) {
                    return v;
                }
            }
        }
        return null;
    }
}
