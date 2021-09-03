package mesut.parserx.gen.ll;

import mesut.parserx.gen.*;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

// la(1) recursive descent parser generator
public class LLRec {
    public Options options;
    Tree tree;
    StringBuilder sb = new StringBuilder();
    CodeWriter code = new CodeWriter(true);
    String curRule;
    int groupCount;
    String tokens = "Tokens";
    AstGen astGen;

    public LLRec(Tree tree, Options options) {
        this.tree = tree;
        this.options = options;
    }

    public static void indent(String data, StringBuilder sb) {
        for (String line : data.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
    }

    public static boolean isSimple(Node node) {
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return regex.node.isName();
        }
        return node.isName();
    }

    void check() {
        new SimpleTransformer(tree) {
            @Override
            public Node transformOr(Or or, Node parent) {
                for (int i = 0; i < or.size(); i++) {
                    Set<Name> s1 = Helper.first(or.get(i), tree, true);
                    for (int j = i + 1; j < or.size(); j++) {
                        Set<Name> s2 = Helper.first(or.get(j), tree, true);
                        Name sym = Factor.conf(s1, s2);
                        if (sym != null) {
                            throw new RuntimeException("factorization needed for " + curRule.name);
                        }
                    }
                }
                return or;
            }

            @Override
            public Node transformSequence(Sequence s, Node parent) {
                Node A = s.first();
                if (Helper.canBeEmpty(A, tree)) {
                    Node B = Helper.trim(s);
                    Set<Name> s1 = Helper.first(A, tree, true);
                    Set<Name> s2 = Helper.first(B, tree, true);
                    Name sym = Factor.conf(s1, s2);
                    if (sym != null) {
                        throw new RuntimeException("factorization needed for " + curRule.name);
                    }
                }
                return s;
            }
        }.transformAll();
    }

    void indent(String data) {
        indent(data, sb);
    }

    public void gen() throws IOException {
        tree = EbnfToBnf.combineOr(tree);
        sb.append("import java.util.List;\n");
        sb.append("import java.util.ArrayList;\n");
        if (options.packageName != null) {
            sb.append(String.format("import %s.%s;\n", options.packageName, options.astClass));
        }
        sb.append("\n");
        sb.append("public class ").append(options.parserClass).append("{\n");
        sb.append(String.format("  List<%s> list = new ArrayList<>();\n", options.tokenClass));
        sb.append(String.format(" %s lexer;\n", options.lexerClass));

        sb.append(String.format("  public %s(%s lexer) throws java.io.IOException{\n    this.lexer = lexer;\n    fill();\n  }\n", options.parserClass, options.lexerClass));

        writeConsume();
        writePop();
        writePeek();
        writeFill();

        astGen = new AstGen(tree, options);
        astGen.genAst();
        astGen.varCount.clear();

        for (RuleDecl decl : tree.rules) {
            if (!decl.hidden) {
                curRule = decl.name;
                gen(decl);
            }
        }
        indent(code.get(), sb);
        sb.append("}");

        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(sb.toString(), file);
        System.out.println("parser file generated to " + file);
        genTokenType();
    }

    void gen(RuleDecl decl) {
        groupCount = 1;
        String type = options.astClass + "." + decl.name;
        code.append(String.format("public %s %s(%s){", type, decl.name, NodeList.join(decl.args, ", ")));
        code.append(String.format("%s res = new %s();", type, type));
        write(decl.rhs, "res", type, 0, 0, false);
        code.append("return res;");
        code.append("}");
    }

    void writeFill() {
        String s = "void fill() throws java.io.IOException{\n" +
                "  while(true){\n" +
                String.format("    %s t = lexer.%s();\n", options.tokenClass, options.lexerFunction) +
                "    list.add(t);\n" +
                "    if(t == null || t.type == 0) return;\n" +
                "  }\n" +
                "}";
        indent(s);
    }

    void writeConsume() {
        String s = options.tokenClass + " consume(int type){\n" +
                "  " + options.tokenClass + " t = pop();\n" +
                "  if(t.type != type)\n" +
                "    throw new RuntimeException(\"unexpected token: \" + t + \" expecting: \" + type);\n" +
                "  return t;\n" +
                "\n}";
        indent(s);
    }

    void writePop() {
        String s = "Token pop(){\n" +
                "  return list.remove(0);\n" +
                "}";
        indent(s);
    }

    void writePeek() {
        String s = "Token peek(){\n" +
                "  return list.get(0);\n" +
                "}";
        indent(s);
    }

    void genTokenType() throws IOException {
        CodeWriter c = new CodeWriter(true);
        if (options.packageName != null) {
            c.append("package " + options.packageName + ";");
            c.append("");
        }
        c.append("public class " + tokens + "{");

        int id = 1;
        for (TokenDecl decl : tree.tokens) {
            if (decl.isSkip) continue;
            c.append(String.format("public static final int %s = %d;", decl.name, id));
            id++;
        }

        c.append("}");
        File file = new File(options.outDir, tokens + ".java");
        Utils.write(c.get(), file);
        System.out.println("write " + file);
    }

    String peekExpr() {
        //todo optimize to field access
        return "peek().type";
    }


    boolean arr(Node node) {
        return node.isStar() || node.isPlus();
    }

