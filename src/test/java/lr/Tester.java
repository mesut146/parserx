package lr;

import java.io.StringReader;

class Tester {

    public static void main(String[] a) throws Exception {
        String in = "(1)";

        Lexer l = new Lexer(new StringReader(in));
        Parser p = new Parser(l);
        p.parse();
    }


}
