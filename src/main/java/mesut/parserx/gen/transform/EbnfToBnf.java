package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//transform ebnf to bnf
public class EbnfToBnf {

    public static boolean leftRecursive = true;//prefer left recursion on regex expansions
    public static boolean expand_or = true;//separate rule for each 'or' content
    public static boolean combine_or = false;//exclusive expand_or
    public static boolean rhsSequence = true;//make sure rhs always sequence
    Tree tree;//input ebnf
    Tree res;//output bnf
    Map<String, Integer> countMap = new HashMap<>();

    public EbnfToBnf(Tree tree) {
        this.tree = tree;
    }

    public static Tree transform(Tree input) {
        return new EbnfToBnf(input).transform();
    }

    public static Tree combineOr(Tree input) {
        Tree res = new Tree(input);
        res.rules.clear();
        for (Map.Entry<Name, Or> entry : combineOrMap(input).entrySet()) {
            RuleDecl decl = entry.getKey().makeRule();
            decl.rhs = entry.getValue().normal();
            res.addRule(decl);
        }
        return res;
    }

    public static LinkedHashMap<Name, Or> combineOrMap(Tree input) {
        //preserves rule order
        LinkedHashMap<Name, Or> map = new LinkedHashMap<>();
        for (RuleDecl decl : input.rules) {
            Or or = map.get(decl.ref());
            if (or == null) {
                or = new Or();
                map.put(decl.ref(), or);
            }
            or.add(decl.rhs);
        }
        return map;
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
        if (combine_or && expand_or) {
            throw new RuntimeException("expand_or and combine_or exclusive");
        }
        for (RuleDecl decl : tree.rules) {
            transformRhs(decl);
        }
        if (combine_or) {
            res = combineOr(res);
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

    Node transform(Group groupNode, RuleDecl decl) {
        //r = a (e1 e2) b;
        //r = a rg1 b;
        //rg1 = e1 e2;
        String newName = decl.name + getCount(decl.name);
        RuleDecl newDecl = new RuleDecl(newName);
        newDecl.rhs = transform(groupNode.node, newDecl);
        addRule(newDecl);
        return newDecl.ref();
    }

    Node transform(Or orNode, RuleDecl decl) {
        if (expand_or) {
            for (Node node : orNode) {
                RuleDecl newDecl = new RuleDecl(decl.name);
                newDecl.rhs = transform(node, newDecl);
                transformRhs(newDecl);
            }
        }
        else {
            Or or = new Or();
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

    Node transform(Regex regex, RuleDecl decl) {
        Node node = transform(regex.node, decl);
        regex.node = node;
        if (regex.isStar()) {
            //b* = € | b* b; left
            //b* = € | b b*; right
            Name ref = new Name(decl.name + getCount(decl.name));
            Node newNode;
            if (leftRecursive) {
                newNode = new Or(new Epsilon(), new Sequence(ref, node));
            }
            else {
                newNode = new Or(new Epsilon(), new Sequence(node, ref));
            }
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
        else if (regex.isPlus()) {
            //b+ = b | b b+; right
            //b+ = b | b+ b; left
            Name ref = new Name(decl.name + getCount(decl.name));
            Node newNode;
            if (leftRecursive) {
                newNode = new Or(regex.node, Sequence.of(ref, regex.node));
            }
            else {
                newNode = new Or(regex.node, Sequence.of(regex.node, ref));
            }
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
        else {
            //r? = € | a;
            Name ref = new Name(decl.name + getCount(decl.name));
            Node newNode = new Or(new Epsilon(), node);
            RuleDecl r = new RuleDecl(ref.name, newNode);
            r.rhs = transform(newNode, r);
            addRule(r);
            return ref;
        }
    }

}
