package gen;

import nodes.*;

import java.io.SequenceInputStream;

//remove left recursions
public class LeftRecursive {
    public Tree res;
    public Tree tree;

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public void process() {
        this.res = new Tree(tree);
        for (RuleDecl rule : tree.rules) {
            res.addRule(handleRule(rule));
        }
    }

    RuleDecl handleRule(RuleDecl rule) {
        while (startr(rule.rhs, rule.ref())) {
            if (start(rule.rhs, rule.ref())) {
                //direct
                removeDirect(rule);
            }
            else {
                //indirect
                indirect(rule);
            }
            System.out.println("removed\n" + rule);
        }
        return rule;
    }

    public void indirect(RuleDecl rule) {
        rule.rhs = replace(rule.rhs, rule.ref());
        //now it is in direct recursive form
        removeDirect(rule);
    }

    //substitute references that can start with name don't touch rest
    Node replace(Node node, final NameNode name) {
        if (node.isOr()) {
            OrNode or = node.asOr();
            for (int i = 0; i < or.size(); i++) {
                if (startr(or.get(i), name)) {
                    or.set(i, replace(or.get(i), name));
                }
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                if (startr(s.get(i), name)) {
                    s.set(i, replace(s.get(i), name));
                    if (!Helper.canBeEmpty(s.get(i), tree)) {
                        break;
                    }
                }
            }
        }
        else if (node.isName()) {
            if (!node.equals(name) && node.asName().isRule() && startr(node, name)) {
                //expand
                Node rr = tree.getRule(node.asName().name).rhs;//rec
                return replace(rr, name);
            }
        }
        else if (node.isGroup()) {
            return new GroupNode(replace(node.asGroup().node, name)).normal();
        }
        else if (node.isRegex()) {
            node.asRegex().node = replace(node.asRegex().node, name);
        }
        return node;
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
                if (s.zero != null) {
                    info.zero = makeSeq(s.zero, regexNode);
                }
                info.one = makeSeq(s.one, regexNode);
            }
            else if (regexNode.isPlus()) {
                RegexNode star = new RegexNode(regexNode.node, "*");
                if (s.zero != null) {
                    info.zero = makeSeq(s.zero, star);
                }
                info.one = makeSeq(s.one, star);
            }
        }
        else if (r.isOr()) {
            OrNode or = r.asOr();
            Node left = or.first();
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
                    info.zero = makeOr(s.zero, left);
                }
                else {
                    info.zero = makeOr(s.zero, split(left, name).zero);
                }

            }
        }
        else if (r.isSequence()) {
            Sequence seq = r.asSequence();
            Node left = seq.first();
            Node right = trim(seq);
            SplitInfo s1 = split(left, name);
            if (start(left, name)) {
                info.one = makeSeq(s1.one, right);
                if (start(right, name) && Helper.canBeEmpty(left, tree)) {
                    //right is also lr, so merge
                    info.one = makeOr(info.one, split(right, name).one);
                }
                if (s1.zero == null) {
                    if (Helper.canBeEmpty(left, tree)) {
                        info.zero = right;
                    }
                }
                else {
                    info.zero = makeSeq(s1.zero, right);
                }
            }
            else {
                SplitInfo s2 = split(right, name);
                if (Helper.canBeEmpty(left, tree)) {
                    info.one = s2.one;
                }
                info.zero = new Sequence(s1.zero, right);
                if (s2.zero != null) {
                    info.zero = new OrNode(info.zero, new Sequence(left, s2.zero).normal()).normal();
                }
            }
        }
        else {
            throw new RuntimeException("invalid: " + r.getClass());
        }
        return info;
    }

    Node makeSeq(Node... all) {
        Sequence s = new Sequence();
        for (Node ch : all) {
            if (ch != null) {
                s.add(ch);
            }
        }
        return s.normal();
    }

    Node makeOr(Node... all) {
        OrNode s = new OrNode();
        for (Node ch : all) {
            if (ch != null) {
                s.add(ch);
            }
        }
        return s.normal();
    }

    boolean start(Node node, NameNode name) {
        return Helper.first(node, tree, false).contains(name);
    }

    boolean startr(Node node, NameNode name) {
        return Helper.first(node, tree, true).contains(name);
    }

    public static class SplitInfo {
        public Node zero, one;
        boolean eps;
    }

}
