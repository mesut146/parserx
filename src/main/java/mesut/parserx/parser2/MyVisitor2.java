package mesut.parserx.parser2;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

public class MyVisitor2 {

    public static Tree makeTree(String data) throws IOException {
        //grammar += " ";
        Parser parser = new Parser(new Lexer(new StringReader(data)));
        return new MyVisitor2().visitTree(parser.tree()).prepare();
    }

    public static Tree makeTree(File path) throws IOException {
        //grammar += " ";
        Parser parser = new Parser(new Lexer(new FileReader(path)));
        Tree tree = new MyVisitor2().visitTree(parser.tree());
        tree.file = path;
        tree.resolveIncludes();
        return tree.prepare();
    }

    public Tree visitTree(Ast.tree node) {
        Tree tree = new Tree();
        for (Ast.includeStatement inc : node.includeStatement) {
            tree.addInclude(UnicodeUtils.trimQuotes(inc.STRING.value));
        }
        if (node.optionsBlock != null) {
            for (Ast.option option : node.optionsBlock.option) {
                if (option.key.value.equals("")) {
                    //todo
                }
            }
        }
        for (Ast.treeg1 g1 : node.tokens) {
            if (g1.tokenBlock != null) {
                for (Ast.tokenDecl decl : g1.tokenBlock.tokenDecl) {
                    tree.addToken(visitTokendecl(decl));
                }
            }
            else {
                for (Ast.tokenDecl decl : g1.skipBlock.tokenDecl) {
                    tree.addSkip(visitTokendecl(decl));
                }
            }
        }
        if (node.startDecl != null) {
            tree.start = new Name(node.startDecl.name.IDENT.value);
        }
        for (Ast.treeg2 g2 : node.rules) {
            if (g2.assocDecl != null) {
                Assoc assoc = new Assoc();
                tree.assocList.add(assoc);
                assoc.isLeft = g2.assocDecl.type.LEFT != null;
                for (Ast.ref token : g2.assocDecl.ref) {
                    assoc.list.add(new Name(token.name.IDENT.value, true));
                }
            }
            else {
                tree.addRule(visitRuledecl(g2.ruleDecl));
            }
        }
        return tree;
    }


    public TokenDecl visitTokendecl(Ast.tokenDecl node) {
        TokenDecl decl = new TokenDecl(node.name.IDENT.value);
        if (node.HASH != null) {
            decl.fragment = true;
        }
        decl.rhs = visitRhs(node.rhs);
        return decl;
    }


    public RuleDecl visitRuledecl(Ast.ruleDecl node) {
        RuleDecl decl = new RuleDecl(node.name.IDENT.value);
        if (node.args != null) {
            decl.args.add(new Name(node.args.name.IDENT.value));
            for (Ast.argsg1 g1 : node.args.rest) {
                decl.args.add(new Name(g1.name.IDENT.value));
            }
        }
        decl.rhs = visitRhs(node.rhs);
        return decl;
    }


    public Node visitRhs(Ast.rhs node) {
        Or or = new Or();
        or.add(visitSequence(node.sequence));
        for (Ast.rhsg1 g1 : node.g1) {
            or.add(visitSequence(g1.sequence));
        }
        return or.normal();
    }


    public Node visitSequence(Ast.sequence node) {
        Sequence s = new Sequence();
        for (Ast.regex r : node.regex) {
            s.add(visitRegex(r));
        }
        if (node.label != null) {
            s.label = node.label.name.IDENT.value;
        }
        return s.normal();
    }


    public Node visitRegex(Ast.regex node) {
        Node res = visitSimple(node.simple);
        if (node.name != null) {
            res.varName = node.name.name.toString();
        }
        if (node.type != null) {
            String type;
            if (node.type.QUES != null) {
                type = "?";
            }
            else if (node.type.STAR != null) {
                type = "*";
            }
            else {
                type = "+";
            }
            return new Regex(res, type);
        }
        return res;
    }


    public Node visitSimple(Ast.simple node) {
        if (node.group != null) {
            return new Group(visitRhs(node.group.rhs));
        }
        else if (node.dotNode != null) {
            return new Dot();
        }
        else if (node.bracketNode != null) {
            return new Bracket(node.bracketNode.BRACKET.value);
        }
        else if (node.ref != null) {
            return new Name(node.ref.name.IDENT.value);
        }
        else if (node.stringNode != null) {
            return StringNode.from(node.stringNode.STRING.value);
        }
        else if (node.EPSILON != null) {
            return new Epsilon();
        }
        else if (node.untilNode != null) {
            return new Until(visitRegex(node.untilNode.regex));
        }
        else if (node.repeatNode != null) {
            Node rhs = visitRhs(node.repeatNode.rhs);
            if (rhs.isSequence() || rhs.isOr()) {
                rhs = new Group(rhs);
            }
            return new Regex(rhs, "*");
        }
        else if (node.SHORTCUT != null) {
            String s = node.SHORTCUT.value;
            return new Group(Shortcut.from(s.substring(2, s.length() - 2)));
        }
        else {
            throw new RuntimeException("unexpected");
        }
    }

}
