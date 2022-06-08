package mesut.parserx.gen.targets;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;
import mesut.parserx.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JavaAst extends BaseVisitor<Void, JavaAst.Info> {
    public Tree tree;
    CodeWriter w = new CodeWriter(true);
    Options options;
    //class name -> map of node -> count
    CountingMap2<String, String> varCount = new CountingMap2<>();
    int groupCount;
    String curRule;
    Printer p;

    public JavaAst(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
        p = new Printer();
    }

    public void genAst() throws IOException {
        new Normalizer(tree).normalize();
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("");

        w.append("public class %s{", options.astClass);
        for (RuleDecl decl : tree.rules) {
            groupCount = 1;
            curRule = decl.baseName();
            model(decl);
        }
        w.append("}");

        Files.write(Paths.get(options.outDir, options.astClass + ".java"), w.get().getBytes());
        varCount.clear();
    }

    void model(RuleDecl decl) {
        w.append("public static class %s{", decl.baseName() + options.nodeSuffix);
        decl.rhs.accept(this, new Info(decl.retType, "res"));

        if (tree.isOriginal(decl.ref) && options.genVisitor) {
            w.append("public <R,P> R accept(%sVisitor<R, P> visitor, P arg){", options.parserClass);
            w.all("return visitor.visit%s(this, arg);\n}", Utils.camel(decl.baseName()));
        }
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
        for (Node ch : or) {
            if (ch.isEpsilon()) continue;
            //in case of factorization pre-write some code
            ch.astInfo.which = id;
            if (options.useSimple && RecDescent.isSimple(ch)) {
                ch.accept(this, arg);
            }
            else {
                //sequence
                //complex choice point inits holder
                Type clsName = new Type(arg.outerCls, Utils.camel(arg.outerCls.name) + id);
                String v = arg.outerCls.name.toLowerCase() + id;
                ch.astInfo.nodeType = clsName;
                ch.astInfo.varName = v;
                ch.astInfo.outerVar = arg.outerVar;
                ch.astInfo.assignOuter = true;
                w.append("%s %s;", clsName.name, v);
            }
            id++;
        }
        return null;
    }

    void orInit(Or or) {
        for (Node ch : or) {
            if (ch.isEpsilon()) continue;
            if (options.useSimple && RecDescent.isSimple(ch)) {
            }
            else {
                //sequence
                //complex choice point inits holder
                Type clsName = ch.astInfo.nodeType;

                w.append("public static class %s{", clsName.name);
                //holder ref
                w.append("%s holder;", curRule);
                ch.accept(this, new Info(clsName, ch.astInfo.varName));
                p.writePrinter(ch, true);
                w.append("}");
            }
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

        void writePrinter(Node rhs, boolean isAlt) {
            w.append("public String toString(){");
            if (isAlt) {
                w.append("StringBuilder sb = new StringBuilder();");
                rhs.accept(this, null);
                w.append("return sb.toString();");
            }
            else {
                if (rhs.isOr()) {
                    w.append("StringBuilder sb = new StringBuilder(\"%s#\" + which + \"{\");", curRule);
                }
                else {
                    w.append("StringBuilder sb = new StringBuilder(\"%s{\");", curRule);
                }
                rhs.accept(this, null);
                w.append("return sb.append(\"}\").toString();");
            }
            w.append("}");//toString
        }

        String printer(Name name) {
            if (name.isToken) {
                return String.format("\"'\" + %s.value + \"'\"", name.astInfo.varName);
            }
            else {
                return String.format("%s.toString()", name.astInfo.varName);
            }
        }

        @Override
        public Void visitName(Name name, Void arg) {
            w.append("sb.append(%s);", printer(name));
            return null;
        }

        @Override
        public Void visitSequence(Sequence s, Void arg) {
            for (int i = 0; i < s.size(); i++) {
                s.get(i).accept(this, null);
                if (i < s.size() - 1) {
                    Node next = s.get(i + 1);
                    if (next.isOptional()) {
                        w.append("if(%s != null) sb.append(\"%s\");", next.astInfo.varName, options.sequenceDelimiter);
                    }
                    else if (next.isStar()) {
                        w.append("if(!%s.isEmpty()) sb.append(\"%s\");", next.asRegex().node.astInfo.varName, options.sequenceDelimiter);
                    }
                    else {
                        w.append("sb.append(\"%s\");", options.sequenceDelimiter);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitRegex(Regex regex, Void arg) {
            String v = regex.node.astInfo.varName;
            Name name = regex.node.asName();
            if (regex.isStar() || regex.isPlus()) {
                w.append("if(!%s.isEmpty()){", v);
                w.append("sb.append('[');");
                w.append("for(int i = 0;i < %s.size();i++){", v);

                if (name.isToken) {
                    w.append("sb.append(\"'\" + %s.get(i).value + \"'\");", v);
                }
                else {
                    w.append("sb.append(%s.get(i).toString());", v);
                }
                w.append("if(i < %s.size() - 1) sb.append(\",\");", v);
                w.append("}");
                w.append("sb.append(']');");
                w.append("}");
            }
            else {
                w.append("sb.append(%s == null?\"\":%s);", v, printer(name));
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
