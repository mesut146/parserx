package parser.pred;

import java.io.IOException;
import java.io.StringReader;

public class Test {
    @org.junit.Test
    public void name() throws IOException {
        Lexer lexer = new Lexer(new StringReader("2^3"));
        Parser parser = new Parser(lexer);
        System.out.println(parser.E());
    }
}
