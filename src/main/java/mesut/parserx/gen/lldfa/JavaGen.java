package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.gen.lr.LrItem;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JavaGen {
    LLDfaBuilder builder;
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;

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
        Map<LrItem, String> names = new HashMap<>();
        for (ItemSet set : all) {
            if (set.transitions.isEmpty()) continue;
            if (set.isStart) {
                w.append("public %s %s(){", rule.retType, rule.getName());
                //create res
                w.append("%s res = new %s();", rule.retType, rule.retType);
            }
            else {
                w.append("public %s S%d(){", rule.retType, set.stateId);
            }

            //collect items by la
            Map<Name, List<LrItem>> groups = new HashMap<>();
            int cnt = 0;
            for (LrItem item : set.all) {
                //factor consume & assign
                //node creations
                if (item.dotPos == 0) {
                    String name = "v" + cnt++;
                    names.put(item, name);
                }
                if (item.dotPos < item.rhs.size()) {
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
            }
            //process items
            boolean first = true;
            for (Name sym : groups.keySet()) {
                List<LrItem> list = groups.get(sym);
                if (first) {
                    w.append("if(la.type == Tokens.%s){", sym.name);
                    first = false;
                }
                else {
                    w.append("else if(la.type == Tokens.%s){", sym.name);
                }
                //define factor
                w.append("%s la = consume(Tokens.%s);", options.tokenClass, sym.name);
                //create and assign nodes
                for (LrItem item : list) {
                    if (item.dotPos == 0) {
                        Type type = item.rule.which == -1 ? rule.retType : item.rule.rhs.astInfo.nodeType;
                        w.append("%s %s = new %s();", type, names.get(item), type);
                    }
                    else if (item.isReduce(tree)) {
                        //assign parent
                        for (LrItem parent : item.reduceParent) {
                            Node pNode = parent.getNode(parent.dotPos);
                            w.append("%s.%s = %s;", names.get(parent), pNode.astInfo.varName, names.get(item));
                        }
                    }
                }
                for (LrItem item : list) {
                    Name node = has(item, sym);
                    if (node != null) {
                        w.append("%s.%s = la;", names.get(item), node.astInfo.varName);
                    }
                }
                ItemSet target = builder.getTarget(set, sym);
                //inline target reductions
                for (LrItem item : target.all) {
                    if (item.isReduce(tree)) {
                        if (item.lookAhead.contains(LLDfaBuilder.dollar)) {
                            LrItem sender = null;
                            for (LrItem s : list) {
                                if (s.rule.equals(item.rule)) {
                                    sender = s;
                                }
                            }
                            w.append("res.which = %d;", item.rule.which);
                            w.append("res.%s = %s;", item.rhs.astInfo.varName, names.get(sender));
                            w.append("return res;");
                        }
                        else {
                            for (LrItem parent : item.reduceParent) {
                                w.append("%s.%s = %s;", names.get(parent), parent.getNode(parent.dotPos).astInfo.varName, names.get(item));
                            }
                        }

                    }
                }
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
                    w.append("res = S%d();", target.stateId);
                }
                w.append("}");
            }
            w.append("return res;");
            w.append("}");
        }
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
