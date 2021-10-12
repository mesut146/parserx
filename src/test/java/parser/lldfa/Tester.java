package parser.lldfa;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class Tester {
    @Test
    public void test() throws IOException {
        Lexer1 lexer1 = new Lexer1(new StringReader("1+2"));
        Parser1 parser1 = new Parser1(lexer1);
        parser1.parse();
    }
}
