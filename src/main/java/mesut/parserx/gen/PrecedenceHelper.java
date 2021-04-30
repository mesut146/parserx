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
        lastLevel++;
    }

    private void handle() {
        Node rhs = rule.rhs;
        if (!rhs.isOr()) return;

        //collect operators
        OrNode or = rhs.asOr();
        OrNode rest = new OrNode();
        for (Node ch : or) {
            if (!ch.isSequence()) {
                rest.add(ch);
                continue;
            }
            boolean added = false;
            Sequence sequence = ch.asSequence();
            if (sequence.size() == 3 && isName(sequence.first(), rule.name) && isName(sequence.last(), rule.name)) {
                Node mid = sequence.get(1);
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
        if (levels.isEmpty()) {
            return;
        }
        //transform
        System.out.println(levels);
        //group same level operators
        Map<Integer, OrNode> groups = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
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
        res.add(Sequence.of(rule.ref(), groups.remove(0).normal(), rule.ref()));

        int id = 0;
        Node prev = new OrNode(rest.list);
        for (Map.Entry<Integer, OrNode> entry : groups.entrySet()) {
            String name = rule.name + id++;
            String lhsName = name + "0";
            NameNode ref = new NameNode(name, false);
            res.add(new NameNode(name, false));
            tree.addRule(new RuleDecl(name, Sequence.of(new NameNode(lhsName), entry.getValue().normal(), new NameNode(lhsName))));
            //make lhs rhs
            RuleDecl lhsRule = new RuleDecl(lhsName, new OrNode(ref, prev));
            tree.addRule(lhsRule);
            prev = lhsRule.ref();
        }
    }

}
