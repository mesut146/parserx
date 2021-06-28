package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;

//remove left recursions
public class LeftRecursive {
    public Tree tree;
    Map<String, Node> cache = new HashMap<>();

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public static Tree transform(Tree input) {
        LeftRecursive leftRecursive = new LeftRecursive(input);
        leftRecursive.process();
        return leftRecursive.tree;
    }

    public void process() {
        for (RuleDecl rule : tree.rules) {
            handleRule(rule);
        }
    }

    void handleRule(RuleDecl rule) {
        while (startr(rule.rhs, rule.ref())) {
            if (start(rule.rhs, rule.ref())) {
                //direct
                rule.rhs = removeDirect(rule.rhs, rule.ref());
            }
            else {
                //indirect
                rule.rhs = indirect(rule);
            }
        }
    }

    public Node indirect(RuleDecl rule) {
        Node node = rule.rhs.copy();
        Name ref = rule.ref();
        //cut last transition reaches rule
        Set<Name> set = Helper.first(node, tree, true, true, false);
        for (Name any : set) {
            if (any.equals(ref)) continue;
            //if any start with rule
            RuleDecl anyDecl = tree.getRule(any);
            Node rhs = anyDecl.rhs;
            if (start(rhs, ref)) {
                //cut transition
                rhs = subFirst(rhs, ref);
                rhs = removeDirect(rhs, any);
                anyDecl.rhs = rhs;
                //now any doesn't start with ref
            }
        }
        return node;
    }

    //make sure node doesnt start with ref
    Node subFirst(Node node, Name ref) {
        if (node.isOr()) {
            Or res = new Or();
            for (Node ch : node.asOr()) {
                if (start(ch, ref)) {
                    ch = subFirst(ch, ref);
                }
                res.add(ch);
            }
            return res;
        }
        else if (node.isSequence()) {
            Sequence res = new Sequence(node.asSequence().list);
            for (int i = 0; i < node.asSequence().size(); i++) {
                Node ch = res.get(i);
                if (start(ch, ref)) {
                    res.set(i, subFirst(ch, ref));
                    if (!Helper.canBeEmpty(ch, tree)) {
                        //go on if epsilon
                        break;
                    }
                }
            }
            return res;
        }
        else if (node.isName()) {
            if (node.equals(ref)) {
                //expand
                return tree.getRule(ref).rhs.copy();
            }
        }
        else if (node.isGroup()) {
            return new Group(subFirst(node.asGroup().node, ref)).normal();
        }
        else if (node.isRegex()) {
            return new Regex(subFirst(node.asRegex().node, ref), node.asRegex().type);
        }
        return node;
    }

    Node trimFirst(Sequence s) {
        return new Sequence(s.list.subList(1, s.size())).normal();
    }

    Node trimFirst(Or s) {
        return new Or(s.list.subList(1, s.size())).normal();
    }

    public Node removeDirect(Node node, Name ref) {
        if (!start(node, ref)) {
            return node;
        }
        SplitInfo info = split(node, ref);
        Node one = info.one.normal();
        Node tail;
        //extract tail
        if (one.isSequence()) {
            tail = trimFirst(one.asSequence());
        }
        else if (one.isOr()) {
            //multiple ones, extract all
            Or or = one.asOr();
            Or tmp = new Or();
            for (Node ch : or) {
                tmp.add(trimFirst(ch.asSequence()));
            }
            tail = tmp.normal();
        }
        else {
            throw new RuntimeException("invalid tail: " + one);
        }
        //a0 | A t,   a0 t*
        return removeDirect(new Sequence(info.zero, new Regex(tail, "*")), ref);
    }

