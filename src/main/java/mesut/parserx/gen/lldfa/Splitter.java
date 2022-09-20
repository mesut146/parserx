package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//split a rule by itself
//PREFIX X SUFFIX | PRIM
public class Splitter extends BaseVisitor<Splitter.SplitHolder, Void> {
    Tree tree;
    Name rule;
    Map<Name, SplitHolder> cache = new HashMap<>();

    public Splitter(Tree tree, Name rule) {
        this.tree = tree;
        this.rule = rule;
    }

    public static SplitHolder split(Name rule, Tree tree) {
        var delc = tree.getRule(rule);
        var finder = new Splitter(tree, rule);
        var res = delc.rhs.accept(finder, null);
        res.rule = rule;
        return res;
    }

    public static class SplitHolder {
        Node prefix;
        Node suffix;
        Node primary;
        Name rule;
        RegexType type;

        public boolean isLeft(Tree tree) {
            if (prefix != null) {
                return FirstSet.canBeEmpty(prefix, tree);
            }
            return false;
        }

        public boolean isRight(Tree tree) {
            if (prefix != null) {
                return FirstSet.canBeEmpty(suffix, tree);
            }
            return false;
        }

        public boolean isMid() {
            return prefix != null;
        }

        @Override
        public String toString() {
            if (prefix != null) {
                if (primary != null) {
                    return String.format("prefix: %s suffix: %s primary: %s", prefix, suffix, primary);
                }
                else {
                    return String.format("prefix: %s suffix: %s", prefix, suffix);
                }
            }
            else {
                return String.format("primary: %s", primary);
            }
        }
    }


    @Override
    public SplitHolder visitGroup(Group group, Void arg) {
        return group.node.accept(this, arg);
    }

    @Override
    public SplitHolder visitOr(Or or, Void arg) {
        List<Node> prefix = new ArrayList<>();
        List<Node> suffix = new ArrayList<>();
        List<Node> prims = new ArrayList<>();
        for (var ch : or) {
            var sp = ch.accept(this, arg);
            if (sp.primary != null) {
                prims.add(sp.primary);
            }
            if (sp.prefix != null) {
                prefix.add(sp.prefix);
                suffix.add(sp.suffix);
            }
        }
        var res = new SplitHolder();
        if (!prefix.isEmpty()) {
            res.prefix = Or.make(prefix);
            res.suffix = Or.make(suffix);
        }
        if (!prims.isEmpty()) {
            res.primary = Or.make(prims);
        }
        return res;
    }

    @Override
    public SplitHolder visitRegex(Regex regex, Void arg) {
        var sp = regex.node.accept(this, arg);
        if (sp.prefix == null) {
            sp.primary = regex;
            return sp;
        }
        if (regex.isOptional()) {
            //P X S | PRIM | %eps
            if (sp.primary == null) {
                sp.primary = new Epsilon();
            }
            else {
                sp.primary = new Regex(sp.primary, RegexType.OPTIONAL);
            }
        }
        else if (regex.isStar()) {
            //A*=(P X S | PRIM)*
            sp.type = regex.type;
        }
        else {
            //A+=(P X S | PRIM)+
            sp.type = regex.type;
        }
        return sp;
    }

    @Override
    public SplitHolder visitSequence(Sequence seq, Void arg) {
        if (seq.size() == 1) {
            return seq.get(0).accept(this, arg);
        }
        //A B = (PA X SA | PRA) (PB X SB | PRB)
        var A = seq.first();
        var B = Helper.trim(seq);
        var sp1 = A.accept(this, arg);
        var sp2 = B.accept(this, arg);
        var res = new SplitHolder();
        if (sp1.prefix != null) {
            if (sp2.prefix != null) {
                //PA | (PA X SA | PRA) PB = PA | A PB
                res.prefix = new Or(sp1.prefix, new Sequence(A, sp2.prefix));
                //SA (PB X SB | PRB) | SB = SA B | SB
                res.suffix = new Or(sp2.suffix, new Sequence(sp1.suffix, B));
                if (sp1.primary != null && sp2.primary != null) {
                    res.primary = new Or(sp1.primary, sp2.primary);
                }
            }
            else {
                //(PA X SA | PRA) B
                res.prefix = sp1.prefix;
                res.suffix = new Sequence(sp1.suffix, B);
                if (sp1.primary != null) {
                    res.primary = Sequence.make(sp1.primary, B);
                }
            }
        }
        else {
            if (sp2.prefix != null) {
                //A (PB X SB | PRB)
                res.prefix = new Sequence(A, sp2.prefix);
                res.suffix = sp2.suffix;
                if (sp2.primary != null) {
                    res.primary = Sequence.make(A, sp2.primary);
                }
            }
            else {
                res.primary = new Sequence(A, B);
            }
        }
        return res;
    }

    @Override
    public SplitHolder visitName(Name name, Void arg) {
        if (cache.containsKey(name)) return cache.get(name);
        var res = new SplitHolder();
        cache.put(name, res);
        if (name.isToken) {
            res.primary = name;
        }
        else if (name.equals(rule)) {
            res.prefix = new Epsilon();
            res.suffix = new Epsilon();
        }
        else {
            var tmp = tree.getRule(name).rhs.accept(this, arg);
            if (tmp.prefix != null) {
                cache.put(name, tmp);
                return tmp;
            }
            else {
                res.primary = name;
            }
        }
        return res;
    }


}
