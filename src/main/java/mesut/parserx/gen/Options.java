package mesut.parserx.gen;

public class Options {
    public String parserClass = "Parser";
    public String lexerClass = "Lexer";
    public String lexerFunction = "next";
    public String tokenClass = "Token";
    public String packageName;
    public String outDir;
    public boolean genVisitor = false;
    public String astClass = "Ast";
    public String nodeSuffix = "";
    public boolean useSimple = true;

    public String sequenceDelimiter = ", ";
    public String arrayDelimiter = ", ";
    public boolean dump = false;
}
