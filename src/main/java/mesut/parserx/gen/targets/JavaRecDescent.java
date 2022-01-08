package mesut.parserx.gen.targets;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static mesut.parserx.gen.ll.RecDescent.loopLimit;
import static mesut.parserx.gen.ll.RecDescent.tokens;

public class JavaRecDescent {

    public static boolean debug = false;
    public Options options;
    Tree tree;
    CodeWriter code = new CodeWriter(true);
    RuleDecl curRule;
    int flagCount;
    int firstCount;

    public JavaRecDescent(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        if (options.packageName != null) {
            code.append("package %s;", options.packageName);
            code.append("");
        }
        code.append("import java.util.List;");
        code.append("import java.util.ArrayList;");
        code.append("import java.io.IOException;");
        if (options.packageName != null) {
            code.append("import %s.%s;", options.packageName, options.astClass);
        }
        code.append("");
        code.append("public class %s{", options.parserClass);
        code.append("%s lexer;", options.lexerClass);
        code.append("%s la;", options.tokenClass);
        code.append("");

        code.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);

        code.all("this.lexer = lexer;\nla = lexer.next();\n}");
        code.append("");

        writeConsume();

        for (RuleDecl decl : tree.rules) {
            curRule = decl;
            gen(decl);
            code.append("");
        }
        code.append("}");

        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(code.get(), file);
        genTokenType();
    }

    void writeConsume() {
        code.append("%s consume(int type, String name){", options.tokenClass);
        code.append("if(la.type != type){");
        code.append("throw new RuntimeException(\"unexpected token: \" + la + \" expecting: \" + name);");
        code.all("}");
        code.append("try{");
        code.append("%s res = la;", options.tokenClass);
        code.append("la = lexer.next();");
        code.append("return res;");
        code.all("}\ncatch(IOException e){");
        code.append("throw new RuntimeException(e);");
        code.append("}");
        code.append("}");
    }

    void genTokenType() throws IOException {
        CodeWriter c = new CodeWriter(true);
        if (options.packageName != null) {
            c.append("package %s;", options.packageName);
            c.append("");
        }
        c.append("public class %s{", tokens);
        c.append("public static final int EOF = 0;");
        int id = 1;
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            //if (decl.isSkip) continue;
            c.append("public static final int %s = %d;", decl.name, id);
            id++;
        }
        c.append("}");
        File file = new File(options.outDir, tokens + ".java");
        Utils.write(c.get(), file);
    }

    String peekExpr() {
        return "la.type";
    }

    void gen(RuleDecl decl) {
        StringBuilder params = new StringBuilder();
        int i = 0;
        for (Node arg : decl.ref.args) {
            if (i > 0) params.append(", ");
            if (arg.isName()) {
                Name name = arg.asName();
                params.append(String.format("%s %s", getType(name), name.astInfo.varName));
            }
            else {
                Regex regex = arg.asRegex();
                Name name = regex.node.asName();
                params.append(String.format("List<%s> %s", getType(name), regex.astInfo.varName));
            }
            i++;
        }
        Type type = decl.retType;
        code.append("public %s %s(%s){", type, decl.baseName(), params);
        code.append("%s res = new %s();", type, type);
        flagCount = 0;
        firstCount = 0;

        write(decl.rhs);

        code.append("return res;");
        code.append("}");
    }

    void write(Node node) {
        if (node.astInfo.which != -1) {
            code.all(node.astInfo.writeWhich());
        }
        if (node.astInfo.nodeType != null) {
            code.all(node.astInfo.writeNode());
        }
        if (node.astInfo.substitution) {
            code.append("%s.%s = %s;", node.astInfo.outerVar, node.astInfo.subVar, node.astInfo.varName);
        }
        if (node.isOr()) {
            Or or = node.asOr();
            writeOr(or);
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            write(group.node);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                write(s.get(i));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            writeName(name);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            writeRegex(regex);
        }
        else if (node.isEpsilon()) {
            if (node.astInfo.isFactored) {
                code.append("%s = %s;", node.astInfo.varName, node.astInfo.factor.varName);
            }
        }
    }

    Type getType(Name name) {
        if (name.isToken) return new Type(options.tokenClass);
        return tree.getRule(name).retType;
    }

    String tokenConsumer(Name token) {
        return String.format("consume(%s.%s, \"%s\")", tokens, token.name, token.name);
    }

    private void writeRegex(Regex regex) {
        Set<Name> set = FirstSet.tokens(regex, tree);
        if (regex.isOptional()) {
            if (set.size() <= loopLimit) {
                code.append("if(%s){", loopExpr(set));
                write(regex.node);
                code.append("}");
            }
            else {
                beginSwitch(set);
                write(regex.node);
                endSwitch("");
            }
            return;
        }
        Name name = regex.node.asName();
        if (regex.astInfo.isFactored) {
            code.append("%s.%s.addAll(%s);", name.astInfo.outerVar, name.astInfo.varName, regex.astInfo.factor.varName);
            /*if (regex.astInfo.factor == null) {
                code.append("%s.%s.addAll(%s);", name.astInfo.outerVar, name.astInfo.varName, regex.astInfo.varName);
            }
            else {
                if (name.isToken) {
                    code.append("%s.%s.addAll(%s);", regex.astInfo.outerVar, name.astInfo.varName, regex.astInfo.factor.varName);
                }
                else {
                    code.append("for(int i = 0;i < %s.size();i++){", regex.astInfo.factor.varName);
                    code.append("%s.%s.add(%s(%s.get(i)));", regex.astInfo.outerVar, name.astInfo.varName, name.name, regex.astInfo.factor.varName);
                    code.append("}");
                }
            }*/
            return;
        }
        else if (set.isEmpty()) {
            //factored loop
            code.append("for(int i = 0;i < %s.size();i++){", regex.astInfo.factor.varName);
            code.append("%s.%s.add(%s(%s.get(i)));", regex.astInfo.outerVar, name.astInfo.varName, name.name, regex.astInfo.factor.varName);
            code.append("}");
            return;
        }

        //regex.node.astInfo.isInLoop = regex.isStar() || regex.isPlus();
        if (regex.isStar()) {
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    throw new RuntimeException("star loop");
                    /*code.append("List<%s> %s = new ArrayList<>();", getType(name), regex.astInfo.varName);
                    code.append("while(%s){", loopExpr(set));
                    String consumer = name.isToken ? tokenConsumer(name) : name + "()";
                    code.append("%s.add(%s);", regex.astInfo.varName, consumer);
                    code.append("}");*/
                }
                else {
                    code.append("while(%s){", loopExpr(set));
                    write(name);
                    code.append("}");
                }
            }
            else {
                flagCount++;
                String flagStr = "flag";
                if (flagCount > 1) flagStr += flagCount;
                code.append(String.format("boolean %s = true;", flagStr));
                code.append(String.format("while(%s){", flagStr));
                String def = flagStr + " = false;\n";
                beginSwitch(set);
                write(name);
                endSwitch(def);
                code.append("}");
            }
        }
        else {
            //plus
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    code.append("List<%s> %s = new ArrayList<>();", getType(name), regex.astInfo.varName);
                    code.append("do{");
                    String consumer = name.isToken ? tokenConsumer(name) : name + "()";
                    code.append("%s.add(%s);", regex.astInfo.varName, consumer);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
                else {
                    code.append("do{");
                    write(name);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
            }
            else {
                flagCount++;
                firstCount++;
                String flagStr = "flag";
                String firstStr = "first";
                if (flagCount > 1) flagStr += flagCount;
                if (firstCount > 1) firstStr += firstCount;
                code.append("boolean %s = true;", flagStr);
                code.append("boolean %s = true;", firstStr);
                code.append("while(%s){", flagStr);
                String def = String.format("if(!%s)  %s = false;\n" + "else  throw new RuntimeException(\"unexpected token: \"+la);", firstStr, flagStr);
                beginSwitch(set);
                write(name);
                endSwitch(def);
                code.append("first = false;\n");
                code.append("}");
            }
        }
    }

    String loopExpr(Set<Name> set) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            Name tok = it.next();
            sb.append(String.format("%s == %s.%s", peekExpr(), tokens, tok.name));
            if (it.hasNext()) {
                sb.append(" || ");
            }
        }
        return sb.toString();
    }

    String withArgs(Name name) {
        StringBuilder args = new StringBuilder();
        if (!name.args.isEmpty()) {
            for (int i = 0; i < name.args.size(); i++) {
                args.append(name.args.get(i).astInfo.varName);
                if (i < name.args.size() - 1) {
                    args.append(",");
                }
            }
        }
        return name.name + "(" + args + ")";
    }

    private void writeName(Name name) {
        if (name.astInfo.isFactored && name.astInfo.isFactor) {
            //factor names may be different?
            if (!name.astInfo.varName.equals(name.astInfo.factor.varName) && !name.astInfo.isInLoop) {
                //redeclare so it's available to rest
                code.append("%s %s = %s;", getType(name), name.astInfo.varName, name.astInfo.factor.varName);
            }
            //!name.astInfo.isInLoop
            return;
        }
        if (name.astInfo.isFactored) {
            //no consume
            if (name.astInfo.isInLoop) {
                if (name.astInfo.isPrimary) throw new RuntimeException("todo");
                code.append("%s.%s.add(%s);", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
            }
            else {
                if (name.astInfo.isPrimary) {
                    code.append("%s = %s;", name.astInfo.varName, name.astInfo.factor.varName);
                }
                else {
                    code.append("%s.%s = %s;", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
                }
            }
        }
        else if (name.astInfo.isPrimary) {
            code.append("%s = %s;", name.astInfo.varName, withArgs(name));
        }
        else if (name.astInfo.isSecondary) {
            code.append("%s = %s;", name.astInfo.varName, withArgs(name));
            //code.append("%s = %s(%s);", name.astInfo.outerVar, name.name, name.astInfo.outerVar);
        }
        else {
            String rhs;
            if (name.isRule()) {
                rhs = withArgs(name);
            }
            else {
                rhs = tokenConsumer(name);
            }
            if (name.astInfo.isFactor) {
                code.append("%s %s = %s;", getType(name), name.astInfo.varName, rhs);
            }
            else {
                if (name.astInfo.isInLoop) {
                    code.append("%s.%s.add(%s);", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
                else {
                    code.append("%s.%s = %s;", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
            }
        }
    }

    private void writeOr(Or or) {
        code.append(String.format("switch(%s){", peekExpr()));
        Node empty = null;
        for (int i = 0; i < or.size(); i++) {
            Node ch = or.get(i);
            Set<Name> set = FirstSet.tokens(ch, tree);
            if (!set.isEmpty()) {
                for (Name la : set) {
                    code.append(String.format("case %s.%s:", tokens, la.name));
                }
                code.append("{");
                write(ch);
                code.append("break;");
                code.append("}");
                if (Helper.canBeEmpty(ch, tree)) {
                    empty = ch;
                }
            }
            else {
                empty = ch;
            }
        }
        if (empty != null) {
            code.append("default:{");
            write(empty);
            code.append("}");
        }
        else if (!Helper.canBeEmpty(or, tree)) {
            code.append("default:{");
            StringBuilder arr = new StringBuilder();
            boolean first = true;
            for (Name tok : FirstSet.tokens(or, tree)) {
                if (!first) {
                    arr.append(",");
                }
                arr.append(tok);
                first = false;
            }
            code.append("throw new RuntimeException(\"expecting one of [%s] got: \"+la);", arr);
            code.append("}");
        }
        code.append("}");
    }

    void beginSwitch(Set<Name> set) {
        code.append("switch(" + peekExpr() + "){");
        for (Name token : set) {
            code.append("case %s.%s:", tokens, token.name);
        }
        code.append("{");
    }

    void endSwitch(String def) {
        code.all("}\nbreak;");
        if (!def.isEmpty()) {
            code.append("default:{");
            code.all(def);
            code.append("}");
        }
        code.append("}");
    }
}
