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

    NameNode first(Node node) {
        if (node.isName()) {
            return node.asName();
        }
        else if (node.isSequence()) {
            return node.asSequence().get(0).asName();
        }
        else if (node.isOr()) {
            return first(node.asOr().get(0));
        }
        throw new RuntimeException("first: " + node);
    }

    static class info {
        Node tail, rest, prefix;
    }

}
