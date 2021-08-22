package mesut.parserx.gen.ll;

import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.IOUtils;

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
    String nodeSuffix = "Node";
    int groupCount;
    boolean hasFirst;
    boolean hasFlag;

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
        sb.append("public class ").append(options.parserClass).append("{\n");

        writeConsume();
        writePop();

        for (RuleDecl decl : tree.rules) {
            if (!decl.hidden) {
                curRule = decl.name;
                groupCount = 1;
                hasFirst = false;
                hasFlag = false;
                varCount.clear();
                gen(decl);
            }
        }
        indent(code.toString(), sb);
        sb.append("}");

        IOUtils.write(sb.toString(), new File(options.outDir, options.parserClass + ".java"));
    }

    void gen(RuleDecl decl) {
        model(decl);

        String type = decl.name + nodeSuffix;
        code.append(String.format("public %s %s(%s){\n", type, decl.name, NodeList.join(decl.args, ", ")));
        code.append(String.format("  %s res = new %s();\n", type, type));
        indent(write(decl.rhs, "res", type), code);
        code.append("  return res;\n");
        code.append("}\n");
    }

    void writeConsume() {
        String s = "void consume(TokenType type){\n" +
                "  Token t = pop();\n" +
                "  if(!t.is(type)) throw new RuntimeException(\"unexpected token: \"+t+\" expecting: \"+type\")" +
                "\n}";
        indent(s);
    }

    void writePop() {
        String s = "Token pop(){\n" +
                "  return list.remove(0);\n" +
                "}";
        indent(s);
    }

    void model(RuleDecl decl) {
        sb2 = new StringBuilder();
        sb.append(String.format("\npublic static class %s{\n", decl.name + "Node"));
        indent(model(decl.rhs, decl.name));
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
                    String clsName = camel(prefix) + num;
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
            if (regex.isOptional()) {
                return model(regex.node, prefix);
            }
            else {
                if (regex.node.isName()) {
                    String nm = vName(regex.node.asName(), prefix);
                    if (regex.node.asName().isToken) {
                        sb.append(String.format("public List<Token> %s = new ArrayList<>();\n", nm));
                    }
                    else {
                        sb.append(String.format("public List<%s%s> %s = new ArrayList<>();\n", regex.node, nodeSuffix, nm));
                    }
                }
                else {
                    //group
                    String cls = curRule + "g" + groupCount++;
                    sb.append(String.format("public List<%s> %s = new ArrayList<>();\n", cls, cls.toLowerCase()));
                    StringBuilder s0 = new StringBuilder("\npublic static class " + cls + "{\n");
                    indent(model(regex.node, cls), s0);
                    s0.append("\n}\n");
                    sb2.append(s0);
                }
            }
        }
        else if (node.isGroup()) {
            final Group group = node.asGroup();
            final String s = "g" + groupCount++;
            String cls = curRule + s;
            sb.append(String.format("public %s %s%s;\n", cls, curRule.toLowerCase(), s));

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

    String camel(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
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

    String write(Node node, String vname, String cls) {
        StringBuilder sb = new StringBuilder();
        if (node.isOr()) {
            Or or = node.asOr();
            sb.append("switch(peek().type){\n");
            for (int i = 0; i < or.size(); i++) {
                Node ch = or.get(i);
                String var = cls + (i + 1);
                Set<Name> set = Helper.first(ch, tree, true, false, true);
                for (Name la : set) {
                    sb.append("  case ").append(la.name).append(":\n");
                }
                sb.append("  {\n");
                if (isSimple(ch)) {
                    indent(write(ch, var, cls), sb);
                }
                else {
                    sb.append(String.format("  %s.%s = new %s();\n", vname, var, var));
                    indent(write(ch, var, cls), sb);
                }
                sb.append("break;\n");
                sb.append("  }\n");
            }
            sb.append("  default: \n  throw new RuntimeException(\"err\");");
            sb.append("\n}\n");
        }
        else if (node.isGroup()) {
            sb.append(write(node.asGroup().node, vname, cls));
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                sb.append(write(s.get(i), vname, cls));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isRule()) {
                sb.append(vname).append(".").append(vName(name, cls)).append(" = ");
                sb.append(name);
                sb.append("(");
                //with args
                sb.append(");\n");
            }
            else {
                sb.append(vname).append(".").append(vName(name, cls)).append(" = ");
                sb.append(String.format("consume(TokenType.%s);\n", name.name));
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isOptional()) {
                //empty default
                writeSwitch(regex.node, sb, "", write(regex.node, vname, cls));
            }
            else if (regex.isStar()) {
                //while
                if (!hasFlag) {
                    hasFlag = true;
                    sb.append("boolean ");
                }
                sb.append("flag = true;\n");
                sb.append("while(flag){\n");
                String def = "flag = false;\n";
                writeSwitch(regex.node, sb, def, write(regex.node, vname, cls));
                sb.append("\n}\n");
            }
            else {
                //do while
                if (!hasFlag) {
                    hasFlag = true;
                    sb.append("boolean ");
                }
                sb.append("flag = true;\n");
                if (!hasFirst) {
                    hasFirst = true;
                    sb.append("boolean ");
                }
                sb.append("first = true;\n");
                sb.append("while(flag){\n");
                String def = "if(!first)  flag = false;\n" + "else  throw new RuntimeException(\"unexpected token: \"+peek());";
                writeSwitch(regex.node, sb, def, write(regex.node, vname, cls));
                sb.append("first = false;\n");
                sb.append("\n}\n");
            }
        }
        else {
            throw new RuntimeException("unexpected " + node);
        }
        return sb.toString();
    }

    void writeSwitch(Node node, StringBuilder sb, String def, String action) {
        sb.append("switch(peek().type){\n");
        for (Name token : Helper.first(node, tree, true, false, true)) {
            sb.append("  case TokenType.").append(token).append(":\n");
        }
        sb.append("  {\n");
        indent(action, sb);
        sb.append("  }\n  break;\n");
        if (!def.isEmpty()) {
            sb.append("  default:\n");
            indent(def, sb);
        }
        sb.append("}\n");
    }
}
