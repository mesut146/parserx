package itself;

import mesut.parserx.nodes.*;

public class MyVisitor extends ParserVisitor<Object, Object> {

    @Override
    public Tree visitTree(Ast.tree node, Object o) {
        Tree tree = new Tree();
        for (Ast.includeStatement inc : node.includeStatement) {
            tree.addInclude(inc.STRING.value);
        }
        for (Ast.treeg1 g1 : node.g1) {
            if (g1.tokenBlock != null) {
                for (Ast.tokenDecl decl : g1.tokenBlock.tokenDecl) {
                    tree.addToken((TokenDecl) decl.accept(this, null));
                }
            }
            else {
                for (Ast.tokenDecl decl : g1.skipBlock.tokenDecl) {
                    tree.addSkip((TokenDecl) decl.accept(this, null));
                }
            }
        }
        if (node.startDecl != null) {
            tree.start = new Name(node.startDecl.name.IDENT.value);
        }
        for (Ast.ruleDecl decl : node.ruleDecl) {
            tree.addRule((RuleDecl) decl.accept(this, node));
        }
        return tree;
    }

    @Override
    public TokenDecl visitTokendecl(Ast.tokenDecl node, Object o) {
        TokenDecl decl = new TokenDecl(node.name.IDENT.value);
        if (node.HASH != null) {
            decl.fragment = true;
        }
        decl.rhs = (Node) node.rhs.accept(this, node);
        return decl;
    }

    @Override
    public RuleDecl visitRuledecl(Ast.ruleDecl node, Object o) {
        RuleDecl decl = new RuleDecl(node.name.IDENT.value);
        if (node.args != null) {
            decl.args.add(new Name(node.args.name.IDENT.value));
            for (Ast.argsg1 g1 : node.args.g1) {
                decl.args.add(new Name(g1.name.IDENT.value));
            }
        }
        decl.rhs = (Node) node.rhs.accept(this, null);
        return decl;
    }

    @Override
    public Node visitRhs(Ast.rhs node, Object o) {
        Or or = new Or();
        or.add(visitSequence(node.sequence, null));
        for (Ast.rhsg1 g1 : node.g1) {
            or.add(visitSequence(g1.sequence, null));
        }
        return or;
    }

    @Override
    public Node visitSequence(Ast.sequence node, Object o) {
        Sequence s = new Sequence();
        for (Ast.regex r : node.regex) {
            s.add(visitRegex(r, null));
        }
        if (node.label != null) {
            s.label = node.label.name.IDENT.value;
        }
        return s;
    }

    @Override
    public Node visitRegex(Ast.regex node, Object o) {
        Node res = visitSimple(node.simple, null);
        if (node.g1 != null) {
            res.varName = node.g1.name.IDENT.value;
        }
        if (node.g2 != null) {
            String type;
            if (node.g2.QUES != null) {
                type = "?";
            }
            else if (node.g2.STAR != null) {
                type = "*";
            }
            else {
                type = "+";
            }
            return new Regex(res, type);
        }
        return res;
    }

    @Override
    public Node visitSimple(Ast.simple node, Object o) {
        if (node.group != null) {
            return visitGroup(node.group, null);
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
            return new Until(visitRegex(node.untilNode.regex, null));
        }
        else if (node.repeatNode != null) {
            return new Regex(visitRhs(node.repeatNode.rhs, null), "*");
        }
        else {
            throw new RuntimeException("unexpected");
        }
    }

    @Override
    public Group visitGroup(Ast.group node, Object o) {
        return new Group(visitRhs(node.rhs, null));
    }



}
