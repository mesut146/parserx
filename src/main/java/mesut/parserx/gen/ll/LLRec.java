package mesut.parserx.gen.ll;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// la(1) recursive descent parser generator
public class LLRec {
    public Options options;
    Tree tree;
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2;
    StringBuilder code = new StringBuilder();
    String curRule;
    Map<String, Map<String, Integer>> varCount = new HashMap<>();
    String nodeSuffix = "";
    int groupCount;
    String tokens = "Tokens";

    public LLRec(Tree tree, Options options) {
        this.tree = tree;
        this.options = options;
    }

    static void indent(String data, StringBuilder sb) {
        for (String line : data.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
    }


    void indent(String data) {
        indent(data, sb);
    }

    public void gen() throws IOException {
        tree = EbnfToBnf.combineOr(tree);
        sb.append("import java.util.List;\n");
        sb.append("import java.util.ArrayList;\n");
        sb.append("\n");
        sb.append("public class ").append(options.parserClass).append("{\n");
        sb.append(String.format("  List<%s> list = new ArrayList<>();\n", options.tokenClass));
        sb.append(String.format(" %s lexer;\n", options.lexerClass));

        sb.append(String.format("  public %s(%s lexer) throws java.io.IOException{\n    this.lexer = lexer;\n  fill();\n  }\n", options.parserClass, options.lexerClass));

        writeConsume();
        writePop();
        writePeek();
        writeFill();

        for (RuleDecl decl : tree.rules) {
            if (!decl.hidden) {
                curRule = decl.name;
                varCount.clear();
                gen(decl);
            }
        }
        indent(code.toString(), sb);
        sb.append("}");

        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(sb.toString(), file);
        System.out.println("parser file generated to " + file);
        genTokenType();
    }

    void gen(RuleDecl decl) {
        groupCount = 1;
        model(decl);
        groupCount = 1;

        String type = decl.name + nodeSuffix;
        code.append(String.format("public %s %s(%s){\n", type, decl.name, NodeList.join(decl.args, ", ")));
        code.append(String.format("  %s res = new %s();\n", type, type));
        indent(write(decl.rhs, "res", type, 0, 0, false), code);
        code.append("  return res;\n");
        code.append("}\n");
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
            c.append(String.format("public static final int %s = %d;", decl.tokenName, id));
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

    void model(RuleDecl decl) {
        sb2 = new StringBuilder();
        sb.append(String.format("\npublic static class %s{\n", decl.name + nodeSuffix));
        indent(model(decl.rhs, decl.name + nodeSuffix));
        indent(sb2.toString(), sb);
        sb.append("\n}\n");
    }

    private String model(Node node, final String prefix) {
        final StringBuilder sb = new StringBuilder();
        if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                indent(model(ch, prefix), sb);
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                sb.append(String.format("public Token %s;\n", vName(name, prefix)));
            }
            else {
                sb.append(String.format("public %s%s %s;\n", name, nodeSuffix, vName(name, prefix)));
            }
        }
        else if (node.isOr()) {
            sb.append("public int which;\n");
            int num = 1;
            for (final Node ch : node.asOr()) {
                if (isSimple(ch)) {
                    sb.append(model(ch, prefix));
                }
                else {
                    String clsName = Utils.camel(prefix) + num;
                    sb.append(String.format("%s %s%d;\n", clsName, prefix.toLowerCase(), num));
                    StringBuilder s0 = new StringBuilder("\npublic static class " + clsName + "{\n");
                    indent(model(ch, clsName), s0);
                    s0.append("\n}\n");
                    sb2.append(s0);
                }
                num++;
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            Node ch = regex.node;
            if (regex.isOptional()) {
                return model(ch, prefix);
            }
            else {
                if (ch.isName()) {
                    String nm = vName(ch.asName(), prefix);
                    if (ch.asName().isToken) {
                        sb.append(String.format("public List<Token> %s = new ArrayList<>();\n", nm));
                    }
                    else {
                        sb.append(String.format("public List<%s%s> %s = new ArrayList<>();\n", ch, nodeSuffix, nm));
                    }
                }
                else {
                    //group
                    String var = "g" + groupCount++;
                    String cls = curRule + var;
                    sb.append(String.format("public List<%s> %s = new ArrayList<>();\n", cls, var));
                    StringBuilder s0 = new StringBuilder("\npublic static class " + cls + "{\n");
                    if (ch.isGroup()) {
                        ch = ch.asGroup().node;
                    }
                    indent(model(ch, cls), s0);
                    s0.append("\n}\n");
                    sb2.append(s0);
                }
            }
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            String var = "g" + groupCount++;
            String cls = curRule + var;
            sb.append(String.format("public %s %s;\n", cls, var));

            StringBuilder s0 = new StringBuilder("\npublic static class " + cls + "{\n");
            indent(model(group.node, cls), s0);
            s0.append("\n}\n");
            sb2.append(s0);
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass());
        }
        return sb.toString();
    }

