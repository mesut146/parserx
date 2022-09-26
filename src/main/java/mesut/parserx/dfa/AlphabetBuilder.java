package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlphabetBuilder extends Transformer {
    Set<Range> ranges;
    List<Bracket> brackets;

    public AlphabetBuilder(Tree tree) {
        super(tree);
    }

    //normalize bracket by r1
    private boolean split(Range r1, Bracket bracket) {
        for (var r2 : ranges) {
            if (r1.equals(r2) || !r1.intersect(r2)) {
                continue;
            }
            var inter = Range.intersect(r1, r2);
            var me1 = Range.of(r1.start, inter.start - 1);
            var me2 = Range.of(inter.end + 1, r1.end);
            bracket.ranges.remove(r1);
            ranges.remove(r1);
            ranges.remove(r2);
            if (me1.isValid()) {
                bracket.ranges.add(me1);
                ranges.add(me1);
            }
            if (me2.isValid()) {
                bracket.ranges.add(me2);
                ranges.add(me2);
            }
            bracket.ranges.add(inter);
            ranges.add(inter);
            bracket.clear();
            bracket.addAll(bracket.ranges);
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
        for (char c : ch.value.toCharArray()) {
            var bracket = new Bracket();
            bracket.add(new Range(c, c));
            bracket.negate = true;
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

    //find all intersecting inputs and split them so that all of them becomes unique
    public void build() {
        ranges = new HashSet<>();
        brackets = new ArrayList<>();

        //first collect ranges
        transformTokens();

        //find intersecting ranges and split them
        outer:
        while (true) {
            for (var bracket : brackets) {
                for (var range : bracket.getRanges()) {
                    //if this range intersect other ranges
                    if (!range.isSingle()) {
                        if (split(range, bracket)) {
                            continue outer;
                        }
                    }
                }//for bracket
            }
            break;//found none break while
        }
        //finally, add all ranges to alphabet
        for (var bracket : brackets) {
            for (var range : bracket.getRanges()) {
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
}
