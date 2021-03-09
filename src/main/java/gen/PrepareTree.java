package gen;

import nodes.*;
import nodes.RuleDecl;

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
            for (int i = 0; i < sequence.size(); i++) {
                sequence.set(i, check(sequence.get(i), tree));
            }
        }
        else if (node.isOr()) {
            OrNode orNode = node.asOr();
            for (int i = 0; i < orNode.size(); i++) {
                orNode.set(i, check(orNode.get(i), tree));
            }
        }
        else if (node.isGroup()) {
            GroupNode orNode = node.asGroup();
            orNode.node = check(orNode.node, tree);
        }
        else if (node.isName()) {
            NameNode nameNode = node.asName();
            //first look into rules
            if (tree.hasRule(nameNode.name)) {
                nameNode.isToken = false;
            }
            else {
                if (tree.getToken(nameNode.name) == null) {
                    throw new RuntimeException("invalid reference: " + nameNode.name);
                }
                else {
                    nameNode.isToken = true;
                }
            }
        }
        else if (node.isRegex()) {
            node.asRegex().node = check(node.asRegex().node, tree);
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
        else if (node instanceof EmptyNode) {

        }
        else {
            throw new RuntimeException("unexpected node: " + node);
        }
        return node;
    }
}