    void write(Node node, String outerVar, String outerCls, int flagCount, int firstCount, boolean isArr) {
        if (node.isOr()) {
            Or or = node.asOr();
            code.append(String.format("switch(%s){", peekExpr()));
            for (int i = 0; i < or.size(); i++) {
                Node ch = or.get(i);
                Set<Name> set = Helper.first(ch, tree, true, false, true);
                for (Name la : set) {
                    code.append(String.format("case %s.%s:", tokens, la.name));
                }
                code.append("{");
                code.append(String.format("%s.which = %s;", outerVar, i + 1));
                if (isSimple(ch)) {
                    write(ch, outerVar, outerCls, flagCount, firstCount, false);
                }
                else {
                    String varl = outerCls.toLowerCase() + (i + 1);
                    String cls = Utils.camel(outerCls) + (i + 1);
                    String type = options.astClass + "." + curRule + "." + cls;
                    code.append(String.format("%s %s = new %s();\n", type, varl, type));
                    code.append(String.format("%s.%s = %s;\n", outerVar, varl, varl));
                    write(ch, varl, cls, flagCount, firstCount, arr(ch));
                }
                code.append("break;");
                code.append("}");
            }
            if (!Helper.canBeEmpty(node, tree)) {
                code.append("default:{");
                StringBuilder arr = new StringBuilder();
                boolean first = true;
                for (Name nm : Helper.first(or, tree, true)) {
                    if (!first) {
                        arr.append(",");
                    }
                    arr.append(nm);
                    first = false;
                }
                code.append(String.format("throw new RuntimeException(\"expecting one of [%s] got: \"+peek());", arr));
                code.append("}");
            }
            else {

            }
            code.append("}");
        }
        else if (node.isGroup()) {
            String var = "g" + groupCount++;
            String cls = curRule + var;
            String type = options.astClass + "." + curRule + "." + cls;
            code.append(String.format("%s %s = new %s();", type, var, type));
            if (isArr) {
                code.append(String.format("%s.%s.add(%s);", outerVar, var, var));
            }
            else {
                code.append(String.format("%s.%s = %s;", outerVar, var, var));
            }
            write(node.asGroup().node, var, cls, flagCount, firstCount, false);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                write(s.get(i), outerVar, outerCls, flagCount, firstCount, false);
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.factored) {
                //throw new RuntimeException("factored ref");
            }
            String rhs;
            if (name.isRule()) {
                if (!name.args.isEmpty()) {
                    //throw new RuntimeException("ref with args");
                }
                rhs = name.name + "(" + ")";
            }
            else {
                rhs = "consume(" + tokens + "." + name.name + ")";
            }
            if (name.astInfo.isFactor) {
                String type = name.isToken ? "Token" : name.name;
                code.append(String.format("%s %s = %s;", type, node.astInfo.varName, rhs));
            }
            else {
                if (isArr) {
                    code.append(String.format("%s.%s.add(%s);", node.astInfo.outerVar, node.astInfo.varName, rhs));
                }
                else {
                    code.append(String.format("%s.%s = %s;", node.astInfo.outerVar, node.astInfo.varName, rhs));
                }
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            Set<Name> set = Helper.first(node, tree, true, false, true);
            if (regex.isOptional()) {
                if (set.size() == 1) {
                    code.append(String.format("if(%s == %s.%s){", peekExpr(), tokens, set.iterator().next()));
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, true);
                    code.append("}");
                }
                else {
                    beginSwitch(set);
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, false);
                    endSwitch("");
                }
            }
            else if (regex.isStar()) {
                if (set.size() == 1) {
                    code.append(String.format("while(%s == %s.%s){", peekExpr(), tokens, set.iterator().next()));
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, true);
                    code.append("}");
                }
                else {
                    flagCount++;
                    String flagStr = "flag";
                    if (flagCount > 1) flagStr += flagCount;
                    code.append(String.format("boolean %s = true;", flagStr));
                    code.append(String.format("while(%s){", flagStr));
                    String def = flagStr + " = false;\n";
                    beginSwitch(set);
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, true);
                    endSwitch(def);
                    code.append("}");
                }
            }
            else {
                //plus
                if (set.size() == 1) {
                    code.append("do{");
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, true);
                    code.down();
                    code.append(String.format("}while(%s == %s.%s);", peekExpr(), tokens, set.iterator().next()));
                }
                else {
                    flagCount++;
                    firstCount++;
                    String flagStr = "flag";
                    String firstStr = "first";
                    if (flagCount > 1) flagStr += flagCount;
                    if (firstCount > 1) firstStr += firstCount;
                    code.append(String.format("boolean %s = true;", flagStr));
                    code.append(String.format("boolean %s = true;", firstStr));
                    code.append(String.format("while(%s){", flagStr));
                    String def = String.format("if(!%s)  %s = false;\n" + "else  throw new RuntimeException(\"unexpected token: \"+peek());", firstStr, flagStr);
                    beginSwitch(set);
                    write(regex.node, outerVar, outerCls, flagCount, firstCount, true);
                    endSwitch(def);
                    code.append("first = false;\n");
                    code.append("}");
                }
            }
        }
        else if (!node.isEpsilon()) {
            throw new RuntimeException("unexpected " + node);
        }
    }


    void beginSwitch(Set<Name> set) {
        code.append("switch(" + peekExpr() + "){");
        for (Name token : set) {
            code.append(String.format("case %s.%s:", tokens, token));
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
