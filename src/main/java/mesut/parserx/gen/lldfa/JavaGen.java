package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.RDParserGen;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.gen.targets.JavaRecDescent;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JavaGen {
    LLDfaBuilder builder;
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;
    Map<Integer, Map<Node, List<Variable>>> localMap = new HashMap<>();
    Map<Integer, List<Variable>> paramMap = new HashMap<>();
    ItemSet curSet;

    public JavaGen(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        tree = builder.tree;
        initLocals();
        initParams();
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

        for (var ruleName : builder.rules.keySet()) {
            rule = tree.getRule(ruleName);
            writeRule(builder.rules.get(ruleName));
        }
        for (var decl : builder.ebnf.rules) {
            if (builder.rules.containsKey(decl.baseName())) continue;
            rule = decl;
            writeNormal();
        }

        w.append("}");

        var file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(w.get(), file);
        JavaRecDescent.genTokenType(tree);
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
            int alt = 1;
            for (var ch : or) {
                w.append("%sif(%s){", alt > 1 ? "else " : "", JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree)));
                w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
                w.append("%s.holder = res;", ch.astInfo.varName);
                w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
                w.append("res.which = %s;", alt);
                ch.accept(this, arg);
                w.append("}");
                alt++;
            }
            w.append("else throw new RuntimeException(\"expecting one of %s got: \"+la);", FirstSet.tokens(or, tree));
            return null;
        }

        @Override
        public Void visitName(Name name, Void arg) {
            consumer(name, name.astInfo.outerVar);
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            var ch = regex.node;
            var la = JavaRecDescent.loopExpr(FirstSet.tokens(ch, tree));
            if (regex.isOptional()) {
                w.append("if(%s){", la);
                ch.accept(this, null);
                w.append("}");
            }
            else if (regex.isStar()) {
                w.append("while(%s){", la);
                ch.accept(this, null);
                w.append("}");
            }
            else {
                w.append("do{");
                ch.accept(this, null);
                w.down();
                w.append("}while(%s);", la);
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
        for (var set : all) {
            writeSet(set);
        }
    }

    void writeSet(ItemSet set) {
        curSet = set;
        if (set.transitions.isEmpty()) return;
        var type = set.isStart ? rule.retType : "void";
        var name = set.isStart ? rule.getName() : "S" + set.stateId;
        w.append("public %s %s(%s) throws IOException{", type, name, params(set));

        writeReduces();
        var first = true;
        Set<Name> allLa = new HashSet<>();
        //collect items by la
        var groups = groupByToken();
        for (var tr : set.transitions) {
            if (tr.symbol.isName() && tr.symbol.asName().isToken) {
                var sym = tr.symbol.asName();
                allLa.add(sym);
                var items = groups.get(sym);
                w.append("%sif(la.type == Tokens.%s){", first ? "" : "else ", sym.name);
                first = false;
                //create and assign nodes
                createNodes(sym);
                assign(items, sym);

                //assign la
                for (var item : items) {
                    var node = findSym(item, sym);
                    if (node == null) continue;
                    assignStr(getBoth(item, sym), node, "la");
                }
                //preReduce(sym);
                //get next la
                //todo beginning of every state is better
                w.append("la = lexer.%s();", options.lexerFunction);
                //inline target reductions
                handleTarget(sym, tr.target, tr);
                w.append("}");//if
            }
            else {
                var sym = tr.symbol;
                var laList = FirstSet.tokens(sym, tree);
                allLa.addAll(laList);
                w.append("%sif(%s){", first ? "" : "else ", JavaRecDescent.loopExpr(laList));
                first = false;

                //create and assign nodes
                String vname = null;
                if (localMap.containsKey(curSet.stateId) && localMap.get(curSet.stateId).containsKey(sym) && !localMap.get(curSet.stateId).get(sym).isEmpty()) {
                    var locals = localMap.get(curSet.stateId).get(sym);
                    for (var local : locals) {
                        w.append("%s %s = new %s();", local.type, local.name, local.type);
                        if (!local.isHolder()) {
                            if (local.item.isAlt()) {
                                w.append("%s.holder = %s;", local.name, local.holder.name);
                            }
                            vname = local.name;
                        }
                    }
                }
                else {
                    Item item = null;
                    for (var it : set.all) {
                        if (it.dotPos == it.rhs.size()) continue;
                        var rest = Sequence.make(it.rhs.list.subList(it.dotPos, it.rhs.size()));
                        if (hasCommon(rest, sym)) {
                            if (item != null) throw new RuntimeException("can not happen");
                            item = it;
                        }
                    }
                    vname = getParam(item).name;
                }

                //consume all
                var seq = sym.isSequence() ? sym.asSequence() : new Sequence(sym);
                for (var ch : seq) {
                    if (ch.isName()) {
                        consumer(ch.asName(), vname);
                    }
                    else if (ch.isRegex()) {
                        var regex = ch.asRegex();
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
                handleTarget(sym, tr.target, tr);
                w.append("}");
            }
        }
        if (!set.transitions.isEmpty() && set.isStart) {
            w.append("else throw new RuntimeException(\"expecting one of %s got: \"+la);", allLa);
        }
        w.append("}");
    }

    String params(ItemSet set) {
        if (set.isStart) return "";
        var sb = new StringBuilder();
        int cnt = 0;

        for (var e : paramMap.get(set.stateId)) {
            if (cnt++ > 0) {
                sb.append(", ");
            }
            sb.append(e.type);
            sb.append(" ");
            sb.append(e.name);
        }

        return sb.toString();
    }

    void initLocals() {
        for (var entry : builder.rules.entrySet()) {
            rule = tree.getRule(entry.getKey());
            for (var set : entry.getValue()) {
                initLocals(set);
            }
        }
    }

    private void initLocals(ItemSet set) {
        curSet = set;
        for (var tr : set.transitions) {
            if (tr.symbol.isName() && tr.symbol.asName().isToken) {
                var sym = tr.symbol.asName();
                var items = tr.pairs.stream().map(pair -> pair.origin).collect(Collectors.toList());
                var group = groupByToken();
                var locals = genLocals(group.get(sym));
                //var locals = genLocals(items);
                localMap.computeIfAbsent(set.stateId, k -> new HashMap<>()).put(sym, locals);
            }
            else {
                //multi
                Item item = null;
                for (var it : set.all) {
                    if (it.dotPos != 0) continue;
                    var rest = Sequence.make(it.rhs.list.subList(it.dotPos, it.rhs.size()));
                    if (hasCommon(rest, tr.symbol)) {
                        item = it;
                        break;
                    }
                }
                if (item == null) continue;
                var locals = new ArrayList<Variable>();
                if (!item.isAlt()) {
                    locals.add(new Variable(item.rule.retType, "v0", item));
                }
                else {
                    //holder
                    var holder = new Variable(item.rule.retType, "v0", item.siblings);
                    locals.add(holder);
                    //alt
                    locals.add(new Variable(item.rule.rhs.astInfo.nodeType, "v1", item, holder));
                }
                localMap.computeIfAbsent(set.stateId, k -> new HashMap<>()).put(tr.symbol, locals);
            }
        }
    }

    private List<Variable> genLocals(List<Item> items) {
        var vars = new ArrayList<Variable>();
        var done = new HashSet<String>();
        int cnt = 0;
        var holderMap = new HashMap<Type, Variable>();
        for (var item : items) {
            if (item.dotPos > 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                var name = "v" + cnt++;
                vars.add(new Variable(item.rule.retType, name, item));
            }
            else {
                //holder
                if (!done.contains(item.rule.baseName())) {
                    var parentName = "v" + cnt++;
                    var holderVar = new Variable(item.rule.retType, parentName, item.siblings);
                    vars.add(holderVar);
                    done.add(item.rule.baseName());
                    holderMap.put(holderVar.type, holderVar);
                }
                //alt
                var name = "v" + cnt++;
                var holderVar = holderMap.get(item.rule.retType);
                vars.add(new Variable(item.rule.rhs.astInfo.nodeType, name, item, holderVar));

            }
        }
        return vars;
    }

    void initParams() {
        for (var entry : builder.rules.entrySet()) {
            rule = tree.getRule(entry.getKey());
            for (var set : entry.getValue()) {
                initParam(set);
            }
        }
    }

    void initParam(ItemSet set) {
        curSet = set;
        for (var tr : set.transitions) {
            if (tr.target.transitions.isEmpty()) continue;//inlined no params
            //if (paramMap.containsKey(tr.target.stateId)) continue;//already done
            if (tr.symbol.isName() && tr.symbol.asName().isToken) {
                initParamNormal(set, tr);
            }
            else {
                initParamMultiSym(set, tr);
            }
        }
    }

    private void initParamNormal(ItemSet set, LLTransition tr) {
        var sym = tr.symbol.asName();
        var locals = localMap.get(set.stateId).get(sym);
        locals = locals.stream().filter(v -> !v.isHolder()).collect(Collectors.toList());

        if (paramMap.containsKey(tr.target.stateId)) {
            //check local overlaps param
            handleOverlap(tr.target, locals);
        }
        else {
            //check local overlaps param
            //check cur param overlaps param
            //transfer
            //init target params
            var params = new ArrayList<Variable>();
            //cur params+locals
            int cnt = 0;
            if (paramMap.containsKey(set.stateId)) {
                for (var param : paramMap.get(set.stateId)) {
                    if (isReduced(param)) continue;
                    params.add(makeParam(param, "p" + cnt++));
                }
            }
            for (var local : locals) {
                //overlap
                params.add(makeParam(local, "p" + cnt++));
            }
            paramMap.put(tr.target.stateId, params);
        }
        if (paramMap.containsKey(set.stateId)) {
            handleOverlap(tr.target, paramMap.get(set.stateId));
        }
    }

    private void initParamMultiSym(ItemSet set, LLTransition tr) {
        var params = new ArrayList<Variable>();
        //cur params+locals
        if (paramMap.containsKey(set.stateId)) {
            int cnt = 0;
            for (var param : paramMap.get(set.stateId)) {
                if (isReduced(param)) continue;
                params.add(makeParam(param, "p" + cnt++));
            }
        }
        paramMap.put(tr.target.stateId, params);
    }

    Variable makeParam(Variable from, String name) {
        Variable res;
        if (from.item == null) {
            res = new Variable(from.type, name, from.children);
        }
        else {
            res = new Variable(from.type, name, from.item, from.holder);
        }
        res.prevs.add(from);
        return res;
    }

    void handleOverlap(ItemSet target, List<Variable> locals) {
        if (!paramMap.containsKey(target.stateId)) return;
        for (var prm : paramMap.get(target.stateId)) {
            //if two item overlap, convert it to stack
            for (var local : locals) {
                if (local.isHolder() || prm.isHolder()) continue;
                if (overlap(local.item, prm.item)) {
                    throw new RuntimeException("overlap");
                    //break;
                }
            }
        }
    }

    boolean overlap(Item i1, Item i2) {
        //has common but not all same
        if (i1.ids.equals(i2.ids)) return false;
        return hasCommonId(i1.ids, i2.ids);
    }


    private void handleTarget(Node sym, ItemSet target, LLTransition tr) {
        if (target.transitions.isEmpty()) {
            //inline final state
            inline(target, sym, tr);
        }
        else {
            callTarget(target, sym);
        }
        if (curSet.isStart) {
            for (Variable v : localMap.get(curSet.stateId).get(sym)) {
                if (v.type.equals(rule.retType)) {
                    w.append("return %s;", v.name);
                    break;
                }
            }
        }
    }

    Map<Set<Name>, List<Item>> collectReduces(ItemSet set) {
        var map = new HashMap<Set<Name>, List<Item>>();
        for (var item : set.all) {
            if (!item.isReduce(tree)) continue;
            if (!item.isAlt()) continue;//skip normal
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
            rhs = String.format("consume(%s.%s, \"%s\")", RDParserGen.tokens, name.name, name.name);
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

    void callTarget(ItemSet target, Node sym) {
        var targetParams = paramMap.get(target.stateId);
        var sb = new StringBuilder();
        var first = true;
        for (var prm : targetParams) {
            if (!first) sb.append(", ");
            sb.append(getBothVar(prm.item, sym).name);
            first = false;
        }
        w.print("S%d(%s);\n", target.stateId, sb, sym, target);
    }

    boolean isReduced(Variable param) {
        if (param.isHolder()) {
            //throw new IllegalStateException("prm can never be holder,it has to be an alt or normal item");
            for (var item : curSet.all) {
                if (!item.isReduce(tree)) continue;
                //prm is holder of local item
                for (var ch : param.children) {
                    if (sameItem(item, ch)) return true;
                }
            }
            return false;
        }
        for (var item : curSet.all) {
            if (!item.isReduce(tree)) continue;
            if (sameItem(item, param.item)) return true;//recursion forces reduce
        }
        return false;
    }

    boolean hasCommonId(Set<Integer> s1, Set<Integer> s2) {
        var tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        return !tmp.isEmpty();
    }

    boolean sameItem(Item i1, Item i2) {
        return i1.equals(i2) || hasCommonId(i1.ids, i2.ids);
    }

    Variable getLocal(Item item, Node sym) {
        if (!localMap.containsKey(curSet.stateId)) return null;
        var map = localMap.get(curSet.stateId);
        if (map.containsKey(sym)) {
            for (var v : map.get(sym)) {
                if (v.isHolder() || !v.item.rule.equals(item.rule)) continue;
                if (sameItem(v.item, item)) {
                    return v;
                }
            }
        }
        return null;
    }

    Variable getParam(Item item) {
        if (!paramMap.containsKey(curSet.stateId)) return null;
        for (var v : paramMap.get(curSet.stateId)) {
            if (!v.isHolder() && v.item.rule.equals(item.rule) && sameItem(v.item, item)) {
                return v;
            }
        }
        return null;
    }

    String getBoth(Item item, Node sym) {
        return getBothVar(item, sym).name;
    }

    Variable getBothVar(Item item, Node sym) {
        var res = getLocal(item, sym);
        if (res == null) res = getParam(item);
        if (res == null) {
            throw new RuntimeException();
        }
        return res;
    }

    String getHolder(Item item, Node sym) {
        //if (sym == null) return getParam(item).name + ".holder";
        return getBoth(item, sym) + ".holder";
    }

    Variable holderVar(Item item, Node sym) {
        if (localMap.containsKey(curSet.stateId)) {
            for (var v : localMap.get(curSet.stateId).get(sym)) {
                if (v.isHolder() && v.children.contains(item)) {
                    return v;
                }
            }
        }
        if (paramMap.containsKey(curSet.stateId)) {
            for (var v : paramMap.get(curSet.stateId)) {
                if (v.isHolder() && v.children.contains(item)) {
                    return v;
                }
            }
        }
        return null;
    }

    private void createNodes(Node sym) {
        if (!localMap.containsKey(curSet.stateId)) return;
        var vars = localMap.get(curSet.stateId).get(sym);
        for (var v : vars) {
            w.append("%s %s = new %s();", v.type, v.name, v.type);
            if (!v.isHolder() && v.item.isAlt()) {
                w.append("%s.holder = %s;", v.name, v.holder.name);
            }
        }
    }

    //assign created nodes to parents
    private void assign(List<Item> list, Name sym) {
        Set<String> done = new HashSet<>();
        for (var item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                //normal item
                for (var parent : item.parents) {
                    var node = findSym(parent, item.rule.ref);
                    var parentName = getBoth(parent, sym);
                    var name = getBoth(item, sym);
                    assignStr(parentName, node, name);
                }
            }
            else {
                //alt holder
                if (!done.contains(item.rule.baseName())) {
                    done.add(item.rule.baseName());
                    for (var parent : item.parents) {
                        var node = findSym(parent, item.rule.ref);
                        var parentName = getBoth(parent, sym);
                        var name = holderVar(item, sym).name;
                        assignStr(parentName, node, name);
                    }
                }
            }
        }
    }

    void assignStr(String parent, Node node, String name) {
        if (node.astInfo.isInLoop) {
            w.append("%s.%s.add(%s);", parent, node.astInfo.varName, name);
        }
        else {
            //todo sender dot empty, .A* B
            w.append("%s.%s = %s;", parent, node.astInfo.varName, name);
        }
    }

    private void writeReduces() {
        for (var e : collectReduces(curSet).entrySet()) {
            var la = e.getKey();
            w.append("if(%s){", JavaRecDescent.loopExpr(la));
            for (var item : e.getValue()) {
                if (!item.isAlt()) continue;
                var prm = getParam(item);
                if (prm.isHolder()) {//simple is used
                    w.append("%s.which = %d;", prm.name, item.rule.which);
                }
                else {
                    var name = prm.name;
                    var holder = name + ".holder";
                    w.append("%s.which = %d;", holder, item.rule.which);
                    w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, name);
                }

            }
            w.append("}");
        }
    }

    //inline target reductions if final
    private void inline(ItemSet target, Node sym, LLTransition tr) {
        for (var item : target.all) {
            if (!item.isReduce(tree)) continue;
            if (!item.isAlt()) continue;//only alts have assign
            w.append("if(%s){", JavaRecDescent.loopExpr(item.lookAhead));
            var holder = getHolder(item, sym);
            var name = getBoth(item, sym);
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, name);
            w.append("}");
        }
    }

    //group items by la
    private Map<Name, List<Item>> groupByToken() {
        var groups = new HashMap<Name, List<Item>>();
        for (var item : curSet.all) {
            if (item.dotPos == item.rhs.size()) continue;
            var rest = Sequence.make(item.rhs.list.subList(item.dotPos, item.rhs.size()));
            var tokens = FirstSet.tokens(rest, tree);
            for (var token : tokens) {
                var list = groups.getOrDefault(token, new ArrayList<>());
                list.add(item);
                groups.put(token, list);
            }
        }
        return groups;
    }

    Name findSym(Item item, Name sym) {
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            var ch = item.getNode(i);
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
