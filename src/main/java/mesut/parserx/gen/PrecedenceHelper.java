package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;

public class PrecedenceHelper {
    Tree tree;
    Map<String, Integer> levels = new HashMap<>();
    int lastLevel;
    RuleDecl rule;

    public PrecedenceHelper(Tree tree) {
        this.tree = tree;
        EbnfToBnf.combine_or = true;
        EbnfToBnf.expand_or = false;
        EbnfToBnf.expandGroup = false;
        this.tree = EbnfToBnf.transform(tree);
    }

    public Tree transform() {
        List<RuleDecl> rules = new ArrayList<>(tree.rules);
        for (RuleDecl rule : rules) {
            this.rule = rule;
            levels.clear();
            lastLevel = 0;
            handle();
        }
        return tree;
    }

    boolean isName(Node node, String name) {
        return node.isName() && node.asName().name.equals(name);
    }

    String getOp(NameNode name) {
        TokenDecl tok = tree.getToken(name.name);
        if (tok.regex.isString()) {
            return tok.regex.asString().value;
        }
        return null;
    }

    void addLast(String... all) {
        for (String s : all) {
            levels.put(s, lastLevel);
        }
        lastLevel--;//last added gets lower prec
    }

    OrNode collect(OrNode or) {
        OrNode rest = new OrNode();
        for (Node ch : or) {
            if (!ch.isSequence()) {
                rest.add(ch);
                continue;
            }
            boolean added = false;
            Sequence seq = ch.asSequence();
            if (seq.size() == 3 && isName(seq.first(), rule.name) && isName(seq.last(), rule.name)) {
                Node mid = seq.get(1);
                if (mid.isString()) {
                    addLast(mid.asString().value);
                    added = true;
                }
                else if (mid.isName() && mid.asName().isToken) {
                    String op = getOp(mid.asName());
                    if (op != null) {
                        addLast(op);
                        added = true;
                    }
                }
                else if (mid.isGroup()) {
                    //same level ops
                    GroupNode group = mid.asGroup();
                    if (group.node.isString()) {
                        addLast(group.node.asString().value);
                        added = true;
                    }
                    else if (group.node.isName()) {
                        String op = getOp(group.node.asName());
                        if (op != null) {
                            addLast(op);
                            added = true;
                        }
                    }
                    else if (group.node.isOr()) {
                        OrNode o = group.node.asOr();
                        List<String> arr = new ArrayList<>();
                        for (Node c : o) {
                            if (c.isString()) {
                                arr.add(c.asString().value);
                            }
                            else if (c.isName()) {
                                String op = getOp(c.asName());
                                if (op != null) {
                                    arr.add(op);
                                }
                            }
                            else {
                                break;
                            }
                        }
                        if (arr.size() == o.size()) {
                            //valid
                            addLast(arr.toArray(new String[0]));
                            added = true;
                        }
                    }
                }
            }
            if (!added) {
                rest.add(ch);
            }
        }
        return rest;
    }

    //first is higher
    private void handle() {
        Node rhs = rule.rhs.normal();
        if (!rhs.isOr()) return;

        //collect operators
        OrNode rest = collect(rhs.asOr());

        if (levels.isEmpty()) {
            //no operator
            return;
        }
        //begin transform
        //group same level operators
        Map<Integer, OrNode> groups = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            OrNode list = groups.get(entry.getValue());
            if (list == null) {
                list = new OrNode();
                groups.put(entry.getValue(), list);
            }
            list.add(new StringNode(entry.getKey()));
        }

        //create rules for each group
        OrNode res = new OrNode(rest.list);
        rule.rhs = res;

        //remove lowest because it accepts any so it is in main rule
        res.add(Sequence.of(rule.ref(), groups.remove(lastLevel + 1).normal(), rule.ref()));

        Node prev = new OrNode(rest.list);
        //start from highest
        for (int i = 0; i > lastLevel + 1; i--) {
            OrNode or = groups.get(i);
            String name = getName(or);
            String lhsName = name + "0";
            NameNode ref = new NameNode(name, false);
            res.add(new NameNode(name, false));
            tree.addRule(new RuleDecl(name, Sequence.of(new NameNode(lhsName), or.normal(), new NameNode(lhsName))));
            //make lhs rhs
            RuleDecl lhsRule = new RuleDecl(lhsName, new OrNode(ref, prev).normal());
            tree.addRule(lhsRule);
            prev = lhsRule.ref();
        }
    }

    String getName(OrNode ops) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ops.size(); i++) {
            sb.append(tree.getTokenByValue(ops.get(i).asString().value).tokenName);
            if (i < ops.size() - 1) sb.append("_");
        }
        return sb.toString();
    }

}
