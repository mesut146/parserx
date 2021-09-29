package lexer.itself;

import mesut.parserx.parser2.Lexer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class Tester {
    @Test
    public void name() throws IOException {
        //Lexer l = new Lexer(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        Lexer l = new mesut.parserx.parser2.Lexer(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/resources/str.g"));
        //Lexer l = new Lexer(new StringReader("[:string:]"));
        while (true) {
            mesut.parserx.parser2.Token t = l.next();
            System.out.println(t);
            if (t.type == 0) {
                break;
            }
        }
    }
}
