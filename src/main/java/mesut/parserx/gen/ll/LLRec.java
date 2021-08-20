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
    Name curRule;
    Map<Name, Integer> varCount = new HashMap<>();

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
        for (String line : data.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
    }

    public void gen() throws IOException {
        EbnfToBnf.combine_or = true;
        EbnfToBnf.expand_or = false;
        tree = EbnfToBnf.transform(tree);
        sb.append("public class ").append(options.parserClass).append("{\n");

        writeConsume();
        writePop();

        for (RuleDecl decl : tree.rules) {
            if (!decl.hidden) {
                curRule = decl.ref();
                gen(decl);
            }
        }
        sb.append("}");

        IOUtils.write(sb.toString(), new File(options.outDir, options.parserClass + ".java"));
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
        sb.append(String.format("public static class %s{\n", decl.name + "Node"));
        indent(model(decl.rhs));
        sb.append("\n}\n");
    }

    private String model(Node node) {
        StringBuilder sb = new StringBuilder();
        if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                indent(model(ch), sb);
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                sb.append("Token ").append(vName(name)).append(";\n");
            }
            else {
                sb.append(name).append("Node ").append(vName(name)).append(";\n");
            }
        }
        else if (node.isOr()) {
            sb.append("public int which;\n");
            int num = 1;
            for (Node ch : node.asOr()) {
                sb.append(curRule.name).append(num).append(" ").append(curRule.name.toLowerCase()).append(num).append(";\n");
                num++;
            }
            num = 1;
            for (Node ch : node.asOr()) {
                StringBuilder s0 = new StringBuilder("public class " + curRule.name + num + "{\n");
                indent(model(ch), s0);
                s0.append("\n}\n");
                sb.append(s0);
                num++;
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isOptional()) {
                return model(regex.node);
            }
            else {
                sb.append("public List<");
                if (regex.node.isName()) {
                    if (regex.node.asName().isToken) {
                        sb.append("Token");
                    }
                    else {
                        sb.append(regex.node).append("Node");
                    }
                }
                else {
                    //group
                    sb.append(curRule.name).append("g1");
                }
                sb.append("> ");
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass());
        }
        return sb.toString();
    }

    String vName(Name name) {
        if (varCount.containsKey(name)) {
            int count = varCount.get(name);
            varCount.put(name, count + 1);
            return name.name + count;
        }
        else {
            varCount.put(name, 1);
            return name.name;
        }
    }

    void gen(RuleDecl decl) {
        model(decl);
        /*sb.append("public void ").append(decl.name).append("(");
        if (!decl.args.isEmpty()) {
            sb.append(NodeList.join(decl.args, ", "));
        }
        sb.append("){\n");

        write(decl.rhs);

        sb.append("}");*/
    }

    Name first(Node node) {
        return Helper.first(node, tree, true).iterator().next();
    }

    void writeSwitch(Node node, String s, String s2) {

    }

    String write(Node node) {
        StringBuilder sb = new StringBuilder();
        if (node.isOr()) {
            Or or = node.asOr();
            sb.append("switch(peek().type){\n");
            for (int i = 0; i < or.size(); i++) {
                Node ch = or.get(i);
                Set<Name> set = Helper.first(ch, tree, true);
                for (Name la : set) {
                    sb.append("case ").append(la.name).append(":\n");
                }
                sb.append("{\n");
                write(ch);
                sb.append("}\n");
            }
            sb.append("default: throw new RuntimeException(\"err\")");
            sb.append("\n}");
        }
        else if (node.isGroup()) {
            write(node.asGroup().node);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                write(s.get(i));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isRule()) {
                sb.append(name);//with args
            }
            else {
                sb.append(String.format("consume(TokenType.%s);", name.name));
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isOptional()) {
                writeSwitch(regex.node, "", write(regex.node));
            }
        }
        else {
            throw new RuntimeException("unexpected " + node);
        }
        return sb.toString();
    }


}
