package mesut.parserx.gen;

public class Options {
    public String parserClass = "Parser";
    public String lexerClass = "Lexer";
    public String lexerFunction = "next";
    public String tokenClass = "Token";
    public String packageName;
    public String outDir;
    public String astClass = "Ast";
    public String nodeSuffix = "";

    public String sequenceDelimiter = ", ";
    public String arrayDelimiter = ", ";
    public boolean dump = false;
}
