package mesut.parserx.gen.ll;

import com.squareup.javapoet.MethodSpec;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.IOUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecGenerator {
    public String lexerClass;
    public String outDir;
    public String className;
    Tree tree;
    List<RuleDecl> rules;
    RuleDecl curRule;
    int count = 0;
    Map<String, String> laNameMap = new HashMap<>();//la name for ref
    Map<Node, String> laMap = new HashMap<>();
    List<MethodSpec> laMethods = new ArrayList<>();

    public RecGenerator(Tree tree) {
        this.tree = tree;
    }

    public void generate() throws IOException {
        mergeOrs();
        StringBuilder prods = new StringBuilder();
        StringBuilder laList = new StringBuilder();
        for (RuleDecl rule : rules) {
            curRule = rule;
            count = 0;
            MethodSpec main = MethodSpec.methodBuilder(rule.name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addCode(makeProd(rule.rhs))
                    .build();
            prods.append(main);
        }
        for (MethodSpec spec : laMethods) {
            laList.append(spec);
        }
        Template template = new Template("rec.java.template");

        template.set("parser_class", "GeneratedParser");
        template.set("lexer_class", lexerClass);
        template.set("prod_list", prods.toString());
        template.set("la_list", laList.toString());
        File file = new File(outDir, className + ".java");
        IOUtils.write(template.toString(), file);
    }

    void mergeOrs() {
        rules = new ArrayList<>();
        Map<String, OrNode> map = new HashMap<>();
        for (RuleDecl decl : tree.rules) {
            OrNode or = map.get(decl.name);
            if (or == null) {
                or = new OrNode();
                map.put(decl.name, or);

            }
            or.add(decl.rhs);
        }
        for (Map.Entry<String, OrNode> entry : map.entrySet()) {
            rules.add(new RuleDecl(entry.getKey(), entry.getValue().normal()));
        }
    }

    boolean isOpt(Node node) {
        if (node.isRegex()) {
            return !node.asRegex().isPlus();
        }
        return false;
    }

    RuleDecl getRule(String name) {
        for (RuleDecl ruleDecl : rules) {
            if (ruleDecl.name.equals(name)) {
                return ruleDecl;
            }
        }
        return null;
    }

    String laFuncName(NameNode name) {
        if (laNameMap.containsKey(name.name)) {
            return laNameMap.get(name.name);
        }
        String s = "la_" + name.name;
        laNameMap.put(name.name, s);
        makeLa(getRule(name.name).rhs);
        return s;
    }

    String laFunc(Node node) {
        if (laMap.containsKey(node)) {
            return laMap.get(node);
        }
        String s = laName();
        laMap.put(node, s);
        return s;
    }

    //make func call for la
    String makeLa(Node node) {
        if (laMap.containsKey(node)) {
            return laMap.get(node);
        }
        String fName = laName();
        laMap.put(node, fName + "()");
        StringBuilder sb = new StringBuilder();
        if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                sb.append("if(!").append(makeLa(s.get(i))).append(") return false;\n");
            }
            sb.append("return true;");
        }
        else if (node.isName()) {
            NameNode name = node.asName();
            if (name.isToken) {
                sb.append("return la().type == ").append(name.name);
            }
            else {
                sb.append(makeLa(getRule(name.name).rhs));
            }
        }
        else if (node.isOr()) {
            OrNode or = node.asOr();
            boolean first = true;
            for (Node ch : or) {
                sb.append(first ? "if(" : "else if(").append(makeLa(ch)).append(") return true;\n");
                first = false;
            }
            sb.append("return false;");
        }
        else if (node.isGroup()) {
            return makeLa(node.asGroup().node);
        }
        else if (node.isRegex()) {
            RegexNode regexNode = node.asRegex();
            String s = makeLa(regexNode.node);
            if (regexNode.isStar()) {
                sb.append("if(").append(s).append(") return true;\n");
                sb.append("return true;");
            }
            else if (regexNode.isOptional()) {
                sb.append("if(").append(s).append(") return true;\n");
                sb.append("return true;");
            }
            else {
                sb.append("return ").append(s).append(";");
            }
        }
        else {
            throw new RuntimeException("la " + node.getClass());
        }
        MethodSpec main = MethodSpec.methodBuilder(fName)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addCode(sb.toString())
                .build();
        laMethods.add(main);
        return fName + "()";
    }

    String laName() {
        return curRule.name + count++;
    }

    String makeProd(Node node) {
        StringBuilder sb = new StringBuilder();
        if (node.isOr()) {
            OrNode or = node.asOr();
            for (int i = 0; i < or.size(); i++) {
                if (i == or.size() - 1) {
                    sb.append("else{\n");
                }
                else {
                    if (i > 0) {
                        sb.append("else ");
                    }
                    sb.append("if(").append(makeLa(or.get(i))).append("){\n");
                }
                append(sb, makeProd(or.get(i)));
                sb.append("}\n");
            }
        }
        else if (node.isGroup()) {
            sb.append(makeProd(node.asGroup().node));
        }
        else if (node.isName()) {
            NameNode name = node.asName();
            sb.append(name.name).append(" = ");
            if (name.isToken) {
                sb.append("pop();\n");
            }
            else {
                sb.append(name.name).append("();\n");
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (Node ch : s) {
                //append(sb, makeProd(ch));
                sb.append(makeProd(ch));
            }
            sb.append("\n");
        }
        else if (node.isRegex()) {
            RegexNode regexNode = node.asRegex();
            if (regexNode.isOptional()) {
                sb.append("if(").append(makeLa(regexNode.node)).append("){\n");
                append(sb, makeProd(regexNode.node));
                sb.append("}\n");
            }
            else if (regexNode.isStar()) {
                sb.append("while(").append(makeLa(regexNode.node)).append("){\n");
                append(sb, makeProd(regexNode.node));
                sb.append("}\n");
            }
            else {
                sb.append("do{\n");
                append(sb, makeProd(regexNode.node));
                sb.append("}while(").append(makeLa(regexNode.node)).append(");\n");
            }
        }
        else {
            throw new RuntimeException(node.toString());
        }
        return sb.toString();
    }

    String indent(String code) {
        StringBuilder sb = new StringBuilder();
        for (String line : code.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
        return sb.toString();
    }

    void append(StringBuilder sb, String s) {
        for (String line : s.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
    }
}
