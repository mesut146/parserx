package mesut.parserx.gen.transform;

import mesut.parserx.gen.Copier;
import mesut.parserx.gen.lldfa.Normalizer;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//transform ebnf to bnf
public class EbnfToBnf extends Transformer {

    public static boolean leftRecursive = true;//prefer left recursion on regex expansions
    public static boolean expand_or = false;//separate rule for each 'or' content
    public static boolean combine_or = true;//exclusive expand_or
    Tree tree;//input ebnf
    Tree out;//output bnf
    CountingMap<String> countMap = new CountingMap<>();
    RuleDecl curRule;

    public EbnfToBnf(Tree tree) {
        this.tree = tree;
    }

    public static Tree transform(Tree input) {
        new Normalizer(input).normalize();
        return new EbnfToBnf(input).transform();
    }

    public static LinkedHashMap<Name, Node> combineOrMap(Tree input) {
        //LinkedHashMap preserves rule order
        LinkedHashMap<Name, List<Node>> map = new LinkedHashMap<>();
        for (RuleDecl decl : input.rules) {
            List<Node> or = map.computeIfAbsent(decl.ref, k -> new ArrayList<>());
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
        out = Copier.copyTree(tree);
        out.rules.clear();
        if (combine_or && expand_or) {
            throw new RuntimeException("expand_or and combine_or exclusive");
        }
        if (combine_or) {
            for (Map.Entry<Name, Node> entry : combineOrMap(tree).entrySet()) {
                out.addRule(new RuleDecl(entry.getKey(), entry.getValue()));
            }
        }
        for (var decl : tree.rules) {
            curRule = decl;
            transformRhs(decl);
        }

        //make rhs sequence
        for (var decl : out.rules) {
            if (!decl.rhs.isSequence()) {
                decl.rhs = new Sequence(decl.rhs);
            }
        }
        return out;
    }

    public void transformRhs(RuleDecl decl) {
        var rhs = decl.rhs.accept(this, null);
        if (rhs != null) {
            addRule(new RuleDecl(decl.ref, rhs));
        }
    }

    public Node visitOr(Or or, Void arg) {
        if (expand_or) {
            for (Node ch : or) {
                RuleDecl newDecl = new RuleDecl(curRule.baseName(), ch.accept(this, null));
                transformRhs(newDecl);
            }
            return null;
        } else {
            List<Node> list = new ArrayList<>();
            for (Node ch : or) {
                list.add(ch.accept(this, null));
            }
            return Or.make(list);
        }
    }

    public Node visitSequence(Sequence seq, Void arg) {
        List<Node> list = new ArrayList<>();
        for (Node ch : seq) {
            list.add(ch.accept(this, null));
        }
        return Sequence.make(list);
    }

    public Node visitRegex(Regex regex, Void arg) {
        Name ref = new Name(curRule.baseName() + countMap.get(curRule.baseName()));
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
                } else {
                    or.add(new Epsilon());
                }
                if (node.isOr()) {
                    for (Node ch : node.asOr()) {
                        addCh(or, ref, ch);
                    }
                } else {
                    addCh(or, ref, node);
                }
            } else {
                //name
                if (regex.isPlus()) {
                    or.add(regex.node);
                } else {
                    or.add(new Epsilon());
                }
                addCh(or, ref, regex.node);
            }
            RuleDecl r = new RuleDecl(ref, Or.make(or));
            r.rhs = r.rhs.accept(this, null);
            addRule(r);
            return ref;
        } else {
            //r? = € | a;
            Node newNode = new Or(new Epsilon(), regex.node);
            RuleDecl r = new RuleDecl(ref, newNode.accept(this, null));
            addRule(r);
            return ref;
        }
    }

    void addCh(List<Node> or, Node ref, Node other) {
        if (leftRecursive) {
            or.add(new Sequence(ref, other));
        } else {
            or.add(new Sequence(other, ref));
        }
    }

}
