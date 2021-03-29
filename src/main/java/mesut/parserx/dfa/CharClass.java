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
        Set<RangeNode> ranges = new HashSet<>();//whole input set as ranges nodes
        List<Bracket> brackets = new ArrayList<>();
        for (TokenDecl decl : tree.getTokens()) {
            walkNodes(decl.regex, ranges, brackets);
        }
        //find intersecting ranges and split them
        outer:
        while (true) {
            for (Bracket bracket : brackets) {
                for (RangeNode r1 : bracket.getRanges()) {
                    //if this range intersect other ranges
                    if (!r1.isSingle()) {
                        if (split(r1, ranges, bracket)) {
                            continue outer;
                        }
                    }
                }//for bracket
            }
            break;//found none break while
        }//while
        for (Bracket bracket : brackets) {
            for (RangeNode rangeNode : bracket.getRanges()) {
                tree.alphabet.addRegex(rangeNode);
            }
        }
        //add chars in strings that are distinct
        for (RangeNode rangeNode : ranges) {
            RangeNode real = tree.alphabet.findRange(rangeNode);
            if (real == null) {
                tree.alphabet.addRegex(rangeNode);
            }
        }
    }

    private static boolean split(RangeNode r1, Set<RangeNode> ranges, Bracket bracket) {
        for (RangeNode r2 : ranges) {
            if (!r1.equals(r2) && r1.intersect(r2)) {
                RangeNode inter = RangeNode.intersect(r1, r2);
                RangeNode me1 = RangeNode.of(r1.start, inter.start - 1);
                RangeNode me2 = RangeNode.of(inter.end + 1, r1.end);
                //RangeNode he1 = RangeNode.of(r2.start, inter.start - 1);
                //RangeNode he2 = RangeNode.of(inter.end + 1, r2.end);
                bracket.rangeNodes.remove(r1);
                ranges.remove(r1);
                ranges.remove(r2);
                if (me1.isValid()) {
                    bracket.rangeNodes.add(me1);
                    ranges.add(me1);
                }
                if (me2.isValid()) {
                    bracket.rangeNodes.add(me2);
                    ranges.add(me2);
                }
                //if (he1.isValid()) ranges.add(he1);
                //if (he2.isValid()) ranges.add(he2);

                bracket.rangeNodes.add(inter);
                ranges.add(inter);
                bracket.clear();
                bracket.addAll(bracket.rangeNodes);
                return true;
            }//for ranges
        }//for ranges
        return false;
    }

    static void walkNodes(Node node, Set<RangeNode> ranges, List<Bracket> brackets) {
        //find all ranges and store them
        if (node.isBracket()) {
            Bracket b = node.asBracket().normalize();
            ranges.addAll(b.rangeNodes);
            brackets.add(b);
        }
        else if (node.isSequence()) {
            for (Node c : node.asSequence()) {
                walkNodes(c, ranges, brackets);
            }
        }
        else if (node.isDot()) {
            Bracket b = DotNode.bracket;
            ranges.addAll(b.rangeNodes);
            brackets.add(b);
        }
        else if (node.isString()) {
            //make range for each char in string
            String str = node.asString().value;
            for (char c : str.toCharArray()) {
                ranges.add(RangeNode.of(c, c));
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
