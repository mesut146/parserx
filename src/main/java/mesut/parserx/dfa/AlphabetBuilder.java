package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlphabetBuilder extends Transformer {
    Set<Range> ranges = new HashSet<>();
    List<Bracket> brackets = new ArrayList<>();

    public AlphabetBuilder(Tree tree) {
        super(tree);
    }

    //find all intersecting inputs and split them so that all of them becomes unique
    public void build() {
        //first collect ranges
        transformTokens();

        //find intersecting ranges and split them
        outer:
        while (true) {
            for (var bracket : brackets) {
                for (var range : bracket.ranges) {
                    //if this range intersect other ranges
                    if (!range.isSingle() && split(range, bracket)) {
                        continue outer;
                    }
                }//for bracket
            }
            break;//found none break while
        }
        //finally, add all ranges to alphabet
        for (var bracket : brackets) {
            for (var range : bracket.ranges) {
                tree.alphabet.addRegex(range);
            }
        }
        //add chars in strings that are distinct
        for (var range : ranges) {
            var real = tree.alphabet.findRange(range);
            if (real == null) {
                tree.alphabet.addRegex(range);
            }
        }
    }

    //normalize bracket by r1
    private boolean split(Range r1, Bracket bracket) {
        for (var r2 : ranges) {
            if (r1.equals(r2)) {
                continue;
            }
            var inter = Range.intersect(r1, r2);
            if (inter == null) continue;
            var me1 = Range.of(r1.start, inter.start - 1);
            var me2 = Range.of(inter.end + 1, r1.end);
            bracket.ranges.remove(r1);
            ranges.remove(r1);
            ranges.remove(r2);
            if (me1 != null) {
                bracket.ranges.add(me1);
                ranges.add(me1);
            }
            if (me2 != null) {
                bracket.ranges.add(me2);
                ranges.add(me2);
            }
            bracket.ranges.add(inter);
            ranges.add(inter);
            //bracket.clear();
            //bracket.addAll(bracket.ranges);
            return true;
        }//for ranges
        return false;
    }

    @Override
    public Node visitBracket(Bracket node, Void parent) {
        node.normalize();
        ranges.addAll(node.ranges);
        brackets.add(node);
        return node;
    }

    @Override
    public Node visitUntil(Until node, Void parent) {
        var ch = node.node.asString();
        transformNode(ch, parent);
        //negated sets
        char firstCh = ch.value.charAt(0);
        for (int i = 0; i < ch.value.length(); i++) {
            var bracket = new Bracket();
            bracket.negate = true;
            char c = ch.value.charAt(i);
            bracket.add(new Range(c, c));
            if (i > 0 && c != firstCh) {
                bracket.add(new Range(firstCh, firstCh));
            }
            node.brackets.add(bracket);
            transformNode(bracket, parent);
        }
        return node;
    }

    @Override
    public Node visitSub(Sub sub, Void arg) {
        sub.node.accept(this, arg);
        sub.string.accept(this, arg);
        return super.visitSub(sub, arg);
    }

    @Override
    public Node visitDot(Dot node, Void parent) {
        Bracket b = Dot.bracket;
        ranges.addAll(b.ranges);
        brackets.add(b);
        return node;
    }

    @Override
    public Node visitString(StringNode node, Void parent) {
        //make range for each char in string
        String str = node.value;
        for (char c : str.toCharArray()) {
            ranges.add(Range.of(c, c));
        }
        return node;
    }


}
