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

public class JavaAst extends BaseVisitor<Void, JavaAst.Info> {
    public Tree tree;
    CodeWriter w = new CodeWriter(true);
    Options options;
    //class name -> map of node -> count
    CountingMap2<String, String> varCount = new CountingMap2<>();
    int groupCount;
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
        for (var decl : tree.rules) {
            groupCount = 1;
            curRule = decl.baseName();
            model(decl);
        }
        w.append("}");

        Utils.write(w.get(), new File(options.outDir, options.astClass + ".java"));
        varCount.clear();
    }

    void model(RuleDecl decl) {
        w.append("public static class %s{", decl.baseName() + options.nodeSuffix);
        decl.rhs.accept(this, new Info(decl.retType, "res"));

        //todo create parent
        p.writePrinter(decl.rhs, false);
        if (decl.rhs.isOr()) {
            orInit(decl.rhs.asOr());
        }
        w.append("}");
    }

    void setVarName(Name name, Type outerCls) {
        String varName = name.astInfo.varName;
        if (varName == null) {
            varName = vName(name, outerCls.toString());
            name.astInfo.varName = varName;
        }
    }

    //make incremental variable name with class scoped
    public String vName(Name name, String cls) {
        int i = varCount.get(cls, name.name);
        return i == 1 ? name.name : name.name + i;
    }

    @Override
    public Void visitName(Name name, Info arg) {
        name.astInfo.outerVar = arg.outerVar;
        //check if user supplied var name
        setVarName(name, arg.outerCls);
        w.append("public %s %s;", name.isToken ? options.tokenClass : name.name, name.astInfo.varName);
        return null;
    }

    @Override
    public Void visitRegex(Regex regex, Info arg) {
        regex.astInfo.outerVar = arg.outerVar;
        Node ch = regex.node;
        if (regex.isOptional()) {
            regex.node.accept(this, arg);
        }
        else {
            Name name = ch.asName();
            setVarName(name, arg.outerCls);
            ch.astInfo.isInLoop = true;
            ch.astInfo.outerVar = arg.outerVar;
            String type = name.isToken ? options.tokenClass : name.name;
            w.append("public List<%s> %s = new ArrayList<>();", type, name.astInfo.varName);
        }
        regex.astInfo.varName = ch.astInfo.varName;
        return null;
    }


    @Override
    public Void visitOr(Or or, Info arg) {
        w.append("public int which;");
        int id = 1;
        for (var ch : or) {
            if (ch.isEpsilon()) continue;
            //in case of factorization pre-write some code
            ch.astInfo.which = id;
            //sequence
            //complex choice point inits holder
            var altType = new Type(arg.outerCls, Utils.camel(arg.outerCls.name) + id);
            String v;
            if (ch.isName() || ch.isSequence() && ch.asSequence().size() == 1) {
                if (ch.isSequence()) {
                    v = ItemSet.sym(ch.asSequence().get(0)).name;
                }
                else {
                    v = ItemSet.sym(ch).name;
                }
            }
            else {
                v = arg.outerCls.name.toLowerCase() + id;
            }
            ch.astInfo.nodeType = altType;
            ch.astInfo.varName = v;
            ch.astInfo.outerVar = arg.outerVar;
            ch.astInfo.assignOuter = true;
            w.append("%s %s;", altType.name, v);

            id++;
        }
        return null;
    }

    void orInit(Or or) {
        for (var ch : or) {
            if (ch.isEpsilon()) continue;
            //sequence
            //complex choice point inits holder
            var clsName = ch.astInfo.nodeType;

            w.append("public static class %s{", clsName.name);
            //holder ref
            w.append("%s holder;", curRule);
            ch.accept(this, new Info(clsName, ch.astInfo.varName));
            p.writePrinter(ch, true);
            w.append("}");
        }
    }

    static class Info {
        Type outerCls;
        String outerVar;

        public Info(Type outerCls, String outerVar) {
            this.outerCls = outerCls;
            this.outerVar = outerVar;
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
                    res = String.format("sb.append(\"'\").append(%s.value.replace(\"'\",\"\\'\")).append(\"'\");", expr);
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
