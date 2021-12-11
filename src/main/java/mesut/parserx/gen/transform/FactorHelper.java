package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

import static mesut.parserx.gen.transform.FactorLoop.debugMethod;

public class FactorHelper {
    Tree tree;
    Factor factor;

    public FactorHelper(Tree tree, Factor factor) {
        this.tree = tree;
        this.factor = factor;
    }


    public Set<Name> loops(Node node) {
        Set<Name> res = new HashSet<>();
        if (node.isRegex()) {
            if (node.astInfo.isFactored) return null;
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                res.add(regex.node.asName());
                for (Name s : FirstSet.firstSet(regex.node.asName(), tree)) {
                    if (FirstSet.isEmpty(follow(regex.node, s), tree)) {
                        res.add(s);
                    }
                }
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return res;
            Name name = node.asName();
            if (name.isRule()) {
                return loops(tree.getRule(name).rhs);
            }
        }
        else if (node.isGroup()) {
            return loops(node.asGroup().node);
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            Node a = seq.first();
            Node b = Helper.trim(seq);

            res = loops(a);
            if (res != null) {
                if (FirstSet.canBeEmpty(a, tree)) {
                    res.addAll(loops(b));
                }
                return res;
            }
            else {
                if (FirstSet.canBeEmpty(a, tree)) {
                    return loops(b);
                }
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Node a = or.first();
            Node b = Helper.trim(or);
            res = loops(a);
            res.addAll(loops(b));
        }
        return res;
    }

    public Node follow(Node node, Name first) {
        if (debugMethod) System.out.println("follow node = " + node + ", first = " + first);
        //if (!Helper.start(node, first, tree)) return null;
        //if (node.astInfo.isFactored) return null;
        if (node.isSequence()) {
            Node A = node.asSequence().first();
            Node B = Helper.trim(node.asSequence());
            if (A.equals(first)) {
                return B;
            }
            //A B
            if (Helper.start(A, first, tree)) {
                Node fa = follow(A, first);
                if (Helper.canBeEmpty(A, tree) && Helper.start(B, first, tree)) {
                    //A_eps B | A_noe B
                    throw new RuntimeException("not yet");
                }
                if (fa.isEpsilon()) {
                    return B;
                }
                else {
                    return Sequence.make(fa, B);
                }
            }
            else {
                //A can be empty
                return follow(B, first);
            }
        }
        else if (node.isOr()) {
            Node A = node.asOr().first();
            Node B = Helper.trim(node.asOr());
            if (Helper.start(A, first, tree)) {
                Node fa = follow(A, first);
                if (Helper.start(B, first, tree)) {
                    return new Or(fa, follow(B, first));
                }
                else {
                    return fa;
                }
            }
            else {
                return follow(B, first);
            }
        }
        else if (node.isGroup()) {
            return follow(node.asGroup().node, first);
        }
        else if (node.isRegex()) {
            if (node.astInfo.isFactored) return null;
            Regex regex = node.asRegex();
            Node f = follow(regex.node, first);
            if (regex.isOptional()) {
                return f;
            }
            else if (regex.isPlus()) {
                //A+=A A*
                return new Sequence(f, new Regex(regex.node, "*"));
            }
            else {
                //A*=A A* | €
                return new Sequence(f, new Regex(regex.node, "*"));
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return null;
            if (node.equals(first)) {
                return new Epsilon();
            }
            Name name = node.asName();
            if (name.isToken) {
                throw new RuntimeException("invalid call to follow");
            }
            RuleDecl decl = tree.getRule(name);
            return follow(decl.rhs, first);
        }
        throw new RuntimeException("invalid node");
    }

    public FactorLoop.commonResult commons(Node a, Node b) {
        FactorLoop.commonResult res = new FactorLoop.commonResult();
        Set<Name> s1 = factor.first(a);
        Set<Name> s2 = factor.first(b);
        Set<Name> common = new HashSet<>(s1);
        common.retainAll(s2);
        if (common.isEmpty()) return null;
        if (a.isName() && common.contains(a.asName())) {
            res.name = a.asName();
            return res;
        }
        if (b.isName() && common.contains(b.asName())) {
            res.name = b.asName();
            return res;
        }
        if (a.isRegex() && a.asRegex().asName().equals(b)) {
            //a+ a
            res.isLoop = true;
            res.name = b.asName();
            return res;
        }
        if (b.isRegex() && b.asRegex().asName().equals(a)) {
            //a a+());
            res.isLoop = true;
            res.name = a.asName();
            return res;
        }
        List<Name> list = new ArrayList<>(common);
        //rule has higher priority
        Collections.sort(list, new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                if (o1.isRule()) {
                    return o2.isRule() ? 0 : -1;
                }
                else {
                    return 1;
                }
            }
        });

        //try loops
        for (Name name : list) {
            if (hasLoop(a, name) && hasLoop(b, name)) {
                if (name.isRule()) {
                    res.isLoop = true;
                    res.name = name;
                    return res;
                }
                else if (res.name == null) {
                    res.name = name;
                    return res;
                }
            }
        }
        res.name = list.get(0);
        return res;
    }

