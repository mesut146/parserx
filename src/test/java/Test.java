import java.io.StringReader;

public class Test {


    public static void main(String[] ar) throws Exception {
        Lexer lexer = new Lexer(new StringReader("1+2"));
        /*for (; ; ) {
            Token token = lexer.next();
            if (token.type == 0) break;
            System.out.println(token);
        }*/
        Parser parser = new Parser(lexer);
        System.out.println(eval(parser.expr()));
    }

    static double eval(Parser.expr e) {
        double d = eval(e.mul);
        for (int i = 0; i < e.g1.size(); i++) {
            Parser.expr.exprg1 g1 = e.g1.get(i);
            if (g1.g2.PLUS != null) {
                d += eval(g1.mul);
            }
            else {
                d -= eval(g1.mul);
            }
        }
        return d;
    }

    static double eval(Parser.mul e) {
        double d = eval(e.atom);
        for (int i = 0; i < e.g1.size(); i++) {
            Parser.mul.mulg1 g1 = e.g1.get(i);
            if (g1.g2.MUL != null) {
                d *= eval(g1.atom);
            }
            else {
                d /= eval(g1.atom);
            }
        }
        return d;
    }

    static double eval(Parser.atom e) {
        if (e.NUMBER != null) {
            return Double.parseDouble(e.NUMBER.value);
        }
        if (e.atom1 != null) {
            return eval(e.atom1.expr);
        }
        return -eval(e.atom2.atom);
    }

}
