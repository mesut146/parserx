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
        String in = "1+2*-3+6^2";

        Lexer l = new Lexer(new StringReader(in));
        Parser p = new Parser(l);
        //Parser.debug = true;
        Symbol sym = p.parse();
        Ast.E res = AstBuilder.makeE(sym);
        System.out.println(eval(res));
    }

    static double eval(Ast.E e) {
        if (e.NUMBER != null) {
            return Integer.parseInt(e.NUMBER.value);
        }
        if (e.which == 1) {
            return -eval(e.e1.E);
        }
        if (e.which == 2) {
            return eval(e.e2.E);
        }
        if (e.which == 4) {
            return Math.pow(eval(e.e4.E), eval(e.e4.E2));
        }
        if (e.which == 5) {
            return eval(e.e5.E) * eval(e.e5.E2);
        }
        if (e.which == 6) {
            return eval(e.e6.E) / eval(e.e6.E2);
        }
        if (e.which == 7) {
            return eval(e.e7.E) + eval(e.e7.E2);
        }
        if (e.which == 8) {
            return eval(e.e8.E) - eval(e.e8.E2);
        }
        return 0;
    }


}
