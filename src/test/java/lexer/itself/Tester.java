package lexer.itself;

import common.Env;
import org.junit.Test;

import java.io.IOException;

public class Tester {
    @Test
    public void name() throws IOException {
        Lexer2.bufSize = 9;
        Lexer2 lexer2 = new Lexer2(Env.getResFile("java/large.java"));
        for (; ; ) {
            Token2 token2 = lexer2.next();
            System.out.println(token2);
            if (token2.type == 0) break;
        }
    }
}