    //split regex into proper left recursive version
    //R = R0 | R1 where R0 doesn't start with R and R1 start with R, R1 = R T
    public SplitInfo split(Node r, Name name) {
        Node zero = null;
        Or one = null;
        //info.eps = Helper.canBeEmpty(r, tree);
        if (r.isGroup()) {
            SplitInfo info = split(r.asGroup().node, name);
            zero = info.zero;
            one = info.one;
        }
        else if (r.isName()) {
            if (r.equals(name)) {
                one = new Or(r);
            }
            else {
                zero = r;
            }
        }
        else if (r.isRegex()) {
            Regex regex = r.asRegex();
            SplitInfo s = split(regex.node, name);
            if (regex.isOptional()) {
                if (start(r, name)) {
                    one = s.one;
                    zero = s.zero;
                }
                else {
                    zero = regex.node;
                }
            }
            else if (regex.isStar()) {
                if (s.zero != null) {
                    zero = makeSeq(s.zero, regex);
                }
                one = new Or(makeSeq(s.one, regex));
            }
            else if (regex.isPlus()) {
                Regex star = new Regex(regex.node, "*");
                if (s.zero != null) {
                    zero = makeSeq(s.zero, star);
                }
                one = new Or(makeSeq(s.one, star));
            }
        }
        else if (r.isOr()) {
            Or or = r.asOr();
            Or zero0 = new Or();
            one = new Or();
            for (Node ch : or) {
                if (start(ch, name)) {
                    SplitInfo s = split(ch, name);
                    one.add(s.one);
                    if (s.zero != null) {
                        zero0.add(s.zero);
                    }
                }
                else {
                    zero0.add(ch);
                }
            }
            zero = zero0.normal();
        }
        else if (r.isSequence()) {
            Sequence seq = r.asSequence();
            Node left = seq.first();
            Node right = trimFirst(seq);
            SplitInfo s1 = split(left, name);
            if (start(left, name)) {
                one = new Or(makeSeq(s1.one.normal(), right));
                if (start(right, name) && Helper.canBeEmpty(left, tree)) {
                    //right is also lr, so merge
                    one = new Or(makeOr(one, split(right, name).one));
                }
                if (s1.zero == null) {
                    SplitInfo s2 = split(right, name);
                    if (Helper.canBeEmpty(left, tree) && s2.zero != null) {
                        zero = right;
                    }
                }
                else {
                    zero = makeSeq(s1.zero, right);
                }
            }
            else {
                SplitInfo s2 = split(right, name);
                if (Helper.canBeEmpty(left, tree)) {
                    one = s2.one;
                }
                zero = new Sequence(s1.zero, right);
                if (s2.zero != null) {
                    zero = new Or(zero, new Sequence(left, s2.zero).normal()).normal();
                }
            }
        }
        else if (r.isString()) {
            zero = r;
        }
        else {
            throw new RuntimeException("invalid node: " + r.getClass() + " = " + r);
        }
        if (zero != null) zero = zero.normal();
        if (one != null) one = new Or(one.normal());
        return new SplitInfo(zero, one);
    }

    Node makeSeq(Node... all) {
        Sequence s = new Sequence();
        for (Node ch : all) {
            if (ch != null) {
                s.add(ch.normal());
            }
        }
        return s.normal();
    }

    Node makeOr(Node... all) {
        Or s = new Or();
        for (Node ch : all) {
            if (ch != null) {
                s.add(ch.normal());
            }
        }
        return s.normal();
    }

    boolean start(Node node, Name name) {
        return Helper.first(node, tree, false).contains(name);
    }

    boolean startr(Node node, Name name) {
        return Helper.first(node, tree, true).contains(name);
    }

    public static class SplitInfo {
        public Node zero;
        public Or one;

        public SplitInfo(Node zero, Or one) {
            this.zero = zero;
            this.one = one;
        }
    }

    class PathNode {
        String name;
        List<PathNode> next = new ArrayList<>();

        public PathNode(String name) {
            this.name = name;
        }

        PathNode makePath(Name prod) {
            return makePath(prod, new HashMap<Name, PathNode>());
        }

        PathNode makePath(Name name, Map<Name, PathNode> map) {
            if (map.containsKey(name)) return map.get(name);
            PathNode pathNode = new PathNode(name.name);
            map.put(name, pathNode);
            //get start nodes
            Set<Name> set = Helper.first(tree.getRule(name).rhs, tree, false, true, false);
            for (Name ch : set) {
                pathNode.next.add(makePath(ch, map));
            }
            return pathNode;
        }
    }

}
