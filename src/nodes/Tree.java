package nodes;

import dfa.NFA;
import grammar.ParseException;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//the grammar file for both lexer and parser
public class Tree {

    List<TokenDecl> skip;
    List<TokenDecl> tokens;
    List<RuleDecl> rules;

    public Tree() {
        tokens = new ArrayList<>();
        rules = new ArrayList<>();
        skip = new ArrayList<>();
    }

    public void addToken(TokenDecl token) {
        tokens.add(token);
    }

    public void addSkip(TokenDecl token) {
        token.isSkip = true;
        skip.add(token);
    }

    public void addRule(RuleDecl rule) {
        rules.add(rule);
    }

    public TokenDecl getToken(String name) throws ParseException {
        for (TokenDecl decl : tokens) {
            if (decl.tokenName.equals(name)) {
                return decl;
            }
        }
        for (TokenDecl decl : skip) {
            if (decl.tokenName.equals(name)) {
                return decl;
            }
        }
        throw new ParseException("unkdown reference=" + name);
    }

    public int indexOf(String name) {
        int i = 0;
        for (TokenDecl decl : tokens) {
            if (decl.tokenName.equals(name)) {
                return i;
            }
            i++;
        }
        i = 0;
        for (TokenDecl decl : skip) {
            if (decl.tokenName.equals(name)) {
                return i;
            }
            i++;
        }
        throw new ParseException("unkdown reference=" + name);
    }

    //construct NFA from this grammar file
    public NFA makeNFA() throws ParseException {
        makeDistincRanges();
        NFA nfa = new NFA(100);
        nfa.tree = this;
        for (TokenDecl decl : tokens) {
            if (!decl.fragment) {
                nfa.addRegex(decl);
            }
        }
        for (TokenDecl decl : skip) {
            if (!decl.fragment) {
                nfa.addRegex(decl);
            }
        }
        return nfa;
    }

    //ebnf to bnf
    /*public Tree transform() {
        Tree tree = new Tree();//result tree

        for (RuleDecl decl : rules.list) {
            RuleDecl d = new RuleDecl(decl.name);
            Rule rhs = decl.rhs;
            if (rhs.isGroup()) {
                //remove unnecessary parenthesis
                //r = (s1 s2);
                d.rhs = rhs.asGroup().rhs;
                tree.addRule(d);
            }
            else if (rhs.isName()) {
                tree.addRule(decl);
            }
            else if (rhs.isSequence()) {
                //todo
                d.rhs=rhs.asSequence().transform(decl,tree);
                tree.addRule(d);
            }
        }

        return tree;
    }*/

    void printTokens(StringBuilder sb) {
        sb.append("/* tokens */\n\n");
        sb.append("tokens{\n");
        for (TokenDecl td : tokens) {
            sb.append("  ");
            sb.append(td);
            sb.append("\n");
        }
        sb.append("}");
    }

    void printSkips(StringBuilder sb) {
        sb.append("/* skip tokens */\n\n");
        sb.append("skip{\n");
        for (TokenDecl td : skip) {
            sb.append("  ");
            sb.append(td);
            sb.append("\n");
        }
        sb.append("}");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        printTokens(sb);
        sb.append("\n\n");
        printSkips(sb);
        sb.append("\n\n");

        sb.append("/* rules */\n\n");
        sb.append(NodeList.join(rules, "\n"));
        return sb.toString();
    }

    //find all intersecting inputs and split them so that all of them becomes unique
    private void makeDistincRanges() {
        Set<RangeNode> ranges = new HashSet<>();//whole input set as ranges nodes
        List<Bracket> map = new ArrayList<>();
        for (TokenDecl decl : tokens) {
            walkNodes(decl.regex, ranges, map);
        }
        for (TokenDecl decl : skip) {
            walkNodes(decl.regex, ranges, map);
        }
        //find conflicting ranges and split them
        outer:
        while (true) {
            for (Bracket b : map) {
                for (RangeNode rangeNode : b.rangeNodes) {
                    //if this range intersect some ranges
                    //for (Bracket otherBracket : map) {
                    for (RangeNode otherRange : ranges) {
                        //RangeNode otherRange = it2.next();
                        if (rangeNode.isSingle() && rangeNode.intersect(otherRange) && !rangeNode.same(otherRange)) {
                            RangeNode inter = Bracket.intersect(rangeNode, otherRange);
                            RangeNode me1 = RangeNode.of(rangeNode.start, inter.start - 1);
                            RangeNode me2 = RangeNode.of(inter.end + 1, rangeNode.end);
                            RangeNode he1 = RangeNode.of(otherRange.start, inter.start - 1);
                            RangeNode he2 = RangeNode.of(inter.end + 1, otherRange.end);
                            b.rangeNodes.remove(rangeNode);
                            ranges.remove(rangeNode);
                            //otherBracket.rangeNodes.remove(otherRange);
                            //it2.remove();
                            if (me1.isValid()) {
                                b.rangeNodes.add(me1);
                                ranges.add(me1);
                            }
                            if (me2.isValid()) {
                                b.rangeNodes.add(me2);
                                ranges.add(me2);
                            }
                            ranges.remove(otherRange);
                            if (he1.isValid()) ranges.add(he1);
                            if (he2.isValid()) ranges.add(he2);

                            b.rangeNodes.add(inter);
                            ranges.add(inter);
                            b.list.list.clear();
                            b.list.addAll(b.rangeNodes);
                            continue outer;
                        }//for ranges
                        //}//for bracket
                    }//for ranges
                }//for bracket
            }
            break;
        }//while
    }

    void walkNodes(Node root, Set<RangeNode> ranges, List<Bracket> map) {
        //find all ranges and store them
        if (root.isBracket()) {
            Bracket b = root.asBracket();
            b.normalize();
            ranges.addAll(b.rangeNodes);
            map.add(b);
        }
        else if (root.isSequence()) {
            for (Node c : root.asSequence().list) {
                walkNodes(c, ranges, map);
            }
        }
        else if (root.isString()) {
            StringNode stringNode = root.asString();
            if (stringNode.isDot) {
                Bracket b = stringNode.toBracket().normalize();
                ranges.addAll(b.rangeNodes);
                map.add(b);
            }
            else {
                String str = root.asString().value;
                for (char c : str.toCharArray()) {
                    ranges.add(RangeNode.of(c, c));
                }
            }

        }
        else if (root.isGroup()) {
            walkNodes(root.asGroup().rhs, ranges, map);
        }
        else if (root.isRegex()) {
            walkNodes(root.asRegex().node, ranges, map);
        }
        else if (root.isOr()) {
            for (Node c : root.asOr().list) {
                walkNodes(c, ranges, map);
            }
        }
    }
}
