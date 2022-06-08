package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Type;
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
    Map<Integer, Map<Name, List<Variable>>> nameMap = new HashMap<>();
    Map<Integer, List<Variable>> paramMap = new HashMap<>();
    ItemSet curSet;
    boolean skipHolder = true;

    public JavaGen(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        tree = builder.tree;
        w = new CodeWriter(true);
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("import java.io.IOException;");
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
            //if (!ruleName.equals("E")) continue;
            rule = tree.getRule(ruleName);
            writeRule(builder.rules.get(ruleName));
        }

        w.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(w.get(), file);
        JavaRecDescent recDescent = new JavaRecDescent(tree);
        recDescent.genTokenType();

    }

    void writeRule(Set<ItemSet> all) {
        for (ItemSet set : all) {
            //if (builder.inlined.contains(set) && !set.isStart) continue;
            writeSet(set);
        }
    }

    String params(ItemSet set) {
        if (set.isStart) return "";
        StringBuilder sb = new StringBuilder();
        int cnt = 0;

        for (Variable e : paramMap.get(set.stateId)) {
            if (cnt++ > 0) {
                sb.append(", ");
            }
            sb.append(e.type);
            sb.append(" ");
            sb.append(e.name);
        }

        return sb.toString();
    }

    static class Variable {
        Type type;
        String name;
        Item item;
        Variable bind;//if this is param then bind to local var

        public Variable(Type type, String name, Item item) {
            this.type = type;
            this.name = name;
            this.item = item;
        }

        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    void writeSet(ItemSet set) {
        curSet = set;
        if (set.transitions.isEmpty()) return;
        if (set.isStart) {
            w.append("public %s %s(%s) throws IOException{", rule.retType, rule.getName(), params(set));
        }
        else {
            w.append("public void S%d(%s) throws IOException{", set.stateId, params(set));
        }

        //collect items by la
        Map<Name, List<Item>> groups = group(set);
        writeReduces(set);
        //process items
        boolean first = true;
        for (Name sym : groups.keySet()) {
            List<Item> list = groups.get(sym);

            w.append("%sif(la.type == Tokens.%s){", first ? "" : "else ", sym.name);
            first = false;
            //create and assign nodes
            createNodes(list, sym);
            assign(list, sym);

            //assign la
            for (Item item : list) {
                Name node = has(item, sym);
                if (node != null) {
                    //todo local
                    if (node.astInfo.isInLoop) {
                        w.append("%s.%s.add(la);", getBoth(item, sym), node.astInfo.varName);
                    }
                    else {
                        w.append("%s.%s = la;", getBoth(item, sym), node.astInfo.varName);
                    }
                }
            }
            //get next la
            //todo beginning of every state is better
            w.append("la = lexer.%s();", options.lexerFunction);
            ItemSet target = builder.getTarget(set, sym);
            //inline target reductions
            if (target.transitions.isEmpty()) {
                //inline
                inline(target, sym);
            }
            else {
                //todo looping args, multi state args?
                List<Variable> targetParams = new ArrayList<>();
                if (!paramMap.containsKey(target.stateId)) {
                    paramMap.put(target.stateId, targetParams);
                    w.print("S%d(%s);\n", target.stateId, args(targetParams, sym));
                }
                else {
                    w.print("S%d(%s);\n", target.stateId, args2(paramMap.get(target.stateId), sym));
                }
            }
            if (set.isStart) {
                for (Variable v : nameMap.get(set.stateId).get(sym)) {
                    if (v.item == null && v.type.equals(rule.retType)) {
                        w.append("return %s;", v.name);
                        break;
                    }
                }
            }
            w.append("}");
        }
        if (set.isStart) {
            //todo print la
            w.append("throw new RuntimeException(\"\");");
        }
        w.append("}");
    }

    String args(List<Variable> targetParams, Name sym) {
        StringBuilder sb = new StringBuilder();
        int param_cnt = 0;
        //curParams + createdVars
        boolean hasParams = false;
        if (paramMap.containsKey(curSet.stateId)) {
            boolean first = true;
            Set<Variable> reduced = getReduced();
            for (Variable v : paramMap.get(curSet.stateId)) {
                if (reduced.contains(v)) continue;
                if (!first) sb.append(", ");
                sb.append(v.name);
                targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
                first = false;
                hasParams = true;
            }
        }
        if (hasParams && !nameMap.get(curSet.stateId).isEmpty()) sb.append(", ");
        boolean first = true;
        for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
            if (skipHolder && v.item == null) continue;//skip holder, alt already has ref in it
            if (!first) sb.append(", ");
            sb.append(v.name);
            targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
            first = false;
        }
        return sb.toString();
    }

    String args2(List<Variable> targetParams, Name sym) {
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for (Variable v : targetParams) {
            //locate by item
            if (v.item == null) {
                throw new RuntimeException("not yet");
            }
            if (!f) sb.append(", ");
            sb.append(getBoth(v.item, sym));
            f = false;
        }
        /*int param_cnt = 0;
        //curParams + createdVars
        boolean hasParams = false;
        if (paramMap.containsKey(curSet.stateId)) {
            boolean first = true;
            Set<Variable> reduced = getReduced();
            for (Variable v : paramMap.get(curSet.stateId)) {
                if (reduced.contains(v)) continue;
                if (!first) sb.append(", ");
                sb.append(v.name);
                targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
                first = false;
                hasParams = true;
            }
        }
        if (hasParams && !nameMap.get(curSet.stateId).isEmpty()) sb.append(", ");
        boolean first = true;
        for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
            if (skipHolder && v.item == null) continue;//skip holder, alt already has ref in it
            if (!first) sb.append(", ");
            sb.append(v.name);
            targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
            first = false;
        }*/
        return sb.toString();
    }

    Set<Variable> getReduced() {
        Set<Variable> ignored = new HashSet<>();
        for (Item item : curSet.all) {
            if (!item.isReduce(tree)) continue;
            //mark holder
            for (Variable v : paramMap.get(curSet.stateId)) {
                //remove holder
                if (v.item == null && v.type.equals(item.rule.retType)) {
                    ignored.add(v);
                }
                //alt
                if (v.item != null && item.rule.which != -1 && v.type.equals(item.rule.rhs.astInfo.nodeType)) {
                    ignored.add(v);
                }
                //normal
                if (v.item != null && item.rule.which == -1 && v.type.equals(item.rule.rhs.astInfo.nodeType)) {
                    ignored.add(v);
                }
            }

        }
        return ignored;
    }

    boolean hasCommon(Set<Integer> s1, Set<Integer> s2) {
        Set<Integer> tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        return !tmp.isEmpty();
    }

    String getName(Type type, Name sym) {
        for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
            if (v.type.equals(type)) return v.name;
        }
        return null;
    }

    boolean sameItem(Item i1, Item i2) {
        return hasCommon(i1.ids, i2.ids);
    }

    String getParam(Item item) {
        for (Variable v : paramMap.get(curSet.stateId)) {
            if (v.item != null && sameItem(v.item, item)) {
                return v.name;
            }
        }
        return null;
    }

    String getParam(Type type) {
        for (Variable v : paramMap.get(curSet.stateId)) {
            if (v.type.equals(type)) {
                return v.name;
            }
        }
        return null;
    }

    String getBoth(Item item, Name sym) {
        if (sym == null) {
            return getParam(item);
        }
        String res = getName(getType(item), sym);
        if (res == null) return getParam(item);
        return res;
    }

    private void createNodes(List<Item> list, Name sym) {
        List<Variable> vars = new ArrayList<>();
        Set<String> done = new HashSet<>();
        int cnt = 0;
        Map<Item, String> holderMap = new HashMap<>();
        for (Item item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            //no alt
            if (item.rule.which == -1) {
                String name = "v" + cnt++;
                vars.add(new Variable(item.rule.retType, name, item));
                w.append("%s %s = new %s();", item.rule.retType, name, item.rule.retType);
            }
            else {
                //parent
                if (!done.contains(item.rule.baseName())) {
                    String pname = "v" + cnt++;
                    vars.add(new Variable(item.rule.retType, pname, null));
                    done.add(item.rule.baseName());
                    w.append("%s %s = new %s();", item.rule.retType, pname, item.rule.retType);
                    holderMap.put(item, pname);
                }
                //alt
                String name = "v" + cnt++;
                String pname = holderMap.get(item);
                if (pname == null) {
                    for (Item sib : holderMap.keySet()) {
                        if (item.siblings.contains(sib)) {
                            pname = holderMap.get(sib);
                            break;
                        }
                    }
                }
                vars.add(new Variable(item.rule.rhs.astInfo.nodeType, name, item));
                w.append("%s %s = new %s();", item.rule.rhs.astInfo.nodeType, name, item.rule.rhs.astInfo.nodeType);
                w.append("%s.holder = %s;", name, pname);
            }
        }
        Map<Name, List<Variable>> map = nameMap.computeIfAbsent(curSet.stateId, k -> new HashMap<>());
        map.put(sym, vars);
    }

    private void assign(List<Item> list, Name sym) {
        Set<String> done = new HashSet<>();
        //assign created nodes
        for (Item item : list) {
            if (item.dotPos != 0) continue;
            //no alt
            if (item.rule.which == -1) {
                String name = getName(item.rule.retType, sym);
                for (Item sender : senders(item, list)) {
                    if (sender.getDotNode().astInfo.isInLoop) {
                        w.append("%s.%s.add(%s);", getName(getType(sender), sym), sender.getDotNode().astInfo.varName, name);
                    }
                    else {
                        w.append("%s.%s = %s;", getName(getType(sender), sym), sender.getDotNode().astInfo.varName, name);
                    }
                }
            }
            else {
                //alt holder
                if (!done.contains(item.rule.baseName()) && !item.lookAhead.contains(dollar)) {
                    done.add(item.rule.baseName());
                    for (Item sender : item.senders) {
                        Name node = has(sender, item.rule.ref);
                        if (node.astInfo.isInLoop) {
                            w.append("%s.%s.add(%s);", getBoth(sender, sym), node.astInfo.varName, getName(item.rule.retType, sym));
                        }
                        else {
                            //todo sender dot empty, .A* B
                            w.append("%s.%s = %s;", getBoth(sender, sym), node.astInfo.varName, getName(item.rule.retType, sym));
                        }
                    }
                }
            }
        }
    }

    List<Item> senders(Item item, List<Item> list) {
        List<Item> res = new ArrayList<>();
        for (Item sender : list) {
            if (sender.getDotNode().equals(item.rule.ref)) {
                res.add(sender);
            }
        }
        return res;
    }

    private void writeReduces(ItemSet set) {
        for (Item item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (item.rule.which == -1) continue;//only alts have assign
            w.append("if(%s){", JavaRecDescent.loopExpr(item.lookAhead));
            //String holder = getParam(item.rule.retType);
            String holder;
            if (skipHolder) {
                holder = getParam(item) + ".holder";
            }
            else {
                holder = getParam(item.rule.retType);
            }
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getParam(item));
            w.append("}");
        }
    }

    private void inline(ItemSet set, Name sym) {
        for (Item item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (item.rule.which == -1) continue;//only alts have assign
            w.append("if(%s){", JavaRecDescent.loopExpr(item.lookAhead));
            String holder;
            if (skipHolder) {
                holder = getBoth(item, sym) + ".holder";
            }
            else {
                if (curSet.isStart) {
                    holder = getName(item.rule.retType, sym);
                }
                else {
                    holder = getParam(item.rule.retType);
                }
            }
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getBoth(item, sym));//todo
            w.append("}");
        }
    }

    Type getType(Item item) {
        if (item.rule.which == -1) {
            return item.rule.retType;
        }
        else {
            return item.rule.rhs.astInfo.nodeType;
        }
    }

    private Map<Name, List<Item>> group(ItemSet set) {
        Map<Name, List<Item>> groups = new HashMap<>();
        for (Item item : set.all) {
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
                    List<Item> list = groups.getOrDefault(token, new ArrayList<>());
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

    Name has(Item item, Name sym) {
        //todo loop
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            Node ch = item.getNode(i);
            if (ItemSet.sym(ch).equals(sym)) {
                return ItemSet.sym(ch);
            }
            if (!FirstSet.canBeEmpty(ch, tree)) {
                break;
            }
        }
        return null;
    }
}
