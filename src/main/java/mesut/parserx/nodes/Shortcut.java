package mesut.parserx.nodes;

import mesut.parserx.regex.parser.RegexVisitor;

import java.io.IOException;

public class Shortcut extends Node {
    public String name;

    public Shortcut(String name) {
        this.name = name;
    }

    public static Node from(String name) {
        String regex;
        switch (name) {
            case "line_comment":
                regex = "//[^\\n]*";
                break;
            case "block_comment":
                regex = "/\\*(\\*[^/]|[^*])*\\*/";
                break;
            case "ident":
                regex = "[a-zA-Z_][a-zA-Z0-9_]*";
                break;
            case "integer":
                regex = "[0-9]+";
                break;
            case "decimal":
                regex = "[0-9]+(\\.[0-9]+)?";
                break;
            case "string":
                regex = "\"(\\\\.|[^\r\n\\\\\"])*\"";
                break;
            case "char":
                regex = "'(\\\\.|[^\r\n\\\\'])*'";
                break;
            default:
                throw new RuntimeException("unknown shortcut: " + name);
        }
        try {
            return RegexVisitor.make(regex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return "[:" + name + ":]";
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitShortcut(this, arg);
    }
}
