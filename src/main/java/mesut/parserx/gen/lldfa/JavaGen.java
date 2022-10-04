package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ParserUtils;
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
    ItemSet curSet;
    Common common;

    public JavaGen(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        common = new Common(tree);
        common.gen();
        builder = common.builder;
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
        for (var decl : builder.tree.rules) {
            if (builder.rules.containsKey(decl.baseName())) continue;
            rule = decl;
            writeNormal();
        }

        w.append("}");

        var file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(w.get(), file);
        ParserUtils.genTokenType(tree);
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
                w.append("%sif(%s){", alt > 1 ? "else " : "", ParserUtils.loopExpr(FirstSet.tokens(ch, tree)));
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
            var la = ParserUtils.loopExpr(FirstSet.tokens(ch, tree));
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
        common.curSet = set;
        if (set.transitions.isEmpty()) return;
        var type = set.isStart ? rule.retType : "void";
        var name = set.isStart ? rule.getName() : "S" + set.stateId;
        w.append("public %s %s(%s) throws IOException{", type, name, params(set));

        writeReduces();
        var first = true;
        Set<Name> allLa = new HashSet<>();
        //collect items by la
        //var groups = groupByToken();
        var groups = common.groupBy();
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

                //preReduce(sym);
                //get next la
                //todo beginning of every state is better
                w.append("la = lexer.%s();", options.lexerFunction);
                //inline target reductions
                handleTarget(sym, tr.target, tr);
                w.append("}");//if
            }
            else if (tr.symbol.isName() && !tr.symbol.asName().isToken) {
                var sym = tr.symbol.asName();
                var laList = FirstSet.tokens(sym, tree);
                allLa.addAll(laList);
                var items = groups.get(sym);
                w.append("%sif(%s){", first ? "" : "else ", ParserUtils.loopExpr(laList));
                first = false;
                //create and assign nodes
                createNodes(sym);
                assign(items, sym);

                //preReduce(sym);
                //inline target reductions
                handleTarget(sym, tr.target, tr);
                w.append("}");//if
            }
            else {
                var sym = tr.symbol;
                var laList = FirstSet.tokens(sym, tree);
                allLa.addAll(laList);
                w.append("%sif(%s){", first ? "" : "else ", ParserUtils.loopExpr(laList));
                first = false;

                //create and assign nodes
                String vname = null;
                if (common.localMap.containsKey(curSet.stateId) && common.localMap.get(curSet.stateId).containsKey(sym) && !common.localMap.get(curSet.stateId).get(sym).isEmpty()) {
                    var locals = common.localMap.get(curSet.stateId).get(sym);
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
                        if (common.hasCommon(rest, sym)) {
                            if (item != null) throw new RuntimeException("can not happen");
                            item = it;
                        }
                    }
                    vname = common.getParam(item).name;
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
                            w.append("if(%s){", ParserUtils.loopExpr(FirstSet.tokens(regex.node, tree)));
                            consumer(regex.node.asName(), vname);
                            w.append("}");
                        }
                        else if (regex.isStar()) {
                            w.append("while(%s){", ParserUtils.loopExpr(FirstSet.tokens(regex.node, tree)));
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

        for (var e : common.paramMap.get(set.stateId)) {
            if (cnt++ > 0) {
                sb.append(", ");
            }
            sb.append(e.type);
            sb.append(" ");
            sb.append(e.name);
        }

        return sb.toString();
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
            for (Variable v : common.localMap.get(curSet.stateId).get(sym)) {
                if (v.type.equals(rule.retType)) {
                    w.append("return %s;", v.name);
                    break;
                }
            }
        }
    }

    private void consumer(Name name, String vname) {
        String rhs;
        if (name.isToken) {
            rhs = String.format("consume(%s.%s, \"%s\")", ParserUtils.tokens, name.name, name.name);
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

    void callTarget(ItemSet target, Node sym) {
        var targetParams = common.paramMap.get(target.stateId);
        var sb = new StringBuilder();
        var first = true;
        for (var prm : targetParams) {
            if (!first) sb.append(", ");
            sb.append(common.getBothVar(prm.item, sym).name);
            first = false;
        }
        w.print("S%d(%s);\n", target.stateId, sb, sym, target);
    }

    String getHolder(Item item, Node sym) {
        //if (sym == null) return getParam(item).name + ".holder";
        return common.getBoth(item, sym) + ".holder";
    }

    private void createNodes(Node sym) {
        if (!common.localMap.containsKey(curSet.stateId)) return;
        var vars = common.localMap.get(curSet.stateId).get(sym);
        for (var v : vars) {
            w.append("%s %s = new %s();", v.type, v.name, v.type);
            if (!v.isHolder() && v.item.isAlt()) {
                w.append("%s.holder = %s;", v.name, v.holder.name);
            }
        }
    }

    //assign created nodes to parents
    private void assign(List<Item> list, Name sym) {
        if (!sym.isToken) {
            var type = tree.getRule(sym).retType;
            w.print("%s %s = %s();\n", type, sym.astInfo.varName, sym.name);
        }
        Set<String> done = new HashSet<>();
        for (var item : list) {
            if (item.dotPos != 0) continue;
            if (item.advanced) continue;
            if (!item.isAlt()) {
                //normal item
                for (var parent : item.parents) {
                    var node = common.findSym(parent, item.rule.ref);
                    var parentName = common.getBoth(parent, sym);
                    var name = common.getBoth(item, sym);
                    assignStr(parentName, node, name);
                }
            }
            else {
                //alt holder
                if (!done.contains(item.rule.baseName())) {
                    done.add(item.rule.baseName());
                    for (var parent : item.parents) {
                        var node = common.findSym(parent, item.rule.ref);
                        var parentName = common.getBoth(parent, sym);
                        var name = common.holderVar(item, sym).name;
                        assignStr(parentName, node, name);
                    }
                }
            }
        }
        //assign la
        var rhs = sym.isToken ? "la" : sym.astInfo.varName;
        for (var item : list) {
            var node = common.findSym(item, sym);
            if (node == null) continue;
            assignStr(common.getBoth(item, sym), node, rhs);
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
        for (var e : common.collectReduces(curSet).entrySet()) {
            var la = e.getKey();
            w.append("if(%s){", ParserUtils.loopExpr(la));
            for (var item : e.getValue()) {
                if (!item.isAlt()) continue;
                var prm = common.getParam(item);
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
            w.append("if(%s){", ParserUtils.loopExpr(item.lookAhead));
            var holder = getHolder(item, sym);
            var name = common.getBoth(item, sym);
            w.append("%s.which = %d;", holder, item.rule.which);
            w.append("%s.%s = %s;", holder, item.rhs.astInfo.varName, name);
            w.append("}");
        }
    }
}
