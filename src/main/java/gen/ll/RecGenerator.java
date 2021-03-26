package gen.ll;

import com.squareup.javapoet.MethodSpec;
import com.sun.org.apache.xpath.internal.operations.Or;
import gen.LexerGenerator;
import nodes.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecGenerator {
    LexerGenerator lexerGenerator;
    Tree tree;
    List<RuleDecl> rules;
    RuleDecl curRule;
    int count = 0;
    Map<String, String> laName = new HashMap<>();

    public RecGenerator(Tree tree) {
        this.tree = tree;
    }

    public void generate() {
        mergeOrs();
        for (RuleDecl rule : rules) {
            curRule = rule;
            count = 0;
            MethodSpec main = MethodSpec.methodBuilder(rule.name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addCode(makeProd(rule.rhs))
                    .build();
            System.out.println(main);
        }
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

    String makeLa(Node node) {
        if (node.isSequence()) {
            Sequence s = node.asSequence();
        }
        else if (node.isName()) {
            NameNode name = node.asName();
            if (name.isToken) {
                return "tokens.get(0).type == " + name.name;
            }
            else {

            }
        }
        else {

        }
        return "la(" + node + ")";
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
            if (name.isToken) {
                sb.append("pop();\n");
            }
            else {
                sb.append(name.name).append("()");
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (Node ch : s) {
                append(sb, makeProd(ch));
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

    void append(StringBuilder sb, String s) {
        for (String line : s.split("\n")) {
            sb.append("  ").append(line).append("\n");
        }
    }
}
