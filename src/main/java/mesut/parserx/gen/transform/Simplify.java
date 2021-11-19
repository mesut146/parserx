package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.nodes.*;

//remove unnecessary nodes & merge
public class Simplify extends Transformer {

    public Simplify(Tree tree) {
        super(tree);
    }

    public static void all(Tree tree) {
        new Simplify(tree).transformRules();
    }

    public static Node simplifyNode(Node node) {
        return new Simplify(null).transformNode(node, null);
    }

    boolean isSimple(Node node) {
        return node.isName() || node.isGroup() || node.isRegex() || node.isString();
    }

    //unwrap simple child
    @Override
    public Node visitGroup(Group node, Void parent) {
        Node ch = transformNode(node.node, parent);
        if (isSimple(ch)) {
            if (ch.astInfo.varName == null) {
                ch.astInfo.varName = node.astInfo.varName;
            }
            return ch;
        }
        Group res = new Group(ch);
        res.astInfo = node.astInfo.copy();
        return res;
    }

    @Override
    public Node visitSequence(Sequence node, Void parent) {
        Sequence res = new Sequence();
        res.assocLeft = node.assocLeft;
        res.assocRight = node.assocRight;
        for (Node ch : node) {
            ch = transformNode(ch, parent);
            if (ch.isSequence()) {
                res.addAll(ch.asSequence().list);
            }
            else if (ch.isGroup() && ch.asGroup().node.isSequence()) {
                if (ch.astInfo.varName != null) {
                    //don't over simplify
                    res.add(ch);
                }
                else {
                    res.addAll(ch.asGroup().node.asSequence().list);
                }
            }
            else if (!ch.isEpsilon()) {
                res.add(ch);
            }
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res;
    }

    //todo get rid of merger
    @Override
    public Node visitOr(Or node, Void parent) {
        Or res = new Or();
        for (Node ch : node) {
            ch = transformNode(ch, parent);
            if (ch.isOr()) {
                res.addAll(ch.asOr().list);
            }
            else if (ch.isGroup() && ch.asGroup().node.isOr()) {
                if (ch.astInfo.varName != null) {
                    //don't over simplify
                    res.add(ch);
                }
                else {
                    res.addAll(ch.asGroup().node.asOr().list);
                }
            }
            else {
                res.add(ch);
            }
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        return res.dups();
    }

    Node withAst(Node node, AstInfo info) {
        if (node.astInfo.varName == null) {
            node.astInfo = info;
        }
        return node;
    }

    @Override
    public Node visitRegex(Regex regex, Void arg) {
        if (!regex.node.isGroup() && !regex.node.isName()) {
            return visitRegex(new Regex(new Group(regex.node), regex.type), arg);
        }
        Node ch = transformNode(regex.node, arg);
        Node res = null;
        if (regex.isOptional()) {
            if (ch.isPlus()) {
                res = new Regex(ch.asRegex().node, "*");
            }
            else if (ch.isStar() || ch.isOptional()) {
                res = ch;
            }
        }
        else if (regex.isStar()) {
            if (ch.isStar() || ch.isPlus()) {
                res = ch;
            }
            if (ch.isOptional()) {
                ch = new Regex(ch.asRegex().node, "*");
            }
        }
        else {
            if (ch.isOptional() || ch.isStar()) {
                res = new Regex(ch.asRegex().node, "*");
            }
            if (ch.isPlus()) {
                res = ch;
            }
        }
        if (res == null) {
            res = new Regex(ch, regex.type);
        }
        return withAst(res, regex.astInfo);
    }
}
