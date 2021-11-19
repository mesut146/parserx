package mesut.parserx.parser;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

//cst to ast
public class AstBuilder {

    public static Tree makeTree(String data) throws IOException {
        Parser parser = new Parser(new Lexer(new StringReader(data)));
        return new AstBuilder().visitTree(parser.tree());
    }

    public static Tree makeTree(File path) throws IOException {
        Lexer.bufSize = (int) path.length() + 1;
        Parser parser = new Parser(new Lexer(new FileReader(path)));
        Tree tree = new AstBuilder().visitTree(parser.tree());
        tree.file = path;
        tree.resolveIncludes();
        return tree;
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
        for (Ast.ruleDecl g2 : node.rules) {
            tree.addRule(visitRuledecl(g2));
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
            decl.ref.args.add(new Name(node.args.name.IDENT.value));
            for (Ast.argsg1 g1 : node.args.rest) {
                decl.ref.args.add(new Name(g1.name.IDENT.value));
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
        if (node.assoc != null) {
            if (s.size() != 3 && s.size() != 5) {
                throw new RuntimeException("assoc can only be used with 3 or 5 nodes");
            }
            if (node.assoc.LEFT != null) {
                s.assocLeft = true;
            }
            else {
                s.assocRight = true;
            }
        }
        if (node.label != null) {
            s.label = node.label.name.IDENT.value;
        }
        return s.normal();
    }

    String str(Ast.name name) {
        if (name.IDENT != null) {
            return name.IDENT.value;
        }
        else if (name.OPTIONS != null) {
            return name.OPTIONS.value;
        }
        else if (name.SKIP != null) {
            return name.SKIP.value;
        }
        else if (name.TOKEN != null) {
            return name.TOKEN.value;
        }
        else {
            return name.INCLUDE.value;
        }
    }

    public Node visitRegex(Ast.regex node) {
        if (node.regex1 != null) {
            String name = str(node.regex1.name);
            Node res = visitSimple(node.regex1.simple);
            res.astInfo.varName = name;
            if (node.regex1.type != null) {
                String type = node.regex1.type.PLUS != null ? "+" : (node.regex1.type.STAR != null ? "*" : "?");
                return new Regex(res, type);
            }
            return res;
        }
        else {
            Node res = visitSimple(node.regex2.simple);
            if (node.regex2.type != null) {
                String type = node.regex2.type.PLUS != null ? "+" : (node.regex2.type.STAR != null ? "*" : "?");
                return new Regex(res, type);
            }
            return res;
        }
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
            if (node.stringNode.STRING != null) {
                return StringNode.from(node.stringNode.STRING.value);
            }
            return StringNode.from(node.stringNode.CHAR.value);
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
