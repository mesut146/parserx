package gen;

import nodes.*;

import java.util.HashMap;
import java.util.Map;

//bnf to ebnf
public class EbnfTransformer extends Transformer {
    Tree tree;
    Tree res;
    boolean modified = false;

    public EbnfTransformer(Tree tree) {
        this.tree = tree;
    }

    static Node hasEps(OrNode or) {
        OrNode res = new OrNode();
        for (Node node : or) {
            if (!node.isEmpty()) {
                res.add(node);
            }
        }
        if (res.size() == or.size()) {
            return null;
        }
        return res.normal();
    }

    public Tree transform() {
        //res = new Tree(tree);
        tree = new EpsilonHandler(tree).handleEps();
        handleRec();
        return res;
    }

    private void forOr(RuleDecl rule) {
        Node node = rule.rhs;
        if (!node.isOr()) return;

        OrNode or = node.asOr();
        //rec
        for (int i = 0; i < or.size(); i++) {
            Node ch = or.get(i);
            if (ch.isSequence()) {
                Sequence seq = ch.asSequence();
                if (seq.first().asName().name.equals(rule.name)) {
                    modified = true;
                    //left
                    OrNode restOr = new OrNode();
                    //normal nodes
                    for (int j = 0; j < or.size(); j++) {
                        if (j == i) continue;
                        restOr.add(or.get(j));
                    }
                    Node tail = new Sequence();
                    for (int j = 1; j < seq.size(); j++) {
                        tail.asSequence().add(seq.get(j));
                    }
                    Node rest2 = new GroupNode(restOr.normal()).normal();
                    RegexNode regexNode = new RegexNode(tail.asSequence().normal(), "*");
                    Node rr = new Sequence(rest2, regexNode).normal();
                    res.addRule(new RuleDecl(rule.name, rr));
                    break;
                }
                else if (ch.asSequence().last().asName().name.equals(rule.name)) {
                    //right
                }
            }
        }
    }

    //merge ors
    void handleOrs() {
        Map<String, OrNode> map = new HashMap<>();
        for (RuleDecl decl : tree.rules) {
            OrNode or = map.get(decl.name);
            if (or == null) {
                or = new OrNode();
                map.put(decl.name, or);
            }
            or.add(decl.rhs);
        }
        for (String key : map.keySet()) {
            res.addRule(new RuleDecl(key, map.get(key)));
        }
    }

    void handle(RuleDecl decl) {
        if (decl.rhs.isOr()) {
            forOr(decl);
        }
        else if (decl.rhs.isGroup()) {
            decl.rhs = decl.rhs.asGroup().node;
            handle(decl);
        }
        else if (decl.rhs.isSequence()) {
            Sequence s = decl.rhs.asSequence();
            Node first = s.get(0);
            if (first.isGroup()) {
                first = first.asGroup().node;
            }
            if (first.isOr()) {
                if (Helper.first(first, tree).contains(decl.ref())) {
                    modified = true;
                    info i = parseOr(first.asOr(), decl.ref());
                    Node rem = new Sequence(s.list.subList(1, s.size())).normal();
                    Node n = Sequence.of(i.rest, rem, new RegexNode(Sequence.of(i.tail, rem), "*"));
                    res.addRule(new RuleDecl(decl.name, n));
                }
                else {
                    res.addRule(decl);
                }
            }
            else {
                res.addRule(decl);
            }
        }
        else {
            res.addRule(decl);
        }
    }

    info parseOr(OrNode or, NameNode name) {
        info info = new info();
        OrNode rest = new OrNode();
        for (Node ch : or) {
            if (ch.isSequence()) {
                Sequence s = ch.asSequence();
                if (s.get(0).equals(name)) {
                    if (info.tail == null) {
                        info.tail = new Sequence(s.list.subList(1, s.size())).normal();
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

    void handleRec() {
        System.out.println(tree);
        modified = false;
        res = new Tree(tree);
        for (RuleDecl decl : tree.rules) {
            handle(decl);
        }
        if (modified) {
            //repeat
            tree = res;
            handleRec();
        }
    }

    static class info {
        Node tail, rest;
    }

    static class EpsilonHandler extends Transformer {
        Tree tree, res;
        Map<String, Node> map = new HashMap<>();

        public EpsilonHandler(Tree tree) {
            this.tree = tree;
            this.res = new Tree(tree);
        }

        //convert epsilons to '?'
        Tree handleEps() {
            //collect epslions
            for (RuleDecl decl : tree.rules) {
                Node node = decl.rhs;
                if (node.isOr()) {
                    OrNode or = node.asOr();
                    Node or2 = hasEps(or);
                    if (or2 != null) {
                        //can be optional
                        //replace references with 'e?'
                        if (or2.isSequence() || or2.isOr()) {
                            or2 = new GroupNode(or2);
                        }
                        map.put(decl.name, new RegexNode(or2, "?"));
                    }
                }
            }
            replace();
            return res;
        }

        void replace() {
            for (RuleDecl rule : tree.rules) {
                rule = transformRule(rule);
                if (rule != null) {
                    res.addRule(rule);
                }
            }
        }

        @Override
        public RuleDecl transformRule(RuleDecl decl) {
            if (!map.containsKey(decl.name)) {
                return super.transformRule(decl);
            }
            return null;
        }

        @Override
        public Node transformName(NameNode node) {
            return mapNode(node);
        }

        Node mapNode(NameNode nameNode) {
            if (map.containsKey(nameNode.name)) {
                return map.get(nameNode.name);
            }
            return nameNode;
        }
    }
}
