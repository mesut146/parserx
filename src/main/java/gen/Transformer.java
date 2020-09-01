package gen;

import nodes.*;
import rule.EmptyRule;
import rule.RuleDecl;

import java.util.HashMap;
import java.util.Map;

//transform ebnf to bnf
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
            if (rhs.isGroup()) {//unnecessary group
                newDecl.rhs = rhs.asGroup().rhs;
            }
            else {
                newDecl.rhs = transform(rhs, decl);
            }
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
        String nname = decl.name + "_g" + getCount(decl.name);
        RuleDecl newDecl = new RuleDecl(nname);
        newDecl.rhs = transform(rhs, newDecl);
        addRule(newDecl);
        return new NameNode(nname);
    }

    Node transform(OrNode orNode, RuleDecl decl) {
        if (Config.expand_or) {
            for (Node node : orNode) {
                RuleDecl newDecl = new RuleDecl();
                newDecl.name = decl.name;
                newDecl.rhs = transform(node, newDecl);
                addRule(newDecl);
            }
        }
        else {
            OrNode or = new OrNode();
            for (Node node : orNode) {
                or.add(transform(node, decl));
            }
            return or;
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
            RuleDecl d1 = new RuleDecl(nameNode.name);
            d1.rhs = transform(new Sequence(nameNode, regexNode.node), d1);
            res.addRule(new RuleDecl(nameNode.name, new EmptyRule()));
            addRule(d1);
            return nameNode;
        }
        else if (regexNode.plus) {
            //b+ = b b*;
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "+");
            RegexNode star = new RegexNode();
            star.star = true;
            star.node = regexNode.node;

            RuleDecl expansion = new RuleDecl(nameNode.name);
            expansion.rhs = transform(new Sequence(regexNode.node, transform(star, expansion)), expansion);
            addRule(expansion);
            return nameNode;
        }
        else if (regexNode.optional) {
            //r = a?;
            //r = ;//zero
            //r = a;//one
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "?");
            addRule(new RuleDecl(nameNode.name, new EmptyRule()));
            addRule(new RuleDecl(nameNode.name, regexNode.node));
            return nameNode;
        }
        throw new RuntimeException("invalid regex: " + regexNode);
    }

}
