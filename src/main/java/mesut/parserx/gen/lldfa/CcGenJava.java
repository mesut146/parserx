package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.*;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CcGenJava {
    Tree tree;
    Options options;
    CodeWriter w = new CodeWriter(true);
    LLDfaBuilder builder;
    Set<Name> popperRules = new HashSet<>();
    Set<Name> popperRules2 = new HashSet<>();

    public CcGenJava(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        ParserUtils.genTokenType(tree);
        writeTS(options);
        header();
        for (var entry : builder.rules.entrySet()) {
            var rule = entry.getKey();

            var decl = tree.getRule(rule);
            w.append("public %s %s() throws IOException{", decl.retType, rule.name);
            w.append("%s res = new %s();", decl.retType, decl.retType);

            var rhs = decl.rhs.asOr();
            w.append("switch(%s_decide()){", rule.name);
            for (int i = 1; i <= rhs.size(); i++) {
                var ch = rhs.get(i - 1);
                w.append("case %d:{", i);
                w.append("ts.unmark();");
                alt(ch, i, w);
                var n = new NormalWriter(w,tree);
                ch.accept(n, null);
                w.append("break;");
                w.append("}");
            }
            w.append("}");//switch
            w.append("return res;");
            w.append("}");
            writeDecider(rule);
        }
        //writeRest
        for (var decl : tree.rules) {
            if (builder.rules.containsKey(decl.ref)) continue;
            w.append("public %s %s() throws IOException{", decl.retType, decl.getName());
            w.append("%s res = new %s();", decl.retType, decl.retType);
            var nw = new NormalWriter(w,tree);
            decl.rhs.accept(nw, null);
            w.append("return res;");
            w.append("}");

        }
        //poppers
        for (var name : popperRules) {
            var decl = tree.getRule(name);
            w.append("public void %s_pop() throws IOException{", decl.getName());
            var nw = new Decider();
            nw.popper = true;
            decl.rhs.accept(nw, null);
            w.append("}");
        }
        for (var name : popperRules2) {
            if (popperRules.contains(name)) continue;
            var decl = tree.getRule(name);
            w.append("public void %s_pop() throws IOException{", decl.getName());
            var nw = new Decider();
            nw.popper = true;
            decl.rhs.accept(nw, null);
            w.append("}");
        }
        w.append("}");
        var file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(w.get(), file);
    }

    public static void writeTS(Options options) throws IOException {
        File file = new File(options.outDir, "TokenStream.java");
        var temp = new Template("token_stream.java.template");
        if (options.packageName == null) {
            temp.set("package", "");
        }
        else {
            temp.set("package", "package " + options.packageName + ";");
        }
        temp.set("lexer_class", options.lexerClass);
        temp.set("lexer_function", options.lexerFunction);
        temp.set("token_class", options.tokenClass);
        Utils.write(temp.toString(), file);
    }

    void header() {
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("import java.io.IOException;");
        w.append("");
        w.append("public class %s{", options.parserClass);

        w.append("TokenStream ts;", options.lexerClass);
        w.append("");

        w.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);
        w.all("this.ts = new TokenStream(lexer);\n}");
        w.append("");
    }


    void writeDecider(Name rule) {
        var regexBuilder = new La1RegexBuilder(builder);
        regexBuilder.build(rule);
        for (var s : regexBuilder.rules) {
            w.append("public int %s_decide() throws IOException{", rule);
            w.append("int which = 0;");
            var decider = new Decider();
            s.rhs.accept(decider, null);
            w.append("return which;");
            w.append("}");
        }
    }

    static void alt(Node ch, int which, CodeWriter w) {
        w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
        w.append("%s.holder = res;", ch.astInfo.varName);
        w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
        w.append("res.which = %s;", which);
    }

    class Decider extends BaseVisitor<Void, Void> {
        public boolean popper = false;
        boolean inCondition = false;

        @Override
        public Void visitName(Name name, Void arg) {
            if (name.isToken) {
                if (inCondition) {
                    w.append("ts.pop();");
                    inCondition = false;
                }
                else {
                    w.append("ts.pop(%s.%s, \"%s\");", ParserUtils.tokens, name.name, name.name);
                }
            }
            else {
                //todo
                w.append("%s_pop();", name.name);
                if (popper) {
                    popperRules2.add(name);
                }
                else {
                    popperRules.add(name);
                }
            }
            if (name.astInfo.which != -1) {
                w.append("which = %s;", name.astInfo.which);
            }
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            var ch = regex.node;
            var la = ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type");
            if (regex.isOptional()) {
                w.append("if(%s){", la);
                inCondition = true;
                ch.accept(this, arg);
                inCondition = false;
                w.append("}");
            }
            else if (regex.isStar()) {
                w.append("while(%s){", la);
                inCondition = true;
                ch.accept(this, null);
                inCondition = false;
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

        @Override
        public Void visitSequence(Sequence seq, Void arg) {
            for (var ch : seq) {
                ch.accept(this, arg);
            }
            return null;
        }

        @Override
        public Void visitOr(Or or, Void arg) {
            int i = 0;
            for (var ch : or) {
                if (i > 0) {
                    w.append("else if(%s){", ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type"));
                }
                else {
                    w.append("if(%s){", ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type"));
                }
                inCondition = true;
                ch.accept(this, arg);
                inCondition = false;
                w.append("}");
                i++;
            }
            w.append("else throw new RuntimeException(\"unexpected token: \" + ts.la);");
            return null;
        }
    }

}
