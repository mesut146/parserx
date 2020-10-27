package gen;

import nodes.*;
import nodes.RuleDecl;

import java.util.HashMap;
import java.util.Map;

//transform ebnf to bnf
public class EbnfTransformer {

    Tree tree;//in ebnf
    Tree res;//out bnf
    Map<String, Integer> countMap = new HashMap<>();
    public static boolean leftRecursive = true;
    public static boolean expand_or = true;

    public EbnfTransformer(Tree tree) {
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
        res = new Tree(tree);//result tree

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

    Node transform(GroupNode groupNode, RuleDecl decl) {
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
        if (expand_or) {
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
        for (Node node : sequence) {
            res.add(transform(node, decl));
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res;
    }

    Node transform(RegexNode regexNode, RuleDecl decl) {
        regexNode.node = transform(regexNode.node, decl);
        if (regexNode.isStar()) {
            //r = a b*
            //b* = E | b* b;
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "*");
            //declare
            RuleDecl d1 = new RuleDecl(nameNode.name);
            if (leftRecursive) {
                d1.rhs = transform(new OrNode(new EmptyNode(), new Sequence(nameNode, regexNode.node)), d1);
            }
            else {
                d1.rhs = transform(new OrNode(new EmptyNode(), new Sequence(regexNode.node, nameNode)), d1);
            }
            addRule(d1);
            return nameNode;
        }
        else if (regexNode.isPlus()) {
            //b+ = b b*;
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "+");
            RegexNode star = new RegexNode();
            star.setType("*");
            star.node = regexNode.node;

            RuleDecl expansion = new RuleDecl(nameNode.name);
            expansion.rhs = transform(new Sequence(regexNode.node, transform(star, expansion)), expansion);
            addRule(expansion);
            return nameNode;
        }
        else if (regexNode.isOptional()) {
            //r = a?;
            //r = E | a;
            NameNode nameNode = new NameNode(decl.name + "_" + getCount(decl.name) + "?");
            addRule(new RuleDecl(nameNode.name, new OrNode(new EmptyNode(), regexNode.node)));
            return nameNode;
        }
        throw new RuntimeException("invalid regex: " + regexNode);
    }

}
