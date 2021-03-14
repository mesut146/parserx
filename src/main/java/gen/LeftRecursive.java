package gen;

import nodes.*;


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
            if (start(rule.rhs, rule.ref())) {
                //direct
                System.out.println(rule);
                removeDirect(rule);
                modified = true;
            }
            else {
                //indirect
                System.err.println("indirect left recursion on " + rule);
            }
        }
        return rule;
    }

    public void indirect(RuleDecl rule) {
        Node node = rule.rhs;
        if (node.isOr()) {

        }
        else if (node.isSequence()) {

        }
    }

    Node trim(Sequence s) {
        return new Sequence(s.list.subList(1, s.size())).normal();
    }

    Node trim(OrNode s) {
        return new OrNode(s.list.subList(1, s.size())).normal();
    }

    public void removeDirect(RuleDecl rule) {
        if (!start(rule.rhs, rule.ref())) {
            return;
        }
        SplitInfo info = split(rule.rhs, rule.ref());
        Node tail = null;
        //extract tail
        if (info.one.isSequence()) {
            Sequence s = info.one.asSequence();
            tail = trim(s);
        }
        else if (info.one.isOr()) {
            //multiple ones, extract all
            OrNode or = info.one.asOr();
            OrNode or0 = new OrNode();
            for (Node ch : or) {
                //or0.add(new Sequence(ch.asSequence().list.subList(1, ch.asSequence().size())).normal());
                or0.add(trim(ch.asSequence()));
            }
            tail = or0.normal();
        }
        else {
            System.out.println("tail=" + info.one);
        }
        //a0 | A t,   a0 t*
        rule.rhs = new Sequence(info.zero, new RegexNode(tail, "*"));
    }

    //split regex into proper left recursive version
    //R = R0 | R1
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
                info.zero = Sequence.of(s.zero, regexNode).normal();
                info.one = Sequence.of(s.one, regexNode).normal();
            }
            else if (regexNode.isPlus()) {
                RegexNode star = new RegexNode(regexNode.node, "*");
                info.zero = Sequence.of(s.zero, star).normal();
                info.one = Sequence.of(s.one, star).normal();
            }
        }
        else if (r.isOr()) {
            OrNode or = r.asOr();
            Node left = or.get(0);
            Node right = trim(or);
            if (start(left, name)) {
                SplitInfo s = split(left, name);
                info.one = s.one;
                if (s.zero == null) {
                    info.zero = right;
                }
                else {
                    info.zero = new OrNode(s.zero, right).normal();
                }
            }
            else {
                SplitInfo s = split(right, name);
                if (start(right, name)) {
                    info.one = s.one;
                    info.zero = new OrNode(s.zero, left).normal();
                }
                else {
                    info.zero = new OrNode(s.zero, split(left, name).zero).normal();
                }

            }
        }
        else if (r.isSequence()) {
            Sequence seq = r.asSequence();
            Node left = seq.get(0);
            Node right = trim(seq);
            SplitInfo s1 = split(left, name);
            if (start(left, name)) {
                info.one = new Sequence(s1.one, right).normal();
                if (start(right, name) && Helper.canBeEmpty(left, tree)) {
                    //right also lr, so merge
                    info.one = new OrNode(info.one, split(right, name).one);
                }
                if (s1.zero == null) {
                    if (Helper.canBeEmpty(left, tree)) {
                        info.zero = right;
                    }
                }
                else {
                    info.zero = new Sequence(s1.zero, right).normal();
                }
            }
            else {
                SplitInfo s2 = split(right, name);
                if (Helper.canBeEmpty(left, tree)) {
                    info.one = s2.one;
                }
                OrNode o = new OrNode();
                o.add(new Sequence(s1.zero, right));
                if (s2.zero != null)
                    o.add(new Sequence(left, s2.zero).normal());
                info.zero = o.normal();
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

    public static class SplitInfo {
        public Node zero, one;
        boolean eps;
    }

}
