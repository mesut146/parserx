package gen;

import nodes.*;

import java.util.HashMap;
import java.util.Map;

//transform ebnf to bnf
public class BnfTransformer {

    public static boolean leftRecursive = true;//make repetitions left recursive
    public static boolean expand_or = true;//separate rules for each or content
    public static boolean rhsSequence = true;//make sure rhs always sequence
    public static boolean expand_group = false;//expand group in place instead of separate rule
    Tree tree;//in ebnf
    Tree res;//out bnf
    Map<String, Integer> countMap = new HashMap<>();

    public BnfTransformer(Tree tree) {
        this.tree = tree;
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

    private void addRule(RuleDecl decl) {
        if (decl.rhs != null) {
            res.addRule(decl);
        }
    }

    public Tree transform() {
        res = new Tree(tree);//result tree

        for (RuleDecl decl : tree.rules) {
            transformRhs(decl);
        }
        return res;
    }

    public void transformRhs(RuleDecl decl) {
        Node rhs = transform(decl.rhs, decl);
        if (rhs != null) {
            if (rhsSequence && !rhs.isSequence()) {
                rhs = new Sequence(rhs);
            }
            addRule(new RuleDecl(decl.name, rhs));
        }
    }

    Node transform(Node node, RuleDecl decl) {
        if (node == null) return null;
        if (node.isGroup()) {
            node = transform(node.asGroup(), decl);
        }
        else if (node.isSequence()) {
            node = transform(node.asSequence(), decl);
        }
        else if (node.isRegex()) {
            node = transform(node.asRegex(), decl);
        }
        else if (node.isOr()) {
            node = transform(node.asOr(), decl);
        }
        return node;
    }

    Node transform(GroupNode groupNode, RuleDecl decl) {
        //r = pre (e1 e2) end;
        //r = pre r_g end;
        //r_g = e1 e2;
        Node rhs = groupNode.node;
        if (!rhs.isOr() && !rhs.isSequence()) {//simplify
            //todo remove this,normal() already does it
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
                transformRhs(newDecl);
                //addRule(newDecl);
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
        return res.normal();
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
