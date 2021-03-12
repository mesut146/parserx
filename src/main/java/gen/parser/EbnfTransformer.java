package gen.parser;

import gen.Helper;
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


    public Tree transform() {
        tree = new EpsilonTrimmer(tree).trim();
        handleRec();
        return res;
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
                if (Helper.first(first, tree,false).contains(decl.ref())) {
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

    static class info {
        Node tail, rest;
    }

}
