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
    static String starSuffix = "*", plusSuffix = "+", optSuffix = "?";
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
        //r = a (e1 e2) b;
        //r = a r_g b;
        //r_g = e1 e2;
        Node rhs = groupNode.node;
        String newName = decl.name + getCount(decl.name);
        RuleDecl newDecl = new RuleDecl(newName);
        newDecl.rhs = transform(rhs, newDecl);
        addRule(newDecl);
        return new NameNode(newName);
    }

    Node transform(OrNode orNode, RuleDecl decl) {
        if (expand_or) {
            for (Node node : orNode) {
                RuleDecl newDecl = new RuleDecl();
                newDecl.name = decl.name;
                newDecl.rhs = transform(node, newDecl);
                transformRhs(newDecl);
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
        Node node = transform(regexNode.node, decl);
        regexNode.node = node;
        if (regexNode.isStar()) {
            //b* = E | b* b; left
            //b* = E | b b*; right
            NameNode ref = new NameNode(decl.name + "_" + getCount(decl.name) + starSuffix);
            Node newNode;
            if (leftRecursive) {
                newNode = new OrNode(new EmptyNode(), new Sequence(ref, node));
            }
            else {
                newNode = new OrNode(new EmptyNode(), new Sequence(node, ref));
            }
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
        else if (regexNode.isPlus()) {
            //b+ = b | b b+; right
            //b+ = b | b+ b; left
            NameNode ref = new NameNode(decl.name + "_" + getCount(decl.name) + plusSuffix);
            Node newNode;
            if (leftRecursive) {
                newNode = new OrNode(regexNode.node, Sequence.of(ref, regexNode.node));
            }
            else {
                newNode = new OrNode(regexNode.node, Sequence.of(regexNode.node, ref));
            }
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
        else {
            //r = E | a;
            NameNode ref = new NameNode(decl.name + "_" + getCount(decl.name) + optSuffix);
            Node newNode = new OrNode(new EmptyNode(), node);
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
    }

}
