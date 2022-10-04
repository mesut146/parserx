package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;

//Deep Copy
public class Copier extends Transformer {

    public Copier(Tree tree) {
        super(tree);
    }

    public static Tree copyTree(Tree tree) {
        var res = new Tree();
        res.file = tree.file;
        res.start = tree.start;
        res.includes = tree.includes;
        res.options = tree.options;
        res.lexerMembers = tree.lexerMembers;
        res.originalRules = tree.originalRules;
        for (var decl : tree.rules) {
            res.addRule(copyRule(decl));
        }
        return res;
    }

    public static RuleDecl copyRule(RuleDecl decl) {
        var res = new RuleDecl(decl.ref.<Name>copy(), decl.rhs.copy());
        res.retType = decl.retType;
        return res;
    }

    Node withAst(Node res, Node info) {
        res.astInfo = info.astInfo.copy();
        return res;
    }

    @Override
    public Node visitName(Name name, Void arg) {
        var res = new Name(name.name, name.isToken);
        res.args = new ArrayList<>(name.args);
        return withAst(res, name);
    }

    @Override
    public Node visitRegex(Regex regex, Void arg) {
        var ch = transformNode(regex.node, arg);
        return withAst(new Regex(ch, regex.type), regex);
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        List<Node> list = new ArrayList<>();
        for (Node ch : seq) {
            list.add(transformNode(ch, arg));
        }
        var res = new Sequence(list);
        res.assocLeft = seq.assocLeft;
        res.assocRight = seq.assocRight;
        return withAst(res, seq);
    }

    @Override
    public Node visitOr(Or or, Void arg) {
        List<Node> list = new ArrayList<>();
        for (Node ch : or) {
            String label = ch.label;
            ch = transformNode(ch, arg);
            ch.label = label;
            list.add(ch);
        }
        var res = new Or(list);
        return withAst(res, or);
    }

    @Override
    public Node visitGroup(Group node, Void arg) {
        Node ch = transformNode(node.node, arg);
        return withAst(new Group(ch), node);
    }

    @Override
    public Node visitEpsilon(Epsilon epsilon, Void arg) {
        return withAst(new Epsilon(), epsilon);
    }

    //no ast, lexer only nodes
    @Override
    public Node visitRange(Range range, Void arg) {
        return new Range(range.start, range.end);
    }

    @Override
    public Node visitBracket(Bracket bracket, Void arg) {
        var res = new Bracket();
        res.list = new ArrayList<>(bracket.list);
        return res;
    }

    @Override
    public Node visitDot(Dot dot, Void arg) {
        return new Dot();
    }

    @Override
    public Node visitString(StringNode string, Void arg) {
        return new StringNode(string.value);
    }

    @Override
    public Node visitUntil(Until until, Void arg) {
        return new Until(until.node);
    }

    @Override
    public Node visitShortcut(Shortcut shortcut, Void arg) {
        return new Shortcut(shortcut.name);
    }
}