    boolean isSimple(Node node) {
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return regex.node.isName();
        }
        return node.isName();
    }

    String vName(Name name, String cls) {
        Map<String, Integer> map = varCount.get(cls);
        if (map == null) {
            map = new HashMap<>();
            varCount.put(cls, map);
        }
        Integer i = map.get(cls);
        if (i == null) {
            map.put(name.name, 1);
            return name.name;
        }
        else {
            map.put(name.name, i + 1);
            return name.name + i;
        }
    }

    boolean arr(Node node) {
        return node.isStar() || node.isPlus();
    }

    String write(Node node, String outerVar, String outerCls, int flagCount, int firstCount, boolean isArr) {
        CodeWriter sb = new CodeWriter(true);
        if (node.isOr()) {
            Or or = node.asOr();
            sb.append(String.format("switch(%s){", peekExpr()));
            for (int i = 0; i < or.size(); i++) {
                Node ch = or.get(i);
                Set<Name> set = Helper.first(ch, tree, true, false, true);
                for (Name la : set) {
                    sb.append(String.format("case %s.%s:", tokens, la.name));
                }
                sb.append("{");
                sb.append(String.format("%s.which = %s;", outerVar, i + 1));
                if (isSimple(ch)) {
                    sb.all(write(ch, outerVar, outerCls, flagCount, firstCount, false));
                }
                else {
                    String varl = outerCls.toLowerCase() + (i + 1);
                    String cls = Utils.camel(outerCls) + (i + 1);
                    sb.append(String.format("%s %s = new %s();\n", curRule + "." + cls, varl, curRule + "." + cls));
                    sb.append(String.format("%s.%s = %s;\n", outerVar, varl, varl));
                    sb.all(write(ch, varl, cls, flagCount, firstCount, arr(ch)));
                }
                sb.append("break;");
                sb.append("}");
            }
            if (!Helper.canBeEmpty(node, tree)) {
                sb.append("default:{");
                StringBuilder arr = new StringBuilder();
                boolean first = true;
                for (Name nm : Helper.first(or, tree, true)) {
                    if (!first) {
                        arr.append(",");
                    }
                    arr.append(nm);
                    first = false;
                }
                sb.append(String.format("throw new RuntimeException(\"expecting one of [%s] got: \"+peek());", arr));
                sb.append("}");
            }
            sb.all("\n}");
        }
        else if (node.isGroup()) {
            String var = "g" + groupCount++;
            String cls = curRule + var;
            sb.append(String.format("%s %s = new %s();\n", curRule + "." + cls, var, curRule + "." + cls));
            if (isArr) {
                sb.append(String.format("%s.%s.add(%s);\n", outerVar, var, var));
            }
            else {
                sb.append(String.format("%s.%s = %s;\n", outerVar, var, var));
            }
            sb.append(write(node.asGroup().node, var, cls, flagCount, firstCount, false));
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                sb.append(write(s.get(i), outerVar, outerCls, flagCount, firstCount, false));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isRule()) {
                String args = "";
                if (!name.args.isEmpty()) {
                    throw new RuntimeException("node with args");
                }
                if (isArr) {
                    sb.append(String.format("%s.%s.add(%s(%s));\n", outerVar, vName(name, outerCls), name.name, args));
                }
                else {
                    sb.append(String.format("%s.%s = %s(%s);\n", outerVar, vName(name, outerCls), name.name, args));
                }
            }
            else {
                if (isArr) {
                    sb.append(String.format("%s.%s.add(consume(%s.%s));\n", outerVar, vName(name, outerCls), tokens, name.name));
                }
                else {
                    sb.append(String.format("%s.%s = consume(%s.%s);\n", outerVar, vName(name, outerCls), tokens, name.name));
                }
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isOptional()) {
                writeSwitch(regex.node, sb, "", write(regex.node, outerVar, outerCls, flagCount, firstCount, false));
            }
            else if (regex.isStar()) {
                flagCount++;
                String flagStr = "flag";
                if (flagCount > 1) flagStr += flagCount;
                sb.append(String.format("boolean %s = true;\n", flagStr));
                sb.append(String.format("while(%s){\n", flagStr));
                String def = flagStr + " = false;\n";
                writeSwitch(regex.node, sb, def, write(regex.node, outerVar, outerCls, flagCount, firstCount, true));
                sb.append("\n}\n");
            }
            else {
                flagCount++;
                firstCount++;
                String flagStr = "flag";
                String firstStr = "first";
                if (flagCount > 1) flagStr += flagCount;
                if (firstCount > 1) firstStr += firstCount;
                sb.append(String.format("boolean %s = true;\n", flagStr));
                sb.append(String.format("boolean %s = true;\n", firstStr));
                sb.append(String.format("while(%s){\n", flagStr));
                String def = String.format("if(!%s)  %s = false;\n" + "else  throw new RuntimeException(\"unexpected token: \"+peek());", firstStr, flagStr);
                writeSwitch(regex.node, sb, def, write(regex.node, outerVar, outerCls, flagCount, firstCount, true));
                sb.append("first = false;\n");
                sb.append("\n}\n");
            }
        }
        else {
            throw new RuntimeException("unexpected " + node);
        }
        return sb.get();
    }


    void writeSwitch(Node node, CodeWriter sb, String def, String action) {
        sb.append("switch(" + peekExpr() + "){");
        for (Name token : Helper.first(node, tree, true, false, true)) {
            sb.append(String.format("case %s.%s:", tokens, token));
        }
        sb.append("{");
        sb.all(action);
        sb.all("}\nbreak;");
        if (!def.isEmpty()) {
            sb.append("default:{");
            sb.all(def);
            sb.append("}");
        }
        sb.append("}");
    }
}
