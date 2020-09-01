package gen;

import nodes.*;
import rule.EmptyRule;
import rule.RuleDecl;

import java.util.HashMap;
import java.util.Map;

public class Transformer {

    Tree tree;
    Tree res;
    int count = 0;
    Map<String, Integer> countMap = new HashMap<>();

    public Transformer(Tree tree) {
        this.tree = tree;
    }

    public Tree getRes() {
        return res;
    }

    int getCount(String name) {
        int cnt;
        if (countMap.containsKey(name)) {
            cnt = countMap.get(name);
            cnt++;
        }
        else {
            cnt = 0;
        }
        countMap.put(name, cnt);
        return cnt;
    }

    private void addRule(RuleDecl newDecl) {
        if (newDecl.rhs != null) {
            res.addRule(newDecl);
        }
    }

    public Tree transform(Tree tree) {
        res = new Tree();//result tree

        for (RuleDecl decl : tree.rules) {
            RuleDecl newDecl = new RuleDecl(decl.name);

            Node rhs = decl.rhs;
            newDecl.rhs = transform(rhs, decl);
            addRule(newDecl);
        }
        return res;
    }

    Node transform(Node node, RuleDecl decl) {
        if (node.isGroup()) {
            return transform(node.asGroup(), decl);
        }
        else if (node.isName()) {
            return node;
        }
        else if (node.isSequence()) {
            return transform(node.asSequence(), decl);
        }
        else if (node.isRegex()) {
            return transform(node.asRegex(), decl);
        }
        else if (node.isOr()) {
            return transform(node.asOr(), decl);
        }
        return node;
    }

    Node transform(GroupNode<Node> groupNode, RuleDecl decl) {
        //r = pre (e1 e2) end;
        //r = pre r_g end;
        //r_g = e1 e2;
        Node rhs = groupNode.rhs;
        if (!rhs.isOr() && !rhs.isSequence()) {
            return rhs;
        }
        rhs = transform(rhs, decl);
        String nname = decl.name + "_g" + getCount(decl.name);
        RuleDecl newDecl = new RuleDecl(nname);
        newDecl.rhs = rhs;
        addRule(newDecl);
        return new NameNode(nname);
    }

    Node transform(OrNode orNode, RuleDecl decl) {
        for (Node node : orNode) {
            RuleDecl newDecl = new RuleDecl();
            newDecl.name = decl.name;
            newDecl.rhs = transform(node, newDecl);
            addRule(newDecl);
        }
        return null;
    }


    Node transform(Sequence sequence, RuleDecl decl) {
        Sequence res = new Sequence();
        for (Node node : sequence.list) {
            res.add(transform(node, decl));
        }
        if (res.list.size() == 1) {
            return res.list.get(0);
        }
        return res;
    }

    Node transform(RegexNode regexNode, RuleDecl decl) {
        regexNode.node = transform(regexNode.node, decl);
        if (regexNode.star) {
            //r = a b*;
            //r = a b*;
            //b* = ;empty node means zero times
            //b* = b | b* b;//more
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "*");
            //declare
            RuleDecl empty = new RuleDecl(nameNode.name, new EmptyRule());
            RuleDecl d1 = new RuleDecl(nameNode.name);
            d1.rhs = transform(new OrNode(regexNode.node, new Sequence(nameNode, regexNode.node)), d1);
            res.addRule(empty);
            addRule(d1);
            return nameNode;
        }
        else if (regexNode.plus) {
            //b+ = b b*;
            NameNode nameNode = new NameNode(decl.name + "_" + count++ + "+");
            RegexNode star = new RegexNode();
            star.star = true;
            star.node = regexNode.node;

            RuleDecl expansion = new RuleDecl(nameNode.name, new Sequence(regexNode.node, transform(star, decl)));
            res.addRule(expansion);
            return nameNode;
        }
        else if (regexNode.optional) {
            //r = a?;
            //r = ;//zero
            //r = a;//one
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "?");
            res.addRule(new RuleDecl(nameNode.name, new EmptyRule()));
            res.addRule(new RuleDecl(nameNode.name, regexNode.node));
            return nameNode;
        }
        throw new RuntimeException("invalid regex: " + regexNode);
    }

}
