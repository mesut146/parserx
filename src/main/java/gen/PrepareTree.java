package gen;

import nodes.*;
import rule.RuleDecl;

public class PrepareTree {

    //check rule , token, string references
    public static Tree checkReferences(Tree tree) {
        for (RuleDecl decl : tree.rules) {
            decl.rhs = check(decl.rhs, tree);
        }
        return tree;
    }

    static Node check(Node node, Tree tree) {
        if (node.isSequence()) {
            Sequence sequence = node.asSequence();
            for (int i = 0; i < sequence.list.size(); i++) {
                sequence.list.set(i, check(sequence.list.get(i), tree));
            }
        }
        else if (node.isOr()) {
            OrNode orNode = node.asOr();
            for (int i = 0; i < orNode.list.size(); i++) {
                orNode.list.set(i, check(orNode.list.get(i), tree));
            }
        }
        else if (node.isGroup()) {
            GroupNode orNode = node.asGroup();
            orNode.rhs = check(orNode.rhs, tree);
            /*for (Node sub : node.asGroup()) {
                check(sub, tree);
            }*/
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
        else if (node.isRegex()) {
            return check(node.asRegex().node, tree);
        }
        else if (node.isString()) {
            String val = node.asString().value;
            TokenDecl decl = tree.getTokenByValue(val);
            if (decl == null) {
                throw new RuntimeException("unknown string token: " + val);
            }
            //replace
            return decl.makeReference();
        }
        else {
            throw new RuntimeException("internal error on check: " + node);
        }
        return node;
    }
}
