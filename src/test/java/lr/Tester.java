package lr;

import java.io.StringReader;

class Tester {

    public static void main(String[] a) throws Exception {
        //String in = "1+(4+(6+9))";
        //String in = "(6+(4+7))";
        //String in = "1+4+6";
        //String in = "(1*4^2+6)*7";
        //String in = "1+-2";
        //String in = "1?2:3?4:5";
        String in = "1+2*-3-6";

        Lexer l = new Lexer(new StringReader(in));
        Parser p = new Parser(l);
        //Parser.debug = true;
        p.parse();
    }


}