    public Name common(Node a, Node b) {
        Set<Name> s1 = factor.first(a);
        Set<Name> s2 = factor.first(b);
        Set<Name> common = new LinkedHashSet<>(s1);
        common.retainAll(s2);
        if (common.isEmpty()) return null;
        if (a.isName() && common.contains(a.asName())) {
            return a.asName();
        }
        if (b.isName() && common.contains(b.asName())) {
            return b.asName();
        }
        if (a.isRegex() && a.asRegex().node.asName().equals(b)) {
            return b.asName();
        }
        if (b.isRegex() && b.asRegex().node.asName().equals(a)) {
            return a.asName();
        }
        Name res = null;
        int max = 0;
        for (Name name : common) {
            if (name.isRule()) {
                //most token count wins
                int cur = FirstSet.tokens(name, tree).size();
                if (cur > max) {
                    res = name;
                    max = cur;
                }
            }
            else if (res == null) {
                res = name;
            }
        }
        return res;
    }

    public Name common(Set<Name> s1, Set<Name> s2) {
        Set<Name> common = new LinkedHashSet<>(s1);
        common.retainAll(s2);
        if (common.isEmpty()) return null;

        Name res = null;
        int max = 0;
        for (Name name : common) {
            if (name.isRule()) {
                //most token count wins
                int cur = FirstSet.tokens(name, tree).size();
                if (cur > max) {
                    res = name;
                    max = cur;
                }
            }
            else if (res == null) {
                res = name;
            }
        }
        return res;
    }

    boolean hasLoop(Node node, Name sym) {
        if (debugMethod) System.out.println("hasLoop node = " + node + ", sym = " + sym);

        if (!FirstSet.start(node, sym, tree)) return false;
        if (node.isRegex()) {
            if (node.astInfo.isFactored) return false;
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                if (regex.node.equals(sym)) {
                    return true;
                }
                else {
                    //A* -> a*
                    //follow of a can be empty
                    Node f = follow(regex.node, sym);
                    return FirstSet.canBeEmpty(f, tree);
                }
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return false;
            Name name = node.asName();
            if (name.isRule()) {
                //todo recursion
                return hasLoop(tree.getRule(name).rhs, sym);
            }
            return false;
        }
        else if (node.isGroup()) {
            return hasLoop(node.asGroup().node, sym);
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            Node a = seq.first();
            Node b = Helper.trim(seq);
            //return hasLoop(a, sym) || Helper.canBeEmpty(a, tree) && hasLoop(b, sym);
            if (hasLoop(a, sym)) {
                return true;
            }
            else {
                if (FirstSet.canBeEmpty(a, tree)) {
                    return hasLoop(b, sym);
                }
                else {
                    return false;
                }
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Node a = or.first();
            Node b = Helper.trim(or);
            return hasLoop(a, sym) || hasLoop(b, sym);
        }
        return false;
    }
}
