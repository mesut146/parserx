package mesut.parserx.regex.parser;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RegexVisitor {

    public static Node make(String string) throws IOException {
        Lexer lexer = new Lexer(new StringReader(string));
        Parser parser = new Parser(lexer);
        return new RegexVisitor().visitRhs(parser.rhs());
    }

    public Node visitRhs(Ast.rhs rhs) {
        List<Node> list = new ArrayList<>();
        list.add(visitSeq(rhs.seq));
        for (Ast.rhsg1 rhsg1 : rhs.g1) {
            list.add(visitSeq(rhsg1.seq));
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        else {
            return new Or(list);
        }
    }

    private Node visitSeq(Ast.seq seq) {
        List<Node> list = new ArrayList<>();
        for (Ast.regex rhsg1 : seq.regex) {
            list.add(visitRegex(rhsg1));
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        else {
            return new Sequence(list);
        }
    }

    private Node visitRegex(Ast.regex regex) {
        Node node;
        if (regex.simple.normalChar != null) {
            if (regex.simple.normalChar.CHAR != null) {
                node = new StringNode(regex.simple.normalChar.CHAR.value);
            }
            else if (regex.simple.normalChar.DOT != null) {
                node = new Dot();
            }
            else if (regex.simple.normalChar.ESCAPED != null) {
                char ch = regex.simple.normalChar.ESCAPED.value.charAt(1);
                if (UnicodeUtils.escapeMap.containsKey(ch)) {
                    ch = UnicodeUtils.get(ch);
                }
                else {
                    //escaped meta char
                }
                node = new StringNode("" + ch);
            }
            else {
                node = new StringNode("-");
            }
        }
        else if (regex.simple.bracket != null) {
            Bracket bracket = new Bracket();
            node = bracket;
            if (regex.simple.bracket.XOR != null) {
                bracket.negate = true;
            }
            for (Ast.range range : regex.simple.bracket.range) {
                if (range.g1 != null) {
                    //range
                    bracket.add(new Range(visitRangeChar(range.rangeChar), visitRangeChar(range.g1.rangeChar)));
                }
                else {
                    //simple
                    bracket.add(new Range(visitRangeChar(range.rangeChar)));
                }
            }
        }
        else {
            node = new Group(visitRhs(regex.simple.simple3.rhs));
        }
        if (regex.g1 != null) {
            if (regex.g1.PLUS != null) {
                return new Regex(node, RegexType.PLUS);
            }
            else if (regex.g1.STAR != null) {
                return new Regex(node, RegexType.STAR);
            }
            else {
                return new Regex(node, RegexType.OPTIONAL);
            }
        }
        return node;
    }

    int visitRangeChar(Ast.rangeChar rangeChar) {
        if (rangeChar.CHAR != null) {
            return rangeChar.CHAR.value.charAt(0);
        }
        else if (rangeChar.ESCAPED != null) {
            return UnicodeUtils.get(rangeChar.ESCAPED.value.charAt(1));
        }
        else if (rangeChar.STAR != null) {
            return '*';
        }
        else if (rangeChar.PLUS != null) {
            return '+';
        }
        else if (rangeChar.QUES != null) {
            return '?';
        }
        else if (rangeChar.BAR != null) {
            return '|';
        }
        else if (rangeChar.DOT != null) {
            return '.';
        }
        else if (rangeChar.LPAREN != null) {
            return '(';
        }
        else if (rangeChar.RPAREN != null) {
            return ')';
        }
        else if (rangeChar.XOR != null) {
            return '^';
        }
        else if (rangeChar.MINUS != null) {
            return '-';
        }
        else if (rangeChar.BOPEN != null) {
            return '[';
        }
        else {
            throw new RuntimeException("invalid regex char");
        }
    }
}
