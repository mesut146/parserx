package mesut.parserx.parser;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

//cst to ast
public class AstVisitor {

    public static Tree makeTree(String data) throws IOException {
        var parser = new Parser(new Lexer(new StringReader(data)));
        return new AstVisitor().visitTree(parser.tree());
    }

    public static Tree makeTree(File path) throws IOException {
        Lexer.bufSize = (int) path.length() + 1;
        Parser parser = new Parser(new Lexer(new FileReader(path)));
        Tree tree = new AstVisitor().visitTree(parser.tree());
        tree.file = path;
        tree.resolveIncludes();
        return tree;
    }

    public Tree visitTree(Ast.tree node) {
        var tree = new Tree();
        for (var inc : node.includeStatement) {
            tree.addInclude(UnicodeUtils.trimQuotes(inc.STRING.value));
        }
        if (node.optionsBlock != null) {
            for (var option : node.optionsBlock.option) {
                if (option.key.value.equals("")) {
                    //todo
                }
            }
        }
        for (var g1 : node.tokens) {
            if (g1.tokenBlock != null) {
                for (var decl : g1.tokenBlock.tokenBlock.tokenDecl) {
                    tree.addToken(visitTokendecl(decl));
                }
            }
            else {
                for (var decl : g1.skipBlock.skipBlock.tokenDecl) {
                    tree.addSkip(visitTokendecl(decl));
                }
            }
        }
        if (node.startDecl != null) {
            tree.start = new Name(node.startDecl.name.IDENT.IDENT.value);
        }
        for (var g2 : node.rules) {
            tree.addRule(visitRuledecl(g2));
        }
        return tree;
    }


    public TokenDecl visitTokendecl(Ast.tokenDecl node) {
        var decl = new TokenDecl(node.name.IDENT.IDENT.value);
        if (node.HASH != null) {
            decl.fragment = true;
        }
        decl.rhs = visitRhs(node.rhs);
        return decl;
    }


    public RuleDecl visitRuledecl(Ast.ruleDecl node) {
        var decl = new RuleDecl(node.name.IDENT.IDENT.value);
        if (node.args != null) {
            decl.ref.args.add(new Name(node.args.name.IDENT.IDENT.value));
            for (var g1 : node.args.rest) {
                decl.ref.args.add(new Name(g1.name.IDENT.IDENT.value));
            }
        }
        decl.rhs = visitRhs(node.rhs);
        return decl;
    }


    public Node visitRhs(Ast.rhs node) {
        List<Node> list = new ArrayList<>();
        list.add(visitSequence(node.sequence));
        for (var g1 : node.g1) {
            list.add(visitSequence(g1.sequence));
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return new Or(list);
    }


    public Node visitSequence(Ast.sequence node) {
        List<Node> list = new ArrayList<>();
        for (var r : node.regex) {
            list.add(visitRegex(r));
        }
        //if (list.size() == 1) return list.get(0);
        var seq = new Sequence(list);
        if (node.assoc != null) {
            if (seq.size() != 3 && seq.size() != 5) {
                throw new RuntimeException("assoc can only be used with 3 or 5 nodes");
            }
            if (node.assoc.LEFT != null) {
                seq.assocLeft = true;
            }
            else {
                seq.assocRight = true;
            }
        }
        if (node.label != null) {
            seq.label = node.label.name.IDENT.IDENT.value;
        }
        return seq;
    }

    String str(Ast.name name) {
        if (name.IDENT != null) {
            return name.IDENT.IDENT.value;
        }
        else if (name.OPTIONS != null) {
            return name.OPTIONS.OPTIONS.value;
        }
        else if (name.SKIP != null) {
            return name.SKIP.SKIP.value;
        }
        else {
            return name.TOKEN.TOKEN.value;
        }
    }

    public Node visitRegex(Ast.regex node) {
        if (node.regex1 != null) {
            var name = str(node.regex1.name);
            var res = visitSimple(node.regex1.simple);
            res.astInfo.varName = name;
            if (node.regex1.type != null) {
                var type = node.regex1.type.PLUS != null ? RegexType.PLUS : (node.regex1.type.STAR != null ? RegexType.STAR : RegexType.OPTIONAL);
                return new Regex(res, type);
            }
            return res;
        }
        else {
            var res = visitSimple(node.regex2.simple);
            if (node.regex2.type != null) {
                var type = node.regex2.type.PLUS != null ? RegexType.PLUS : (node.regex2.type.STAR != null ? RegexType.STAR : RegexType.OPTIONAL);
                return new Regex(res, type);
            }
            return res;
        }
    }


    public Node visitSimple(Ast.simple node) {
        if (node.group != null) {
            return new Group(visitRhs(node.group.group.rhs));
        }
        else if (node.dotNode != null) {
            return new Dot();
        }
        else if (node.bracketNode != null) {
            return new Bracket(node.bracketNode.bracketNode.BRACKET.value);
        }
        else if (node.name != null) {
            return new Name(node.name.name.IDENT.IDENT.value);
        }
        else if (node.stringNode != null) {
            if (node.stringNode.stringNode.STRING != null) {
                return StringNode.from(node.stringNode.stringNode.STRING.STRING.value);
            }
            return StringNode.from(node.stringNode.stringNode.CHAR.CHAR.value);
        }
        else if (node.EPSILON != null) {
            return new Epsilon();
        }
        else if (node.untilNode != null) {
            return new Until(visitRegex(node.untilNode.untilNode.regex));
        }
        else if (node.SHORTCUT != null) {
            var s = node.SHORTCUT.SHORTCUT.value;
            return new Group(Shortcut.from(s.substring(2, s.length() - 2)));
        }
        else if(node.call != null){
            var s = node.call.call.CALL_BEGIN.value;
            var res = new Name(s.substring(0, s.length() - 1));
            res.args.add(new Name(node.call.call.IDENT.value));
            for(var arg : node.call.call.g1){
                res.args.add(new Name(arg.IDENT.value));
            }
            return res;
        }    
        else {
            throw new RuntimeException("unexpected");
        }
    }

}
