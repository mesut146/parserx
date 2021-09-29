package mesut.parserx.regex;

import mesut.parserx.nodes.*;

public class RegexFromStr {
    int i = 0;
    String s;

    public static Node build(String s) {
        RegexFromStr regex = new RegexFromStr();
        regex.s = s;
        return regex.rhs();
    }

    public Node rhs() {
        //seq (| seq)*
        Or res = new Or();
        res.add(seq());
        while (i < s.length() && s.charAt(i) == '|') {
            i++;
            res.add(seq());
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res;
    }

    Node seq() {
        //reg+
        Sequence res = new Sequence();
        do {
            res.add(reg());
        } while (i < s.length() && isSimp(s.charAt(i)));
        if (res.size() == 1) {
            return res.get(0);
        }
        return res;
    }

    boolean isSimp(char c) {
        if (c == ')') {
            return false;
        }
        return c != '|';
        /*if ("([~.\\".indexOf(c) != -1) {
            return true;
        }
        return Character.isUnicodeIdentifierStart(c);*/
    }

    Node reg() {
        //simp(*+?)?
        Node node = simp();
        if (i < s.length() && (s.charAt(i) == '*' || s.charAt(i) == '+' || s.charAt(i) == '?')) {
            return new Regex(node, "" + s.charAt(i++));
        }
        return node;
    }

    Node simp() {
        //group | stringNode | bracketNode | untilNode | dotNode;
        switch (s.charAt(i)) {
            case '(': {
                i++;
                Node node = rhs();
                i++;
                return new Group(node);
            }
            case '[': {
                int begin = i;
                int end;
                while (true) {
                    end = s.indexOf(']', i);
                    if (s.charAt(end - 1) == '\\') {
                        //escape
                        i++;
                    }
                    else {
                        break;
                    }
                }
                i = end + 1;
                return new Bracket(s.substring(begin, end));
            }
            case '~': {
                i++;
                return new Until(reg());
            }
            case '.': {
                i++;
                return new Dot();
            }
            default: {
                if (s.charAt(i) == '\\') {
                    i++;
                    i++;
                    return new StringNode("" + s.charAt(i - 1));
                }
                else {
                    Node node = new StringNode("" + s.charAt(i));
                    i++;
                    return node;
                }
                //throw new RuntimeException("unexpected: " + s.charAt(i));
            }
        }
    }
}
