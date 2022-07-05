package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.targets.JavaRecDescent;
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
    Map<Integer, Map<Node, List<Variable>>> nameMap = new HashMap<>();
    Map<Integer, List<Variable>> paramMap = new HashMap<>();
    ItemSet curSet;
    private Set<Item> preReduced = new HashSet<>();

    public JavaGen(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        tree = builder.tree;
        if (true) {
            initParams();
            return;
        }

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

        writeConsume();
        //builder.inlined.forEach(set -> System.out.printf("inlined=%d\n", set.stateId));

        for (String ruleName : builder.rules.keySet()) {
            //if (!ruleName.equals("E")) continue;
            rule = tree.getRule(ruleName);
            writeRule(builder.rules.get(ruleName));
        }
        for (RuleDecl decl : builder.ebnf.rules) {
            if (builder.rules.containsKey(decl.baseName())) continue;
            rule = decl;
            writeNormal();
        }

        w.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(w.get(), file);
        JavaRecDescent recDescent = new JavaRecDescent(tree);
        recDescent.genTokenType();
    }

    private void writeNormal() {
        w.append("public %s %s() throws IOException{", rule.retType, rule.baseName());
        w.append("%s res = new %s();", rule.retType, rule.retType);
        rule.rhs.accept(new NormalWriter(), null);
        w.append("return res;");
        w.append("}");
    }

    class NormalWriter extends BaseVisitor<Void, Void> {

        @Override
        public Void visitOr(Or or, Void arg) {
            int id = 1;
            for (Node ch : or) {
                w.append("%sif(%s){", id > 1 ? "else " : "", JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree)));
                w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
                w.append("%s.holder = res;", ch.astInfo.varName);
                w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
                w.append("res.which = %s;", id++);
                ch.accept(this, arg);
                w.append("}");
            }
            w.append("else throw new RuntimeException(\"expecting one of %s got: \"+la);", FirstSet.tokens(or, tree));
            return null;
        }

        @Override
        public Void visitSequence(Sequence seq, Void arg) {
            return super.visitSequence(seq, arg);
        }

        @Override
        public Void visitName(Name name, Void arg) {
            consumer(name, name.astInfo.outerVar);
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            Node ch = regex.node;
            if (regex.isOptional()) {
                w.append("if(%s){", JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree)));
                ch.accept(this, null);
                w.append("}");
            }
            else if (regex.isStar()) {
                w.append("while(%s){", JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree)));
                ch.accept(this, null);
                w.append("}");
            }
            else {
                w.append("do{");
                ch.accept(this, null);
                w.down();
                w.append("}while(%s);", JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree)));
            }
            return null;
        }
    }

    void writeConsume() {
        w.append("%s consume(int type, String name){", options.tokenClass);
        w.append("if(la.type != type){");
        w.append("throw new RuntimeException(\"unexpected token: \" + la + \" expecting: \" + name);");
        w.all("}");
        w.append("try{");
        w.append("%s res = la;", options.tokenClass);
        w.append("la = lexer.next();");
        w.append("return res;");
        w.all("}\ncatch(IOException e){");
        w.append("throw new RuntimeException(e);");
        w.append("}");
        w.append("}");
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

    void writeSet(ItemSet set) {
        curSet = set;
        if (set.transitions.isEmpty()) return;
        if (set.isStart) {
            w.append("public %s %s(%s) throws IOException{", rule.retType, rule.getName(), params(set));
        }
        else {
            w.append("public void S%d(%s) throws IOException{", set.stateId, params(set));
        }

        writeReduces();
        boolean first = true;
        Set<Name> allLa = new HashSet<>();
        //collect items by la
        Map<Name, List<Item>> groups = groupByToken();
        for (Transition tr : set.transitions) {
            if (tr.symbol.isName() && tr.symbol.asName().isToken) {
                Name sym = tr.symbol.asName();
                allLa.add(sym);
                List<Item> list = groups.get(sym);
                w.append("%sif(la.type == Tokens.%s){", first ? "" : "else ", sym.name);
                first = false;
                //create and assign nodes
                createNodes(list, sym);
                assign(list, sym);

                //assign la
                for (Item item : list) {
                    Name node = has(item, sym);
                    if (node == null) continue;
                    assignStr(w, getBoth(item, sym), "la", node);
                }
                //preReduce(sym);
                //get next la
                //todo beginning of every state is better
                w.append("la = lexer.%s();", options.lexerFunction);
                //inline target reductions
                handleTarget(sym, tr.target);
                w.append("}");//if
            }
            else {
                Node sym = tr.symbol;
                Sequence seq = sym.isSequence() ? sym.asSequence() : new Sequence(sym);
                Set<Name> laList = FirstSet.tokens(sym, tree);
                allLa.addAll(laList);
                w.append("%sif(%s){", first ? "" : "else ", JavaRecDescent.loopExpr(laList));
                first = false;
                Item item;
                List<Item> items = new ArrayList<>();
                for (Item it : set.all) {
                    if (it.dotPos == it.rhs.size()) continue;
                    Node rest = Sequence.make(it.rhs.list.subList(it.dotPos, it.rhs.size()));
                    if (hasCommon(rest, sym)) {
                        items.add(it);
                    }
                }
                if (items.size() != 1) throw new RuntimeException("can not happen");
                item = items.get(0);
                //create and assign nodes
                String vname;
                if (item.dotPos == 0) {
                    List<Variable> vars = new ArrayList<>();
                    if (!item.isAlt()) {
                        vname = "v0";
                        w.append("%s %s = new %s();", item.rule.retType, vname, item.rule.retType);
                        vars.add(new Variable(item.rule.retType, vname, item));
                    }
                    else {
                        //holder
                        String pname = "v0";
                        vname = "v1";
                        vars.add(new Variable(item.rule.retType, pname, item.siblings));
                        w.append("%s %s = new %s();", item.rule.retType, pname, item.rule.retType);
                        //alt
                        w.append("%s %s = new %s();", item.rule.rhs.astInfo.nodeType, vname, item.rule.rhs.astInfo.nodeType);
                        w.append("%s.holder = %s;", vname, pname);
                        vars.add(new Variable(item.rule.rhs.astInfo.nodeType, vname, item));
                    }
                    Map<Node, List<Variable>> map = nameMap.computeIfAbsent(curSet.stateId, k -> new HashMap<>());
                    map.put(sym, vars);
                }
                else {
                    vname = getParam(item);
                }

                //consume all
                for (Node ch : seq) {
                    if (ch.isName()) {
                        consumer(ch.asName(), vname);
                    }
                    else if (ch.isRegex()) {
                        Regex regex = ch.asRegex();
                        if (regex.isOptional()) {
                            w.append("if(%s){", JavaRecDescent.loopExpr(FirstSet.tokens(regex.node, tree)));
                            consumer(regex.node.asName(), vname);
                            w.append("}");
                        }
                        else if (regex.isStar()) {
                            w.append("while(%s){", JavaRecDescent.loopExpr(FirstSet.tokens(regex.node, tree)));
                            consumer(regex.node.asName(), vname);
                            w.append("}");
                        }
                    }
                }

                //inline target reductions
                handleTarget(sym, tr.target);
                w.append("}");
            }
        }
        if (!set.transitions.isEmpty() && set.isStart) {
            w.append("else throw new RuntimeException(\"expecting one of %s got: \"+la);", allLa);
        }
        w.append("}");
    }

    boolean overlap(Item i1, Item i2) {
        //has common but not all same
        if (i1.ids.equals(i2.ids)) return false;
        return hasCommon(i1.ids, i2.ids);
    }

    void initParams() {
        for (var entry : builder.rules.entrySet()) {
            rule = tree.getRule(entry.getKey());
            for (ItemSet set : entry.getValue()) {
                curSet = set;
                for (var tr : set.transitions) {
                    var sym = tr.symbol.asName();
                    var group = groupByToken();
                    var locals = createNodes2(group.get(sym));
                    nameMap.computeIfAbsent(set.stateId, k -> new HashMap<>()).put(sym, locals);
                    handleOverlap(tr.target, locals);//todo what if cur param overlaps
                    //init target params
                    List<Variable> params = new ArrayList<>();
                    //cur params+locals
                    for (Variable param : paramMap.getOrDefault(set.stateId, new ArrayList<>())) {
                        if (isReduced(param)) continue;
                        params.add(param);
                    }
                    for (Variable local : locals) {
                        if (local.item != null) {//non holder
                            params.add(local);
                        }
                    }
                    paramMap.put(tr.target.stateId, params);
                }
            }
        }
    }

    void handleOverlap(ItemSet target, List<Variable> locals) {
        if (!paramMap.containsKey(target.stateId)) return;
        for (Variable prm : paramMap.get(target.stateId)) {
            if (prm.item == null) continue;
            //if two item overlap, convert it to stack
            for (Variable local : locals) {
                if (local.item == null) continue;
                if (overlap(local.item, prm.item)) {
                    prm.isArray = true;
                    break;
                }
            }
        }
    }

    private List<Variable> createNodes2(List<Item> list) {
        List<Variable> vars = new ArrayList<>();
        Set<String> done = new HashSet<>();
        int cnt = 0;
        for (Item item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                String name = "v" + cnt++;
                vars.add(new Variable(item.rule.retType, name, item));
            }
            else {
                //parent
                if (!done.contains(item.rule.baseName())) {
                    String pname = "v" + cnt++;
                    vars.add(new Variable(item.rule.retType, pname, item.siblings));
                    done.add(item.rule.baseName());
                }
                //alt
                String name = "v" + cnt++;
                vars.add(new Variable(item.rule.rhs.astInfo.nodeType, name, item));
            }
        }
        return vars;
    }


    private void handleTarget(Node sym, ItemSet target) {
        if (target.transitions.isEmpty()) {
            //inline final state
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
                CodeWriter to = new CodeWriter(true);
                w.print("S%d(%s);\n", target.stateId, args2(paramMap.get(target.stateId), sym, to));
                if (to.sb.length() > 0) {
                    for (String line : to.sb.toString().split("\n")) {
                        w.append(line);
                    }
                }
            }
        }
        if (curSet.isStart) {
            for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
                if (v.type.equals(rule.retType)) {
                    w.append("return %s;", v.name);
                    break;
                }
            }
        }
    }

    Map<Set<Name>, List<Item>> collectReduces(ItemSet set) {
        Map<Set<Name>, List<Item>> map = new HashMap<>();
        for (var item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (!item.isAlt()) continue;//skip normal
            if (preReduced.contains(item)) continue;
            var la = item.lookAhead;
            var list = map.getOrDefault(la, new ArrayList<>());
            list.add(item);
            map.put(la, list);
        }
        return map;
    }

    private void consumer(Name name, String vname) {
        String rhs;
        if (name.isToken) {
            rhs = String.format("consume(%s.%s, \"%s\")", RecDescent.tokens, name.name, name.name);
        }
        else {
            rhs = name.name + "()";
        }
        if (name.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", vname, name.astInfo.varName, rhs);
        }
        else {
            w.append("%s.%s = %s;", vname, name.astInfo.varName, rhs);
        }
    }

    private boolean hasCommon(Node n1, Node n2) {
        Set<Name> s1 = FirstSet.tokens(n1, tree);
        Set<Name> s2 = FirstSet.tokens(n2, tree);
        Set<Name> set = new HashSet<>(s1);
        set.retainAll(s2);
        return !set.isEmpty();
    }

    //can we reach its reduction, if not don't carry it anymore
    boolean isReachable(Item item) {
        Item reducer = new Item(item, item.rhs.size());
        Queue<ItemSet> queue = new LinkedList<>();
        queue.add(curSet);
        Set<ItemSet> done = new HashSet<>();
        while (!queue.isEmpty()) {
            ItemSet set = queue.poll();
            for (var tr : set.transitions) {
                for (var it : tr.target.all) {
                    if (it.isSame(reducer)) {
                        return true;
                    }
                }
                if (done.add(tr.target)) {
                    queue.add(tr.target);
                }
            }
        }
        return false;
    }

    boolean canPreReduce(Item item, Node sym) {
        //todo just target state enough?
        for (var tr : curSet.transitions) {
            if (tr.symbol.equals(sym)) {
                //does target state has my sibling
                for (var trg : tr.target.all) {
                    for (var sib : item.siblings) {
                        if (!sib.equals(item) && trg.rule.equals(sib.rule)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    String args(List<Variable> targetParams, Node sym) {
        StringBuilder sb = new StringBuilder();
        int param_cnt = 0;
        //curParams + createdVars
        boolean hasParams = false;
        if (paramMap.containsKey(curSet.stateId)) {
            boolean first = true;
            for (Variable v : paramMap.get(curSet.stateId)) {
                if (isReduced(v)) continue;
                if (!first) sb.append(", ");
                sb.append(v.name);
                targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
                first = false;
                hasParams = true;
            }
        }
        if (nameMap.containsKey(curSet.stateId)) {
            boolean first = true;
            for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
                if (v.item == null) continue;//skip holder, alt already has ref in it
                if (!first || hasParams) sb.append(", ");
                sb.append(v.name);
                targetParams.add(new Variable(v.type, "p" + param_cnt++, v.item));
                first = false;
            }
        }
        return sb.toString();
    }

    String args2(List<Variable> targetParams, Node sym, CodeWriter to) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Variable prm : targetParams) {
            //locate by item
            if (prm.item == null) {
                throw new RuntimeException("not yet");
            }
            if (!first) sb.append(", ");
            boolean takeover = false;
//            for (Variable local : nameMap.get(curSet.stateId).get(sym)) {
//                if (local.item == null || prm.item == null) continue;
//                if (sameItem(local.item, prm.item)) {
//                    sb.append(getBoth(local.item, sym));
//                    //local var(local.item) takeover param(prm.item)
//                    takeover = true;
//                    //reduce old item, since we'll lose it and no way to reduce it later
//                    writeAs(prm.item, sym, to);
//                    break;
//                }
//            }
            if (!takeover) {
                String name = getLocal(prm.item, sym, false);//local has priority
                if (name == null) name = getParam(prm.item);
                sb.append(name);
            }
            first = false;
        }
        return sb.toString();
    }

    void writeAs(Item item, Node sym, CodeWriter w) {
        String holder = getHolder(item, sym);
        w.append("%s.which = %d;", holder, item.rule.which);
        w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getBoth(item, sym));
    }

    boolean isReduced(Variable param) {
        if (param.item == null) {
            throw new IllegalStateException("prm can never be holder,it has to be an alt or normal item");
        }
        for (Item item : curSet.all) {
            if (!item.isReduce(tree)) continue;
            if (sameItem(item, param.item)) return true;
        }
        return false;
    }

    Set<Variable> filterReduced() {
        Set<Variable> res = new HashSet<>();
        for (Item item : curSet.all) {
            if (!item.isReduce(tree)) continue;
            //mark holder
            for (Variable v : paramMap.get(curSet.stateId)) {
                //remove holder
                if (v.item == null && v.type.equals(item.rule.retType)) {
                    res.add(v);
                }
                //alt
                if (v.item != null && item.isAlt() && v.type.equals(item.rule.rhs.astInfo.nodeType)) {
                    res.add(v);
                }
                //normal
                if (v.item != null && !item.isAlt() && v.type.equals(item.rule.rhs.astInfo.nodeType)) {
                    res.add(v);
                }
            }
        }
        return res;
    }

    boolean hasCommon(Set<Integer> s1, Set<Integer> s2) {
        Set<Integer> tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        return !tmp.isEmpty();
    }

//    String getLocal(Type type, Node sym) {
//        if (nameMap.containsKey(curSet.stateId)) {
//            var map = nameMap.get(curSet.stateId);
//            if (map.containsKey(sym)) {
//                for (Variable v : map.get(sym)) {
//                    if (v.type.equals(type)) return v.name;
//                }
//            }
//        }
//        return null;
//    }

    String getLocal(Item item, Node sym, boolean exact) {
        if (!nameMap.containsKey(curSet.stateId)) return null;
        var map = nameMap.get(curSet.stateId);
        if (map.containsKey(sym)) {
            for (Variable v : map.get(sym)) {
                if (v.item == null || !v.item.rule.equals(item.rule)) continue;
                if (exact && v.item.equals(item) && v.item.ids.equals(item.ids) || !exact && sameItem(v.item, item)) {
                    return v.name;
                }
            }
        }
        return null;
    }

    boolean sameItem(Item i1, Item i2) {
        return i1.equals(i2) || hasCommon(i1.ids, i2.ids);
    }

    String getParam(Item item) {
        if (!paramMap.containsKey(curSet.stateId)) return null;
        for (Variable v : paramMap.get(curSet.stateId)) {
            if (v.item != null && v.item.rule.equals(item.rule) && sameItem(v.item, item)) {
                return v.name;
            }
        }
        return null;
    }

    String getExact(Item item, Node sym) {
        String res = getLocal(item, sym, true);
        if (res == null) res = getParam(item);
        if (res == null) throw new RuntimeException();
        return res;
    }


    String getBoth(Item item, Node sym) {
        String res = getLocal(item, sym, false);
        if (res == null) res = getParam(item);
        if (res == null) {
            return "null";
            //throw new RuntimeException();
        }
        return res;
    }

    private void createNodes(List<Item> list, Node sym) {
        List<Variable> vars = new ArrayList<>();
        Set<String> done = new HashSet<>();
        int cnt = 0;
        Map<Item, String> holderMap = new HashMap<>();
        for (Item item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                String name = "v" + cnt++;
                vars.add(new Variable(item.rule.retType, name, item));
                w.append("%s %s = new %s();", item.rule.retType, name, item.rule.retType);
            }
            else {
                //parent
                if (!done.contains(item.rule.baseName())) {
                    String pname = "v" + cnt++;
                    vars.add(new Variable(item.rule.retType, pname, item.siblings));
                    done.add(item.rule.baseName());
                    w.append("%s %s = new %s();", item.rule.retType, pname, item.rule.retType);
                    holderMap.put(item, pname);
                }
                //alt
                String name = "v" + cnt++;
                String pname = holderMap.get(item);
                //can use getHolder maybe?
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
        Map<Node, List<Variable>> map = nameMap.computeIfAbsent(curSet.stateId, k -> new HashMap<>());
        map.put(sym, vars);
    }

    private void assign(List<Item> list, Name sym) {
        Set<String> done = new HashSet<>();
        //assign created nodes
        for (Item item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            //no alt
            if (!item.isAlt()) {
                String name = getLocal(item, sym, false);
                for (Item sender : item.senders) {
                    Name node = has(sender, item.rule.ref);
                    String senderName = getBoth(sender, sym);
                    assignStr(w, senderName, name, node);
                }
            }
            else {
                //alt holder
                if (!done.contains(item.rule.baseName())) {
                    done.add(item.rule.baseName());
                    for (Item sender : item.senders) {
                        Name node = has(sender, item.rule.ref);
                        String senderName = getExact(sender, sym);
                        String name = holderVar(item, sym);
                        assignStr(w, senderName, name, node);
                    }
                }
            }
        }
    }

    void assignStr(CodeWriter w, String senderName, String name, Node node) {
        if (node.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", senderName, node.astInfo.varName, name);
        }
        else {
            //todo sender dot empty, .A* B
            w.append("%s.%s = %s;", senderName, node.astInfo.varName, name);
        }
    }

    String getHolder(Item item, Node sym) {
        if (sym == null) return getParam(item) + ".holder";
        return getBoth(item, sym) + ".holder";
    }

    String holderVar(Item item, Node sym) {
        if (nameMap.containsKey(curSet.stateId)) {
            for (Variable v : nameMap.get(curSet.stateId).get(sym)) {
                if (v.item == null && v.children.contains(item)) {
                    return v.name;
                }
            }
        }
        if (paramMap.containsKey(curSet.stateId)) {
            for (Variable v : paramMap.get(curSet.stateId)) {
                if (v.item == null && v.children.contains(item)) {
                    return v.name;
                }
            }
        }
        return null;
    }

    void preReduce(Name sym) {
        //todo goto item reduce too
        if (sym.astInfo.isFactor) return;
        for (Item item : curSet.all) {
            if (item.rule.which == -1) continue;
            var node = has(item, sym);
            if (node == null) continue;
            String holder = getHolder(item, sym);
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getBoth(item, sym));
            preReduced.add(item);
        }
    }

    private void writeReduces() {
        for (var e : collectReduces(curSet).entrySet()) {
            var la = e.getKey();
            w.append("if(%s){", JavaRecDescent.loopExpr(la));
            for (Item item : e.getValue()) {
                if (!item.isAlt()) continue;//only alts have assign
                String holder = getHolder(item, null);
                w.append("%s.which = %d;", holder, item.rule.which);
                w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getParam(item));
            }
            w.append("}");
        }
    }

    //inline target reductions if final
    private void inline(ItemSet set, Node sym) {
        for (Item item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (!item.isAlt()) continue;//only alts have assign
            w.append("if(%s){", JavaRecDescent.loopExpr(item.lookAhead));
            String holder = getHolder(item, sym);
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, getBoth(item, sym));//todo
            if (!curSet.isStart && item.lookAhead.contains(LrDFAGen.dollar)) {
                //w.append("return;");//todo dollar is enough?
            }
            w.append("}");
        }
    }

    private Map<Name, List<Item>> groupByToken() {
        Map<Name, List<Item>> groups = new HashMap<>();
        for (Item item : curSet.all) {
            if (item.dotPos == item.rhs.size()) continue;
            Node rest = Sequence.make(item.rhs.list.subList(item.dotPos, item.rhs.size()));
            Set<Name> tokens = FirstSet.tokens(rest, tree);
            for (Name token : tokens) {
                List<Item> list = groups.getOrDefault(token, new ArrayList<>());
                list.add(item);
                groups.put(token, list);
            }
        }
        return groups;
    }

    Name has(Item item, Name sym) {
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
