package mesut.parserx.gen.transform;

import mesut.parserx.gen.Copier;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//remove left recursions by substitutions
public class LeftRecursive {
    public Tree tree;

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public static Tree transform(Tree input) {
        LeftRecursive lr = new LeftRecursive(input);
        lr.process();
        return lr.tree;
    }

    public static void dot(Tree tree, Writer writer) {
        PrintWriter w = new PrintWriter(writer);
        w.println("digraph G{");
        w.println("rankdir = TB;");
        for (RuleDecl decl : tree.rules) {
            Set<Name> set = FirstSet.firstSet(decl.rhs, tree);
            for (Name sym : set) {
                if (FirstSet.start(sym, decl.ref, tree)) {
                    w.printf("%s -> %s\n", decl.baseName(), sym.name);
                }
            }
        }
        w.println("}");
        w.flush();
    }

    public void process() {
        for (RuleDecl rule : tree.rules) {
            handleRule(rule);
        }
    }

    public void normalizeIndirects() {
        for (RuleDecl rule : tree.rules) {
            if (startr(rule.rhs, rule.ref) && !start(rule.rhs, rule.ref)) {
                //indirect
                cutIndirect(rule);
            }
        }
    }

    void handleRule(RuleDecl rule) {
        while (startr(rule.rhs, rule.ref)) {
            if (start(rule.rhs, rule.ref)) {
                //direct
                rule.rhs = removeDirect(rule.rhs, rule.ref);
            }
            else {
                //indirect
                rule.rhs = indirect(rule);
            }
        }
    }

    Set<Name> onlyRules(Node node) {
        return FirstSet.firstSet(node, tree).stream().filter(Name::isRule).collect(Collectors.toSet());
    }

    public Node indirect(RuleDecl rule) {
        Node node = rule.rhs.copy();
        Name ref = rule.ref;
        //cut last transition that reaches rule
        Set<Name> set = onlyRules(node);
        for (Name any : set) {
            if (any.equals(ref)) continue;
            //if any start with rule
            RuleDecl anyDecl = tree.getRule(any);
            Node rhs = anyDecl.rhs;
            if (start(rhs, ref)) {
                //cut transition
                rhs = subFirst(rhs, ref);
                rhs = removeDirect(rhs, any);
                anyDecl.rhs = rhs;
                //now any doesn't start with ref
            }
        }
        return node;
    }

    public void cutIndirect(RuleDecl rule) {
        Name ref = rule.ref;
        //cut last transition that reaches rule
        Set<Name> set = onlyRules(rule.rhs);
        for (Name any : set) {
            if (any.equals(ref)) continue;
            //if any start with rule
            RuleDecl anyDecl = tree.getRule(any);
            Node rhs = anyDecl.rhs;
            if (start(rhs, ref)) {
                //cut transition
                anyDecl.rhs = subFirst(rhs, ref);
            }
        }
    }

    //make sure node doesn't start with ref
    public Node subFirst(Node node, Name ref) {
        if (node.isOr()) {
            List<Node> res = new ArrayList<>();
            for (Node ch : node.asOr()) {
                if (start(ch, ref)) {
                    ch = subFirst(ch, ref);
                }
                res.add(ch);
            }
            return Or.make(res).withAst(node);
        }
        else if (node.isSequence()) {
            Sequence res = new Sequence(node.asSequence().list);
            res.astInfo = node.astInfo.copy();
            for (int i = 0; i < node.asSequence().size(); i++) {
                Node ch = res.get(i);
                if (start(ch, ref)) {
                    ch = Or.wrap(subFirst(ch, ref));
                    res.set(i, ch);
                    if (!FirstSet.canBeEmpty(ch, tree)) {
                        //go on if epsilon
                        break;
                    }
                }
            }
            return res;
        }
        else if (node.isName()) {
            if (node.equals(ref)) {
                //substitute
                RuleDecl decl = tree.getRule(ref);
                Node copy = new Copier(tree).transformNode(decl.rhs, null);
                Group sub = new Group(copy);

                sub.astInfo.which = node.astInfo.which;
                sub.astInfo.varName = "res2";
                sub.astInfo.nodeType = decl.retType;
                sub.astInfo.outerVar = node.astInfo.outerVar;
                update(sub.node, "res", "res2");
                return sub;
            }
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            group.node = subFirst(group.node, ref);
            return group;
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            regex.node = subFirst(regex.node, ref);
            return regex;
        }
        return node;
    }

    void update(Node node, String from, String to) {
        if (node.isOr()) {
            for (Node ch : node.asOr()) {
                if (ch.astInfo.outerVar.equals(from)) {
                    ch.astInfo.outerVar = to;
                }
            }
        }
        else {
            if (Objects.equals(node.astInfo.outerVar, from)) {
                node.astInfo.outerVar = to;
            }
        }
    }

