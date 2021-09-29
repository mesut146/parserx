package mesut.parserx.nodes;

import mesut.parserx.regex.RegexFromStr;

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
                regex = "/\\*(*[^/]|[^*])*\\*/";
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
                regex = "\"(\\\\.|[^\r\n\"])*\"";
                break;
            default:
                throw new RuntimeException("unknown shortcut: " + name);
        }
        return RegexFromStr.build(regex);
    }


    @Override
    public String toString() {
        return "[:" + name + ":]";
    }
}