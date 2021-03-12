package gen;

import nodes.*;

import java.rmi.registry.Registry;

//remove left recursions
public class LeftRecursive {
    public Tree res;
    public Tree tree;
    boolean modified;

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public void process() {
        this.res = new Tree(tree);
        System.out.println("pass");
        System.out.println(NodeList.join(tree.rules, "\n"));
        modified = false;
        for (RuleDecl rule : tree.rules) {
            res.addRule(handleRule(rule));
        }
        if (modified) {
            tree = res;
            process();
        }
    }

    RuleDecl handleRule(RuleDecl rule) {
        if (Helper.first(rule.rhs, tree, true).contains(rule.ref())) {
            //direct
            modified = true;
            rule = direct(rule);
        }
        else {
            //subs
        }
        return rule;
    }


    info parseOr(OrNode or, NameNode name) {
        info info = new info();
        OrNode rest = new OrNode();
        for (Node ch : or) {
            if (ch.isSequence()) {
                if (info.tail != null) {

                }
                Sequence s = ch.asSequence();
                int idx = find(s, name);
                if (idx != -1) {
                    if (idx > 0) {
                        info.prefix = new Sequence(s.list.subList(0, idx)).normal();
                    }
                    if (info.tail == null) {
                        info.tail = new Sequence(s.list.subList(idx + 1, s.size())).normal();
                    }
                }
            }
            else {
                rest.add(ch);
            }
        }
        info.rest = rest.normal();
        return info;
    }

    int find(Sequence s, NameNode name) {
        for (int i = 0; i < s.size(); i++) {
            if (Helper.first(s.get(i), tree, false).contains(name)) {
                return i;
            }
        }
        return -1;
    }

    private RuleDecl direct(RuleDecl rule) {
        //handle direct
        Node rhs = rule.rhs;
        if (rhs.isGroup()) {
            return direct(new RuleDecl(rule.name, rhs.asGroup().node));
        }
        else if (rhs.isOr()) {
            info i = parseOr(rhs.asOr(), rule.ref());
            if (i != null) {
                modified = true;
                rule = new RuleDecl(rule.name, Sequence.of(i.rest, new RegexNode(i.tail, "*")));
            }
        }
        else if (rhs.isSequence()) {
            Sequence s = rhs.asSequence();
            Node first = s.get(0);
            if (first.isGroup()) {
                first = first.asGroup().node;
            }
            if (first.isOr()) {
                if (Helper.first(first, tree, false).contains(rule.ref())) {
                    modified = true;
                    info i = parseOr(first.asOr(), rule.ref());
                    Node rem = new Sequence(s.list.subList(1, s.size())).normal();
                    Node n = Sequence.of(i.rest, rem, new RegexNode(Sequence.of(i.tail, rem), "*"));
                    rule = new RuleDecl(rule.name, n);
                }
            }
        }
        return rule;
    }

    public SplitInfo split(Node r, NameNode name) {
        SplitInfo info = new SplitInfo();
        info.eps = Helper.canBeEmpty(r, tree);
        if (r.isGroup()) {
            info = split(r.asGroup().node, name);
        }
        else if (r.isName()) {
            if (r.equals(name)) {
                info.one = r;
            }
            else {
                info.zero = r;
            }
        }
        else if (r.isRegex()) {
            RegexNode regexNode = r.asRegex();
            SplitInfo s = split(regexNode.node, name);
            if (regexNode.isOptional()) {
                if (start(r, name)) {
                    info.one = s.one;
                    info.zero = s.zero;
                }
                else {
                    info.zero = regexNode.node;
                }
            }
            else if (regexNode.isStar()) {
                info.zero = Sequence.of(s.zero, regexNode);
                info.one = Sequence.of(s.one, regexNode);
            }
            else if (regexNode.isPlus()) {
                RegexNode star = new RegexNode(regexNode.node, "*");
                info.zero = Sequence.of(s.zero, star);
                info.one = Sequence.of(s.one, star);
            }
        }
        else if (r.isOr()) {
            OrNode or = r.asOr();
            Node left = or.get(0);
            Node right = new OrNode(or.list.subList(1, or.size())).normal();
            if (start(left, name)) {
                SplitInfo s = split(left, name);
                info.one = s.one;
                info.zero = new OrNode(s.zero, right);
            }
            else {
                SplitInfo s = split(right, name);
                info.one = s.one;
                info.zero = new OrNode(s.zero, left);
            }
        }
        else if (r.isSequence()) {
            Sequence seq = r.asSequence();
            Node left = seq.get(0);
            Node right = new Sequence(seq.list.subList(1, seq.size())).normal();
            SplitInfo s1 = split(left, name);
            SplitInfo s2 = split(right, name);
            if (start(left, name)) {

                info.one = new Sequence(s1.one, right);
                info.zero = new Sequence(s1.zero, right);
            }
            else {
                if (Helper.canBeEmpty(left, tree)) {
                    //info.zero = new Sequence(left, s2.zero);
                }
                else {

                }
                info.one = s2.one;
                info.zero = new Sequence(left, s2.zero);
            }
        }
        else {
            throw new RuntimeException("invalid: " + r.getClass());
        }
        return info;
    }

    boolean start(Node node, NameNode name) {
        return Helper.first(node, tree, false).contains(name);
    }

    static class info {
        Node tail, rest, prefix;
    }

    public static class SplitInfo {
        public Node zero, one;
        boolean eps;
    }

}
