package gen;

import nodes.Node;
import nodes.TokenDecl;
import nodes.Tree;
import rule.NameNode;
import rule.RuleDecl;

public class PrepareTree {

    //check references
    public static Tree checkReferences(Tree tree) {
        for (RuleDecl decl : tree.rules) {
            check(decl.rhs, tree);
        }
        return tree;
    }

    static void check(Node node, Tree tree) {
        if (node.isSequence()) {
            for (Node sub : node.asSequence()) {
                check(sub, tree);
            }
        }
        else if (node.isName()) {
            NameNode nameNode = node.asName();
            if (tree.getToken(nameNode.name) == null) {
                //parser ref
                if (tree.hasRule(nameNode.name)) {
                    nameNode.isToken = false;
                }
                else {
                    throw new RuntimeException("invalid reference: " + nameNode.name);
                }
            }
            else {
                nameNode.isToken = true;
            }
        }
        else if (node.isGroup()) {
            for (Node sub : node.asGroup()) {
                check(sub, tree);
            }
        }
        else if (node.isRegex()) {
            check(node.asRegex().node, tree);
        }
        else if (node.isString()) {
            String val = node.asString().value;
            TokenDecl decl = tree.getTokenByValue(val);
            if (decl == null) {
                throw new RuntimeException("unknown string value: " + val);
            }
            //todo replace
        }
        else if (node.isOr()) {
            for (Node sub : node.asOr()) {
                check(sub, tree);
            }
        }
        else {
            throw new RuntimeException("internal error on check: " + node);
        }
    }
}
