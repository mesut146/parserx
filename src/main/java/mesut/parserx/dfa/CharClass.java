package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharClass {
    public static int min = Character.MIN_VALUE;
    public static int max = Character.MAX_VALUE;

    //find all intersecting inputs and split them so that all of them becomes unique
    public static void makeDistinctRanges(Tree tree) {
        Set<Range> ranges = new HashSet<>();
        List<Bracket> brackets = new ArrayList<>();
        //first collect ranges
        for (TokenDecl token : tree.tokens) {
            walkNodes(token.regex, ranges, brackets);
        }
        //find intersecting ranges and split them
        outer:
        while (true) {
            for (Bracket bracket : brackets) {
                for (Range range : bracket.getRanges()) {
                    //if this range intersect other ranges
                    if (!range.isSingle()) {
                        if (split(range, ranges, bracket)) {
                            continue outer;
                        }
                    }
                }//for bracket
            }
            break;//found none break while
        }
        //finally add all ranges to alphabet
        for (Bracket bracket : brackets) {
            for (Range range : bracket.getRanges()) {
                tree.alphabet.addRegex(range);
            }
        }
        //add chars in strings that are distinct
        for (Range range : ranges) {
            Range real = tree.alphabet.findRange(range);
            if (real == null) {
                tree.alphabet.addRegex(range);
            }
        }
    }

    private static boolean split(Range r1, Set<Range> ranges, Bracket bracket) {
        for (Range r2 : ranges) {
            if (!r1.equals(r2) && r1.intersect(r2)) {
                Range inter = Range.intersect(r1, r2);
                Range me1 = Range.of(r1.start, inter.start - 1);
                Range me2 = Range.of(inter.end + 1, r1.end);
                //RangeNode he1 = RangeNode.of(r2.start, inter.start - 1);
                //RangeNode he2 = RangeNode.of(inter.end + 1, r2.end);
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
                //if (he1.isValid()) ranges.add(he1);
                //if (he2.isValid()) ranges.add(he2);

                bracket.ranges.add(inter);
                ranges.add(inter);
                bracket.clear();
                bracket.addAll(bracket.ranges);
                return true;
            }//for ranges
        }//for ranges
        return false;
    }

    static void walkNodes(Node node, Set<Range> ranges, List<Bracket> brackets) {
        if (node.isBracket()) {
            Bracket b = node.asBracket().normalize();
            ranges.addAll(b.ranges);
            brackets.add(b);
        }
        else if (node.isSequence()) {
            for (Node c : node.asSequence()) {
                walkNodes(c, ranges, brackets);
            }
        }
        else if (node.isDot()) {
            Bracket b = Dot.bracket;
            ranges.addAll(b.ranges);
            brackets.add(b);
        }
        else if (node.isString()) {
            //make range for each char in string
            String str = node.asString().value;
            for (char c : str.toCharArray()) {
                ranges.add(Range.of(c, c));
            }
        }
        else if (node.isGroup()) {
            walkNodes(node.asGroup().node, ranges, brackets);
        }
        else if (node.isRegex()) {
            walkNodes(node.asRegex().node, ranges, brackets);
        }
        else if (node.isOr()) {
            for (Node c : node.asOr()) {
                walkNodes(c, ranges, brackets);
            }
        }
    }
}
