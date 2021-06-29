package mesut.parserx.gen.ll;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.Set;

public class LLRec {
    Tree tree;
    StringBuilder sb = new StringBuilder();

    public void gen() {
        sb.append("public class Parser{\n");

        sb.append("void consume(TokenType type){\n");
        sb.append("  Token t = pop();\n");
        sb.append("  if(!t.is(type)) throw new RuntimeException(\"unexpected token: \"+t+\" expecting: \"+type\")");
        sb.append("\n}");

        sb.append("Token pop(){\n");
        sb.append("  return list.remove(0);");
        sb.append("}");

        for (RuleDecl decl : tree.rules) {
            if (!decl.hidden) {
                gen(decl);
            }
        }
        sb.append("}");
    }

    void gen(RuleDecl decl) {
        sb.append("public void ").append(decl.name).append("(");
        if (!decl.args.isEmpty()) {
            sb.append(NodeList.join(decl.args, ", "));
        }
        sb.append("){\n");

        write(decl.rhs);

        sb.append("}");
    }

    Name first(Node node) {
        return Helper.first(node, tree, true).iterator().next();
    }

    void writeSwitch() {

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
