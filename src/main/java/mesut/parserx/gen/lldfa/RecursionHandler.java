package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap;

import java.util.*;

public class RecursionHandler {
    Tree tree;
    HashMap<Name, Name> afterMap;
    List<RuleDecl> toAdd;
    List<RuleDecl> all = new ArrayList<>();
    Map<Name, List<RuleDecl>> allMap = new LinkedHashMap<>();
    CountingMap<Name> idCounter = new CountingMap<>();
    RuleDecl curRule;

    public RecursionHandler(Tree tree) {
        this.tree = tree;
    }

    public static class Info {
        boolean isRec;
        boolean isState;
        boolean isTail;
        boolean isPrim;
    }

    void prepare() {
        //todo epsilons need to be resolved
    }

    public void handleAll() {
        for (var rule : tree.rules) {
            curRule = rule;
            afterMap = new HashMap<>();
            toAdd = new ArrayList<>();
            handleIndirect(rule);
            all.addAll(toAdd);
            allMap.put(rule.ref, toAdd);
        }
        for (var e : allMap.entrySet()) {
            var curRules = e.getValue();
            var orig = tree.getRule(e.getKey());
            orig.rhs = curRules.remove(0).rhs;
            orig.recInfo = new Info();
            orig.recInfo.isRec = true;
            curRules.sort((r1, r2) -> {
                if (r1 == r2) return 0;
                if (r1.recInfo.isPrim) {
                    if (r2.recInfo.isPrim) return r1.getName().compareTo(r2.getName());
                    return -1;
                }
                if (r2.recInfo.isPrim) return 1;
                if (r1.recInfo.isState) {
                    if (r2.recInfo.isState) return r1.getName().compareTo(r2.getName());
                    return -1;
                }
                if (r2.recInfo.isState) return 1;
                return r1.getName().compareTo(r2.getName());
            });
            for (int i = 0; i < curRules.size(); i++) {
                curRules.get(i).index = tree.rules.size() + i;
            }
            int pos = tree.rules.indexOf(orig);
            tree.rules.addAll(pos + 1, curRules);
        }
    }

    void handleIndirect(RuleDecl decl) {
        var set = FirstSet.firstSet(decl.rhs, tree);
        set.removeIf(n -> n.isToken);
        if (!set.contains(decl.ref)) return;

        //primary first
        var prims = new ArrayList<Node>();
        for (var sym : set) {
            var a = getAfter(sym);
            var after = sym.equals(decl.ref) ? new Regex(a, RegexType.OPTIONAL) : a;
            var prim = makePrimary(sym, decl.ref);
            if (sym.equals(decl.ref)) {
                prim.astInfo.isPrimary = true;
            }
            prims.add(new Sequence(prim, after));
        }
        toAdd.add(0, new RuleDecl("S" + decl.ref, new Or(prims)));
        //tails
        for (var e : afterMap.entrySet()) {
            var state = e.getValue();
            var rule = e.getKey();
            //collect all tails for rule
            var list = new ArrayList<Node>();
            for (var sym : set) {
                var a = getAfter(sym);
                var after = sym.equals(decl.ref) ? new Regex(a, RegexType.OPTIONAL) : a;
                var tail = makeTail(sym, rule);
                list.add(new Sequence(tail, after));
                if (sym.equals(decl.ref)) {
                    tail.astInfo.isPrimary = true;
                }
            }
            var newRule = new RuleDecl(state, Or.make(list));
            newRule.recInfo = new Info();
            newRule.recInfo.isState = true;
            newRule.retType = decl.retType;
            toAdd.add(newRule);
        }
    }

    private Name getAfter(Name sym) {
        if (afterMap.containsKey(sym)) {
            return afterMap.get(sym);
        }
        var id = idCounter.get(curRule.ref);
        var res = new Name(curRule.ref + "_" + id);
        res.args.add(sym);
        afterMap.put(sym, res);
        return res;
    }

    Name makeTail(Name rule, Name start) {
        var ref = new Name(rule.name);
        ref.args.add(start);
        if (all.stream().anyMatch(rd -> rd.ref.equals(ref))) {
            return ref;
        }
        var list = new ArrayList<Node>();
        var decl = tree.getRule(rule);
        if (decl.rhs.isOr()) {
            for (var ch : decl.rhs.asOr()) {
                var tail = transformTail(ch, start);
                if (tail != null) list.add(tail);
            }
        }
        else {
            var tail = transformTail(decl.rhs, start);
            if (tail != null) list.add(tail);
        }
        var newRule = new RuleDecl(ref, Or.make(list));
        newRule.recInfo = new Info();
        newRule.recInfo.isTail = true;
        newRule.retType = decl.retType;
        toAdd.add(newRule);
        return ref;
    }

    Node transformTail(Node node, Name start) {
        var seq = node.asSequence();
        if (!seq.get(0).equals(start)) {
            return null;
        }
        var res = new Sequence(seq.list.subList(1, seq.list.size()));
        var arg = new Name(start.name);
        arg.astInfo = seq.get(0).astInfo.copy();//always true?
        if (start.equals(arg)) {
            arg.astInfo.isFactored = true;
        }
        arg.args.add(start);
        res.list.add(0, arg);
        res.astInfo = seq.astInfo.copy();
        return res;
    }

    Name makePrimary(Name rule, Name start) {
        var res = new Name(rule.name + "_no_" + start);
        if (all.stream().anyMatch(rd -> rd.ref.equals(res))) {
            return res;
        }
        var list = new ArrayList<Node>();
        var decl = tree.getRule(rule);
        if (decl.rhs.isOr()) {
            for (var ch : decl.rhs.asOr()) {
                if (!FirstSet.start(ch, start, tree)) {
                    list.add(ch);
                }
            }
        }
        else {
            if (!FirstSet.start(decl.rhs, start, tree)) {
                list.add(decl.ref);
            }
        }
        var newRule = new RuleDecl(res, new Or(list));
        newRule.recInfo = new Info();
        newRule.recInfo.isPrim = true;
        newRule.retType = decl.retType;
        toAdd.add(newRule);
        return res;
    }

}
