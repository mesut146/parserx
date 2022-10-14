package mesut.parserx.gen.ast;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.lldfa.Type;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class JavaAst extends BaseVisitor<Void, Void> {
    public Tree tree;
    CodeWriter w = new CodeWriter(true);
    Options options;
    String curRule;
    Printer p;
    public static boolean printTokenQuote = true;

    public JavaAst(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
        p = new Printer();
    }

    public void genAst() throws IOException {
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("");

        w.append("public class %s{", options.astClass);
        printTokenHelper();

        for (var decl : tree.rules) {
            model(decl);
        }
        w.append("}");

        Utils.write(w.get(), new File(options.outDir, options.astClass + ".java"));
    }

    void printTokenHelper() {
        if (!printTokenQuote) return;
        w.append("");
        w.append("static void printToken(%s token, StringBuilder sb){", options.tokenClass);
        w.append("sb.append(\"'\").append(token.value.replace(\"'\",\"\\\\'\")).append(\"'\");");
        w.append("}");
        w.append("");
    }

    void model(RuleDecl decl) {
        curRule = decl.baseName();
        w.append("public static class %s{", decl.retType.name);
        decl.rhs.accept(this, null);

        //todo create parent
        p.writePrinter(decl.rhs, false);
        if (decl.rhs.isOr()) {
            orInit(decl.rhs.asOr());
        }
        w.append("}");
    }

    @Override
    public Void visitName(Name name, Void arg) {
        w.append("public %s %s;", name.astInfo.nodeType, name.astInfo.varName);
        return null;
    }

    @Override
    public Void visitRegex(Regex regex, Void arg) {
        if (regex.isOptional()) {
            regex.node.accept(this, arg);
        }
        else {
            Node ch = regex.node;
            w.append("public List<%s> %s = new ArrayList<>();", ch.astInfo.nodeType, ch.astInfo.varName);
        }
        return null;
    }

    @Override
    public Void visitOr(Or or, Void arg) {
        w.append("public int which;");
        for (var ch : or) {
            if (ch.isEpsilon()) continue;
            w.append("%s %s;", ch.astInfo.nodeType.name, ch.astInfo.varName);
        }
        return null;
    }

    void orInit(Or or) {
        for (var ch : or) {
            if (ch.isEpsilon()) continue;
            w.append("public static class %s{", ch.astInfo.nodeType.name);
            //holder ref
            w.append("%s holder;", curRule);
            ch.accept(this, null);
            p.writePrinter(ch, true);
            w.append("}");
        }
    }

    class Printer extends BaseVisitor<Void, Void> {
        int cur = 0;
        boolean nonEmpty = false;

        void writePrinter(Node rhs, boolean isAlt) {
            w.append("public String toString(){");
            if (isAlt) {
                w.append("StringBuilder sb = new StringBuilder();");
                rhs.accept(this, null);
                w.append("return sb.toString();");
            }
            else {
                if (rhs.isOr()) {
                    w.append("StringBuilder sb = new StringBuilder(\"%s#\" + which + \"{\");", JavaAst.this.curRule);
                }
                else {
                    w.append("StringBuilder sb = new StringBuilder(\"%s{\");", JavaAst.this.curRule);
                }
                rhs.accept(this, null);
                w.append("return sb.append(\"}\").toString();");
            }
            w.append("}");
        }

        void printer(String expr, boolean isToken) {
            String res;
            if (isToken) {
                if (printTokenQuote) {
                    res = String.format("printToken(%s, sb);", expr);
                }
                else {
                    res = String.format("sb.append(%s.value);", expr);
                }
            }
            else {
                res = String.format("sb.append(%s.toString());", expr);
            }
            w.append("%s", res);
        }

        @Override
        public Void visitName(Name name, Void arg) {
            if (cur > 0) {
                w.append("if(!first){");
                w.append("sb.append(\"%s\");", options.sequenceDelimiter);
                w.append("}");
            }
            printer(name.astInfo.varName, name.isToken);
            return null;
        }

        @Override
        public Void visitSequence(Sequence s, Void arg) {
            nonEmpty = false;
            int backup = cur;
            cur = 0;
            w.append("boolean first = true;");
            for (int i = 0; i < s.size(); i++) {
                Node ch = s.get(i);
                ch.accept(this, null);
                cur++;
                if (ch.isName() && i < s.size() - 1) {
                    if (!nonEmpty) {//is already set
                        w.append("first = false;");
                    }
                    nonEmpty = true;
                }
            }
            cur = backup;
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            var name = regex.node.asName();
            var v = name.astInfo.varName;
            if (regex.isOptional()) {
                w.append("if(%s != null){", v);
                name.accept(this, null);
                w.append("first = false;");
                w.append("}");
            }
            else {
                w.append("if(!%s.isEmpty()){", v);
                if (cur > 0) {
                    w.append("if(!first){");
                    w.append("sb.append(\"%s\");", options.sequenceDelimiter);
                    w.append("}");
                }
                w.append("sb.append('[');");
                w.append("for(int i = 0;i < %s.size();i++){", v);
                printer(String.format("%s.get(i)", v), name.isToken);
                w.append("if(i < %s.size() - 1) sb.append(\"%s\");", v, options.arrayDelimiter);
                w.append("}");
                w.append("sb.append(']');");
                w.append("first = false;");
                w.append("}");
            }
            return null;
        }

        @Override
        public Void visitOr(Or or, Void arg) {
            for (int i = 0; i < or.size(); i++) {
                if (i == 0) {
                    w.append("if(which == 1){");
                }
                else {
                    w.append("else if(which == %d){", i + 1);
                }
                Node ch = or.get(i);
                if (ch.isName()) {
                    ch.accept(this, null);
                }
                else {
                    w.append("sb.append(%s);", ch.astInfo.varName);
                }
                w.append("}");//if
            }
            return null;
        }
    }
}
