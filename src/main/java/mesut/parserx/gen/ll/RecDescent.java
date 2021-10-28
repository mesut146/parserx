package mesut.parserx.gen.ll;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.Recursion;
import mesut.parserx.gen.transform.Simplify;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

// ll(1) recursive descent parser generator
public class RecDescent {
    public static boolean debug = false;
    public static int loopLimit = 10;
    public Options options;
    Tree tree;
    CodeWriter code = new CodeWriter(true);
    RuleDecl curRule;
    String tokens = "Tokens";
    int flagCount;
    int firstCount;

    public RecDescent(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public static boolean isSimple(Node node) {
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return regex.node.isName();
        }
        return node.isName();
    }

    public void gen() throws IOException {
        prepare();
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
            if (!decl.hidden) {
                curRule = decl;
                gen(decl);
                code.append("");
            }
        }
        code.append("}");

        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(code.get(), file);
        genTokenType();
    }

    void writeConsume() {
        code.append("%s consume(int type){", options.tokenClass);
        code.append("if(la.type != type){");
        code.append("throw new RuntimeException(\"unexpected token: \" + la + \" expecting: \" + type);");
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

    private void prepare() throws IOException {
        Simplify.all(tree);
        tree = EbnfToBnf.combineOr(tree);
        AstGen astGen = new AstGen(tree);
        astGen.genAst();
        //if (debug) tree.printRules();

        Recursion recursion = new Recursion(tree);
        recursion.all();
        if (debug && recursion.any) {
            tree.printRules();
        }

        Factor.allowRecursion = true;
        Factor.factorSequence = true;
        Factor factor = recursion.factor;
        factor.factorize();
        if (debug && factor.any) {
            tree.printRules();
        }

        File out = new File(options.outDir, Utils.newName(tree.file.getName(), "-final.g"));
        //Name.autoEncode = true;
        Node.printVarName = false;
        Utils.write(tree.toString(), out);

        new LexerGenerator(tree).generate();
    }

    void gen(RuleDecl decl) {
        Type type = new Type(options.astClass, decl.retType.name);//todo decl.type
        StringBuilder params = new StringBuilder();
        int i = 0;
        for (Node arg : decl.ref.args) {
            if (i > 0) params.append(", ");
            if (arg.isName()) {
                Name name = arg.asName();
                if (name.isToken) {
                    params.append("Token ").append(arg.astInfo.factorName);
                }
                else {
                    params.append(String.format("%s %s", tree.getRule(arg.asName()).retType, arg.astInfo.factorName));
                }
            }
            else {
                Regex regex = arg.asRegex();
                Name name = regex.node.asName();
                if (name.isToken) {
                    params.append(String.format("List<Token> %s", regex.astInfo.factorName));
                }
                else {
                    params.append(String.format("List<%s.%s> %s", options.astClass, name.name, regex.astInfo.factorName));
                }
            }
            i++;
        }
        code.append("public %s %s(%s){", type, decl.baseName(), params);
        code.append("%s res = new %s();", type, type);
        flagCount = 0;
        firstCount = 0;

        if (decl.isRecursive) {
            write(decl.rhs);
        }
        else {
            write(decl.rhs);
        }

        code.append("return res;");
        code.append("}");
    }

    void write(Node node) {
        if (node.astInfo.which != -1) {
            code.all(node.astInfo.writeWhich());
        }
        if (node.astInfo.createNode) {
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
        else if (!node.isEpsilon()) {
            throw new RuntimeException("unexpected " + node);
        }
    }

    private void writeRegex(Regex regex) {
        if (regex.astInfo.isFactored) {
            Name name = regex.node.asName();
            code.append("%s.%s.addAll(%s);", name.astInfo.outerVar, name.astInfo.varName, regex.astInfo.factorName);
            return;
        }
        Set<Name> set = Helper.first(regex, tree, true, false, true);
        regex.node.astInfo.isInLoop = regex.isStar() || regex.isPlus();
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
        }
        else if (regex.isStar()) {
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    Name name = regex.node.asName();
                    String type = name.isToken ? options.tokenClass : options.astClass + "." + name.name;
                    code.append("List<%s> %s = new ArrayList<>();", type, regex.astInfo.factorName);
                    if (regex.astInfo.loopExtra != null) {
                        code.append("%s.add(%s);", regex.astInfo.factorName, regex.astInfo.loopExtra);
                    }
                    code.append("while(%s){", loopExpr(set));
                    String consumer = name.isToken ? "consume(" + tokens + "." + name.name + ")" : name + "()";
                    code.append("%s.add(%s);", regex.astInfo.factorName, consumer);
                    code.append("}");
                }
                else {
                    code.append("while(%s){", loopExpr(set));
                    write(regex.node);
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
                write(regex.node);
                endSwitch(def);
                code.append("}");
            }
        }
        else {
            //plus
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    Name name = regex.node.asName();
                    String type = name.isToken ? options.tokenClass : options.astClass + "." + name.name;
                    code.append("List<%s> %s = new ArrayList<>();", type, regex.astInfo.factorName);
                    if (regex.astInfo.loopExtra != null) {
                        code.append("%s.add(%s);", regex.astInfo.factorName, regex.astInfo.loopExtra);
                    }
                    code.append("do{");
                    String consumer = name.isToken ? "consume(" + tokens + "." + name.name + ")" : name + "()";
                    code.append("%s.add(%s);", regex.astInfo.factorName, consumer);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
                else {
                    code.append("do{");
                    write(regex.node);
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
                write(regex.node);
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
            sb.append(peekExpr()).append(" == ").append(tokens).append(".").append(tok.name);
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
                args.append(name.args.get(i).astInfo.factorName);
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
            return;
        }
        if (name.astInfo.isFactored) {
            //no consume
            if (name.astInfo.isInLoop) {
                code.append("%s.%s.add(%s);", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factorName);
            }
            else {
                code.append("%s.%s = %s;", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factorName);
            }
        }
        else if (curRule.isRecursive) {
            if (name.astInfo.isPrimary) {
                code.append("%s = %s;", name.astInfo.outerVar, withArgs(name));
            }
            else {
                code.append("%s = %s(%s);", name.astInfo.outerVar, name.name, name.astInfo.outerVar);
            }
        }
        else {
            String rhs;
            if (name.isRule()) {
                rhs = withArgs(name);
            }
            else {
                rhs = "consume(" + tokens + "." + name.name + ")";
            }
            if (name.astInfo.isFactor) {
                String type = name.isToken ? "Token" : (options.astClass + "." + name.name);
                code.append("%s %s = %s;", type, name.astInfo.factorName, rhs);
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
            final Node ch = or.get(i);
            Set<Name> set = Helper.first(ch, tree, true, false, true);
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
            for (Name nm : Helper.first(or, tree, true, false, true)) {
                if (!first) {
                    arr.append(",");
                }
                arr.append(nm);
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
