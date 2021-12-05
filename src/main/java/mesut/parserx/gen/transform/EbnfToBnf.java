package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//transform ebnf to bnf
public class EbnfToBnf {

    public static boolean leftRecursive = true;//prefer left recursion on regex expansions
    public static boolean expand_or = false;//separate rule for each 'or' content
    public static boolean combine_or = true;//exclusive expand_or
    public static boolean rhsSequence = true;//make sure rhs always sequence
    Tree tree;//input ebnf
    Tree out;//output bnf
    CountingMap<String> countMap = new CountingMap<>();
    String curRule;

    public EbnfToBnf(Tree tree) {
        this.tree = tree;
    }

    public static Tree transform(Tree input) {
        return new EbnfToBnf(input).transform();
    }

    public static Tree combineOr(Tree input) {
        Tree res = new Tree(input);
        res.rules.clear();
        for (Map.Entry<Name, Node> entry : combineOrMap(input).entrySet()) {
            RuleDecl decl = entry.getKey().makeRule();
            decl.rhs = entry.getValue();
            res.addRule(decl);
        }
        return res;
    }

    public static LinkedHashMap<Name, Node> combineOrMap(Tree input) {
        //LinkedHashMap preserves rule order
        LinkedHashMap<Name, List<Node>> map = new LinkedHashMap<>();
        for (RuleDecl decl : input.rules) {
            List<Node> or = map.get(decl.ref);
            if (or == null) {
                or = new ArrayList<>();
                map.put(decl.ref, or);
            }
            or.add(decl.rhs);
        }
        LinkedHashMap<Name, Node> res = new LinkedHashMap<>();
        for (Map.Entry<Name, List<Node>> entry : map.entrySet()) {
            res.put(entry.getKey(), Or.make(entry.getValue()));
        }
        return res;
    }

    private void addRule(RuleDecl decl) {
        if (decl.rhs != null) {
            out.addRule(decl);
        }
    }

    public Tree transform() {
        out = new Tree(tree);//result tree
        if (combine_or && expand_or) {
            throw new RuntimeException("expand_or and combine_or exclusive");
        }
        for (RuleDecl decl : tree.rules) {
            curRule = decl.baseName();
            transformRhs(decl);
        }
        if (combine_or) {
            out = combineOr(out);
        }
        return out;
    }

    public void transformRhs(RuleDecl decl) {
        Node rhs = transform(decl.rhs);
        if (rhs != null) {
            if (rhsSequence && !rhs.isSequence()) {
                rhs = new Sequence(rhs);
            }
            addRule(new RuleDecl(decl.ref, rhs));
        }
    }

    Node transform(Node node) {
        if (node == null) return null;
        if (node.isGroup()) {
            node = transform(node.asGroup());
        }
        else if (node.isSequence()) {
            node = transform(node.asSequence());
        }
        else if (node.isRegex()) {
            node = transform(node.asRegex());
        }
        else if (node.isOr()) {
            node = transform(node.asOr());
        }
        return node;
    }

    //todo remove this,use Normalizer instead
    Node transform(Group group) {
        //r = a (e1 e2) b;
        //r = a rg1 b;
        //rg1 = e1 e2;
        String newName = curRule + countMap.get(curRule);
        RuleDecl newDecl = new RuleDecl(newName, transform(group.node));
        addRule(newDecl);
        return newDecl.ref.copy();
    }

    Node transform(Or or) {
        if (expand_or) {
            for (Node ch : or) {
                RuleDecl newDecl = new RuleDecl(curRule, transform(ch));
                transformRhs(newDecl);
            }
        }
        else {
            List<Node> list = new ArrayList<>();
            for (Node node : or) {
                list.add(transform(node));
            }
            return Or.make(list);
        }
        return null;
    }

    Node transform(Sequence seq) {
        List<Node> list = new ArrayList<>();
        for (Node ch : seq) {
            list.add(transform(ch));
        }
        return Sequence.make(list);
    }

    Node transform(Regex regex) {
        Name ref = new Name(curRule + countMap.get(curRule));
        if (regex.isStar() || regex.isPlus()) {
            Node node;
            //b* = € | b* b; left
            //b* = € | b b*; right
            //b+ = b | b b+; right
            //b+ = b | b+ b; left
            List<Node> or = new ArrayList<>();

            if (regex.node.isGroup()) {
                Group group = regex.node.asGroup();
                //no need to make another rule
                //node = transform(group.node, decl);
                node = group.node;
                if (regex.isPlus()) {
                    or.add(node);
                }
                else {
                    or.add(new Epsilon());
                }
                if (node.isOr()) {
                    for (Node ch : node.asOr()) {
                        addCh(or, ref, ch);
                    }
                }
                else {
                    addCh(or, ref, node);
                }
            }
            else {
                //name
                if (regex.isPlus()) {
                    or.add(regex.node);
                }
                else {
                    or.add(new Epsilon());
                }
                addCh(or, ref, regex.node);
            }
            RuleDecl r = new RuleDecl(ref, Or.make(or));
            r.rhs = transform(r.rhs);
            addRule(r);
            return ref;
        }
        else {
            //r? = € | a;
            Node newNode = new Or(new Epsilon(), regex.node);
            RuleDecl r = new RuleDecl(ref, transform(newNode));
            addRule(r);
            return ref;
        }
    }

    void addCh(List<Node> or, Node ref, Node other) {
        if (leftRecursive) {
            or.add(new Sequence(ref, other));
        }
        else {
            or.add(new Sequence(other, ref));
        }
    }

}
