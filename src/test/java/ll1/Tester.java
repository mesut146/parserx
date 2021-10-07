package ll1;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class Tester {
    @Test
    public void name() throws IOException {
        Lexer l = new Lexer(new StringReader("a=1+2+3+4"));
        /*while (true) {
            Token t = l.next();
            System.out.println(t);
            if (t.type == 0) return;
        }*/

        Parser p = new Parser(l);
        System.out.println(p.line());
    }
}
