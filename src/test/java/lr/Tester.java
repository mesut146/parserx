package lr;

import java.io.StringReader;

class Tester {

    public static void main(String[] a) throws Exception {
        //String in = "1+(4+(6+9))";
        String in = "(6+4)";

        Lexer l = new Lexer(new StringReader(in));
        Parser p = new Parser(l);
        //Parser.debug = true;
        p.parse();
    }


}
