package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.gen.lr.LrItem;
import mesut.parserx.gen.targets.JavaRecDescent;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static mesut.parserx.gen.lldfa.LLDfaBuilder.dollar;

public class JavaGen {
    LLDfaBuilder builder;
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;
    Map<Integer, Map<Type, String>> nameMap = new HashMap<>();

    public JavaGen(Tree tree) {
        this.tree = tree;
        builder = new LLDfaBuilder(tree);
        options = tree.options;
    }

    public void gen() {
        builder.factor();
        w = new CodeWriter(true);
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("");
        w.append("public class %s{", options.parserClass);

        w.append("%s lexer;", options.lexerClass);
        w.append("%s la;", options.tokenClass);
        w.append("");

        w.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);

        w.all("this.lexer = lexer;\nla = lexer.next();\n}");
        w.append("");

        //writeConsume();
        builder.inlined.forEach(set -> System.out.printf("inlined=%d\n", set.stateId));

        for (String ruleName : builder.rules.keySet()) {
            if (!ruleName.equals("E")) continue;
            rule = tree.getRule(ruleName);
            writeRule(builder.rules.get(ruleName));
        }

        w.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");
        try {
            Utils.write(w.get(), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeRule(Set<ItemSet> all) {
        for (ItemSet set : all) {
            //if (builder.inlined.contains(set) && !set.isStart) continue;
            writeSet(set);
        }
    }

    void writeSet(ItemSet set) {
        if (set.transitions.isEmpty()) return;
        if (set.isStart) {
            w.append("public %s %s(){", rule.retType, rule.getName());
            //create res
            w.append("%s res = new %s();", rule.retType, rule.retType);
        }
        else {
            w.append("public %s S%d(){", rule.retType, set.stateId);
        }

        //collect items by la
        Map<Name, List<LrItem>> groups = group(set);

        //process items
        boolean first = true;
        for (Name sym : groups.keySet()) {
            List<LrItem> list = groups.get(sym);
            Map<Type, String> names = new HashMap<>();
            writeReduces(set, names, list);
            if (first) {
                w.append("if(la.type == Tokens.%s){", sym.name);
                first = false;
            }
            else {
                w.append("else if(la.type == Tokens.%s){", sym.name);
            }
            //create and assign nodes
            createNodes(list, names);
            assign(list, names);
            nameMap.put(set.stateId, names);

            /*
            for (LrItem item : list) {
                if (set.isStart && item.isReduce(tree)) {
                    //assign parent
                    for (LrItem parent : item.reduceParent) {
                        Node pNode = parent.getNode(parent.dotPos);
                        w.append("%s.%s = %s;", names.get(parent.rule.retType), pNode.astInfo.varName, names.get(item.rule.retType));
                    }
                }
            }*/
            //assign la
            for (LrItem item : list) {
                Name node = has(item, sym);
                if (node != null) {
                    Type type = item.rule.which == -1 ? item.rule.retType : item.rhs.astInfo.nodeType;
                    w.append("%s.%s = la;", names.get(type), node.astInfo.varName);
                }
            }
            //get next la
            //todo beginning of every state is better
            w.append("la = lexer.%s();", options.lexerFunction);
            ItemSet target = builder.getTarget(set, sym);
            //inline target reductions
            if (target.transitions.isEmpty()) {
                //inline
                for (LrItem targetItem : target.all) {
                    if (targetItem.dotPos > 0 && targetItem.getNode(targetItem.dotPos - 1).equals(sym)) {
                        //assign factor
                        //w.append("%s.%s = la;", names.get(targetItem), targetItem.getNode(targetItem.dotPos - 1).astInfo.varName);
                    }
                }
            }
            else {
                w.print("res = S%d(", target.stateId);
                boolean f = true;
                for (Type key : names.keySet()) {
                    if (!f) {
                        w.sb.append(", ");
                    }
                    w.sb.append(names.get(key));
                    f = false;
                }
                w.sb.append(");\n");
            }
            w.append("}");
        }
        w.append("return res;");
        w.append("}");
    }


    private void assign(List<LrItem> list, Map<Type, String> names) {
        Set<String> done = new HashSet<>();
        //assign created nodes
        for (LrItem item : list) {
            if (item.dotPos != 0) continue;
            //no alt
            if (item.rule.which == -1) {
                String name = names.get(item.rule.retType);
                for (LrItem sender : senders(item, list)) {
                    Type type = sender.rule.which == -1 ? sender.rule.retType : sender.rhs.astInfo.nodeType;
                    w.append("%s.%s = %s;", names.get(type), sender.getDotNode().astInfo.varName, name);
                }
            }
            else {
                //parent
                if (!done.contains(item.rule.baseName()) && !item.lookAhead.contains(dollar)) {
                    String name = names.get(item.rule.retType);
                    done.add(item.rule.baseName());
                    for (LrItem sender : senders(item, list)) {
                        Type type = sender.rule.which == -1 ? sender.rule.retType : sender.rhs.astInfo.nodeType;
                        w.append("%s.%s = %s;", names.get(type), sender.getDotNode().astInfo.varName, name);
                    }
                }
            }
        }
    }

    List<LrItem> senders(LrItem item, List<LrItem> list) {
        List<LrItem> res = new ArrayList<>();
        for (LrItem sender : list) {
            if (sender.getDotNode().equals(item.rule.ref)) {
                res.add(sender);
            }
        }
        return res;
    }

    private void createNodes(List<LrItem> list, Map<Type, String> names) {
        Set<String> done = new HashSet<>();
        int cnt = 0;
        for (LrItem item : list) {
            if (item.dotPos != 0) continue;
            //no alt
            if (item.rule.which == -1) {
                String name = "v" + cnt++;
                names.put(item.rule.retType, name);
                w.append("%s %s = new %s();", item.rule.retType, name, item.rule.retType);
            }
            else {
                //parent
                if (!done.contains(item.rule.baseName())) {
                    String pname = "v" + cnt++;
                    names.put(item.rule.retType, pname);
                    done.add(item.rule.baseName());
                    w.append("%s %s = new %s();", item.rule.retType, pname, item.rule.retType);
                }
                //alt
                String name = "v" + cnt++;
                names.put(item.rule.rhs.astInfo.nodeType, name);
                w.append("%s %s = new %s();", item.rule.rhs.astInfo.nodeType, name, item.rule.rhs.astInfo.nodeType);
            }
        }
    }

    private void writeReduces(ItemSet set, Map<Type, String> names, List<LrItem> list) {
        for (LrItem item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (item.rule.which == -1) continue;//only alts
            w.append("if(%s){", JavaRecDescent.loopExpr(item.lookAhead));
            String pname = names.get(getType(item));
            w.append("%s.which = %d;", pname, item.rule.which);
            w.append("%s.%s = %s;", pname, item.rhs.astInfo.varName, pname);
            w.append("}");
        }
    }

    Type getType(LrItem item) {
        if (item.rule.which == -1) {
            return item.rule.retType;
        }
        else {
            return item.rule.rhs.astInfo.nodeType;
        }
    }

    private Map<Name, List<LrItem>> group(ItemSet set) {
        Map<Name, List<LrItem>> groups = new HashMap<>();
        for (LrItem item : set.all) {
            //factor consume & assign
            if (item.dotPos >= item.rhs.size()) continue;
            Node rest;
            if (item.rhs.size() - item.dotPos == 1) {
                rest = item.getDotNode();
            }
            else {
                rest = Sequence.make(item.rhs.list.subList(item.dotPos, item.rhs.size()));
            }
            Set<Name> tokens = FirstSet.tokens(rest, tree);
            if (!tokens.isEmpty()) {
                for (Name token : tokens) {
                    List<LrItem> list = groups.getOrDefault(token, new ArrayList<>());
                    list.add(item);
                    groups.put(token, list);
                }
            }
            else {
                throw new RuntimeException("");
            }
        }
        return groups;
    }

    Name has(LrItem item, Name sym) {
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            Node ch = item.getNode(i);
            if (ch.equals(sym)) {
                return ch.asName();
            }
            if (!FirstSet.canBeEmpty(ch, tree)) {
                break;
            }
        }
        return null;
    }

    boolean isTargetEnd(ItemSet target) {
        return builder.hasFinal(target) && target.transitions.isEmpty();
    }
}
