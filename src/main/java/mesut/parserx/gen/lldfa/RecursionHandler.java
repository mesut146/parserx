package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.*;

public class RecursionHandler {
    Tree tree;
    HashMap<Name, Name> afterMap = new HashMap<>();
    List<RuleDecl> toAdd = new ArrayList<>();
    List<RuleDecl> all = new ArrayList<>();
    Map<Name, List<RuleDecl>> allMap = new LinkedHashMap<>();
    int id = 1;

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

    public void all() {
        for (var rule : tree.rules) {
            afterMap.clear();
            toAdd = new ArrayList<>();
            handleIndirect(rule);
            all.addAll(toAdd);
            allMap.put(rule.ref, toAdd);
        }
        for (var e : allMap.entrySet()) {
            var cur = e.getValue();
            var orig = tree.getRule(e.getKey());
            orig.rhs = cur.remove(0).rhs;
            orig.recInfo = new Info();
            orig.recInfo.isRec = true;
            for (var r : cur) {
                tree.addRuleBelow(r, orig);
            }
        }
    }

    void handleIndirect(RuleDecl decl) {
        var set = FirstSet.firstSet(decl.rhs, tree);
        set.removeIf(n -> n.isToken);
        if (!set.contains(decl.ref)) return;

        //primary first
        var prims = new ArrayList<Node>();
        for (var sym : set) {
            var after = sym.equals(decl.ref) ? new Regex(getAfter(sym), RegexType.OPTIONAL) : getAfter(sym);
            prims.add(new Sequence(makePrimary(sym, decl.ref), after));
        }
        toAdd.add(0, new RuleDecl("S" + decl.ref, new Or(prims)));
        //tails
        for (var e : afterMap.entrySet()) {
            var state = e.getValue();
            var rule = e.getKey();
            //collect all tails for rule
            var list = new ArrayList<Node>();
            for (var sym : set) {
                var after = sym.equals(decl.ref) ? new Regex(getAfter(sym), RegexType.OPTIONAL) : getAfter(sym);
                list.add(new Sequence(makeTail(sym, rule), after));
            }
            var newRule = new RuleDecl(state, new Or(list));
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
        var res = new Name("S" + id++);
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
        var newRule = new RuleDecl(ref, new Or(list));
        newRule.recInfo = new Info();
        newRule.recInfo.isTail = true;
        //newRule.retType=
        toAdd.add(newRule);
        return ref;
    }

    Node transformTail(Node node, Name start) {
        var seq = node.asSequence();
        if (seq.get(0).equals(start)) {
            var res = new Sequence(seq.list.subList(1, seq.list.size()));
            var arg = new Name(start.name);
            if (start.equals(arg)) {
                arg.astInfo.isFactored = true;
            }
            arg.args.add(start);
            res.list.add(0, arg);
            return res;
        }
        return null;
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
        toAdd.add(newRule);
        return res;
    }

}