    public Node removeDirect(Node node, Name ref) {
        if (!start(node, ref)) {
            return node;
        }
        SplitInfo info = split(node, ref);
        Node one = info.one;
        Node tail;
        //extract tail
        if (one.isSequence()) {
            tail = Helper.trim(one.asSequence());
        }
        else if (one.isOr()) {
            //multiple ones, extract all
            Or or = one.asOr();
            List<Node> tmp = new ArrayList<>();
            for (Node ch : or) {
                tmp.add(Helper.trim(ch.asSequence()));
            }
            tail = Or.make(tmp);
        }
        else {
            throw new RuntimeException("invalid tail: " + one);
        }
        //a0 | A t,   a0 t*
        return removeDirect(new Sequence(info.zero, new Regex(tail, RegexType.STAR)), ref);
    }

    //split regex into proper left recursive version
    //R = R0 | R1 where R0 doesn't start with R and R1 start with R, R1 = R T
    public SplitInfo split(Node r, Name name) {
        Node zero = null;
        Node one = null;
        if (r.isGroup()) {
            SplitInfo info = split(r.asGroup().node, name);
            zero = info.zero;
            one = info.one;
        }
        else if (r.isName()) {
            if (r.equals(name)) {
                one = new Or(r);
            }
            else {
                zero = r;
            }
        }
        else if (r.isRegex()) {
            Regex regex = r.asRegex();
            SplitInfo s = split(regex.node, name);
            if (regex.isOptional()) {
                if (start(r, name)) {
                    one = s.one;
                    zero = s.zero;
                }
                else {
                    zero = regex.node;
                }
            }
            else if (regex.isStar()) {
                if (s.zero != null) {
                    zero = makeSeq(s.zero, regex);
                }
                one = new Or(makeSeq(s.one, regex));
            }
            else if (regex.isPlus()) {
                Regex star = new Regex(regex.node, RegexType.STAR);
                if (s.zero != null) {
                    zero = makeSeq(s.zero, star);
                }
                one = new Or(makeSeq(s.one, star));
            }
        }
        else if (r.isOr()) {
            Or or = r.asOr();
            List<Node> zeros = new ArrayList<>();
            List<Node> ones = new ArrayList<>();
            for (Node ch : or) {
                if (start(ch, name)) {
                    SplitInfo s = split(ch, name);
                    ones.add(s.one);
                    if (s.zero != null) {
                        zeros.add(s.zero);
                    }
                }
                else {
                    zeros.add(ch);
                }
            }
            one = Or.make(ones);
            zero = Or.make(zeros);
        }
        else if (r.isSequence()) {
            Sequence seq = r.asSequence();
            Node left = seq.first();
            Node right = Helper.trim(seq);
            SplitInfo s1 = split(left, name);
            if (start(left, name)) {
                one = new Or(makeSeq(s1.one, right));
                if (start(right, name) && FirstSet.canBeEmpty(left, tree)) {
                    //right is also lr, so merge
                    one = new Or(makeOr(one, split(right, name).one));
                }
                if (s1.zero == null) {
                    SplitInfo s2 = split(right, name);
                    if (FirstSet.canBeEmpty(left, tree) && s2.zero != null) {
                        zero = right;
                    }
                }
                else {
                    zero = makeSeq(s1.zero, right);
                }
            }
            else {
                SplitInfo s2 = split(right, name);
                if (FirstSet.canBeEmpty(left, tree)) {
                    one = s2.one;
                }
                zero = new Sequence(s1.zero, right);
                if (s2.zero != null) {
                    zero = new Or(zero, new Sequence(left, s2.zero));
                }
            }
        }
        else if (r.isString()) {
            zero = r;
        }
        else {
            throw new RuntimeException("invalid node: " + r.getClass() + " = " + r);
        }
        //if (zero != null) zero = zero;
        //if (one != null) one = new Or(one);
        return new SplitInfo(zero, one);
    }

    Node makeSeq(Node... all) {
        List<Node> list = new ArrayList<>();
        for (Node ch : all) {
            if (ch != null) {
                list.add(ch);
            }
        }
        return Sequence.make(list);
    }

    Node makeOr(Node... all) {
        List<Node> list = new ArrayList<>();
        for (Node ch : all) {
            if (ch != null) {
                list.add(ch);
            }
        }
        return Or.make(list);
    }

    boolean start(Node node, Name name) {
        return FirstSet.start(node, name, tree);
    }

    boolean startr(Node node, Name name) {
        return FirstSet.start(node, name, tree);
    }

    public static class SplitInfo {
        public Node zero;
        public Node one;

        public SplitInfo(Node zero, Node one) {
            this.zero = zero;
            this.one = one;
        }
    }

}
