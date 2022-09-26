package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.*;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.ast.AstGen;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class CcGenJava {
    Tree tree;
    Options options;
    CodeWriter w = new CodeWriter(true);
    LLDfaBuilder builder;

    public CcGenJava(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    void prep() {
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

    public void gen() throws IOException {
        builder = new LLDfaBuilder(tree);
        builder.factor();
        AstGen.gen(tree, Lang.JAVA);
        LexerGenerator.gen(tree, Lang.JAVA);
        ParserUtils.genTokenType(tree);
        writeTS(options);
        prep();
        for (var entry : builder.rules.entrySet()) {
            var rule = entry.getKey();

            var decl = tree.getRule(rule);
            w.append("public %s %s() throws IOException{", decl.retType, rule);
            w.append("%s res = new %s();", decl.retType, decl.retType);

            var rhs = decl.rhs.asOr();
            w.append("ts.mark();");
            w.append("switch(%s_decide()){", rule);
            for (int i = 1; i <= rhs.size(); i++) {
                var ch = rhs.get(i - 1);
                w.append("case %d:{", i);
                w.append("ts.unmark();");
                alt(ch, i);
                var n = new NormalWriter();
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
            if (builder.rules.containsKey(decl.getName())) continue;
            w.append("public %s %s() throws IOException{", decl.retType, decl.getName());
            w.append("%s res = new %s();", decl.retType, decl.retType);
            var nw = new NormalWriter();
            decl.rhs.accept(nw, null);
            w.append("return res;");
            w.append("}");

        }
        w.append("}");
        var file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(w.get(), file);
    }


    void writeDecider(String rule) {
        var regexBuilder = new RegexBuilder(builder);
        var regex = regexBuilder.build(rule);
        w.append("public int %s_decide() throws IOException{", rule);
        var decider = new Decider();
        regex.accept(decider, null);
        w.append("}");
    }

    void alt(Node ch, int which) {
        w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
        w.append("%s.holder = res;", ch.astInfo.varName);
        w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
        w.append("res.which = %s;", which);
    }

    class NormalWriter extends BaseVisitor<Void, Void> {
        @Override
        public Void visitOr(Or or, Void arg) {
            int id = 1;
            for (var ch : or) {
                w.append("%sif(%s){", id > 1 ? "else " : "", ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type"));
                w.append("%s %s = new %s();", ch.astInfo.nodeType, ch.astInfo.varName, ch.astInfo.nodeType);
                w.append("%s.holder = res;", ch.astInfo.varName);
                w.append("res.%s = %s;", ch.astInfo.varName, ch.astInfo.varName);
                w.append("res.which = %s;", id++);
                ch.accept(this, arg);
                w.append("}");
            }
            w.append("else throw new RuntimeException(\"expecting one of %s got: \"+ts.la);", FirstSet.tokens(or, tree));
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
            var la = ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type");
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

    private void consumer(Name name, String vname) {
        String rhs;
        if (name.isToken) {
            rhs = String.format("ts.consume(%s.%s, \"%s\")", ParserUtils.tokens, name.name, name.name);
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

    class Decider extends BaseVisitor<Void, Void> {
        @Override
        public Void visitName(Name name, Void arg) {
            if (name.isToken) {
                if (!name.astInfo.isFactor) {
                    w.append("return %s;", name.astInfo.which);
                }
                else {
                    w.append("ts.consume(Tokens.%s, \"%s\");", name.name, name.name);
                }
            }
            else {
                w.append("return %s_decide();", name.name);
            }
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            var ch = regex.node;
            var la = ParserUtils.loopExpr(FirstSet.tokens(ch, tree), "ts.la.type");
            if (regex.isOptional()) {
                w.append("if(%s){", la);
                ch.accept(this, arg);
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
                ch.accept(this, arg);
                w.append("}");
                i++;
            }
            w.append("else throw new RuntimeException(\"unexpected token: \" + ts.la);");
            return null;
        }
    }

}
