import java.util.List;
import java.util.ArrayList;

public class Parser{
    List<Token> list = new ArrayList<>();
    Lexer lexer;
    public Parser(Lexer lexer) throws java.io.IOException{
        this.lexer = lexer;
        fill();
    }
    Token consume(int type){
        Token t = pop();
        if(t.type != type)
            throw new RuntimeException("unexpected token: " + t + " expecting: " + type);
        return t;

    }
    Token pop(){
        return list.remove(0);
    }
    Token peek(){
        return list.get(0);
    }
    void fill() throws java.io.IOException{
        while(true){
            Token t = lexer.next();
            list.add(t);
            if(t == null || t.type == 0) return;
        }
    }

    public static class mul{
        public atom atom;
        public List<mulg1> g1 = new ArrayList<>();

        public static class mulg2{
            public int which;
            public Token MUL;
            public Token DIV;

        }

        public static class mulg1{
            public mulg2 g2;
            public atom atom;

        }

    }

    public static class expr{
        public mul mul;
        public List<exprg1> g1 = new ArrayList<>();

        public static class exprg2{
            public int which;
            public Token PLUS;
            public Token MINUS;

        }

        public static class exprg1{
            public exprg2 g2;
            public mul mul;

        }

    }

    public static class atom{
        public int which;
        Atom1 atom1;
        Atom2 atom2;
        public Token NUMBER;

        public static class Atom1{
            public Token LP;
            public expr expr;
            public Token RP;

        }

        public static class Atom2{
            public Token MINUS;
            public atom atom;

        }

    }
    public mul mul(){
        mul res = new mul();
        res.atom = atom();


        boolean flag = true;

        while(flag){

            switch(peek().type){
                case Tokens.MUL:
                case Tokens.DIV:
                {
                    mul.mulg1 g1 = new mul.mulg1();
                    res.g1.add(g1);
                    mul.mulg2 g2 = new mul.mulg2();
                    g1.g2 = g2;
                    switch(peek().type){
                        case Tokens.MUL:
                        {
                            g2.which = 1;
                            g2.MUL = consume(Tokens.MUL);
                            break;
                        }
                        case Tokens.DIV:
                        {
                            g2.which = 2;
                            g2.DIV = consume(Tokens.DIV);
                            break;
                        }
                        default:{
                            throw new RuntimeException("expecting one of [MUL,DIV] got: "+peek());
                        }
                    }
                    g1.atom = atom();
                }
                break;
                default:{
                    flag = false;
                }
            }

        }
        return res;
    }
    public expr expr(){
        expr res = new expr();
        res.mul = mul();


        boolean flag = true;

        while(flag){

            switch(peek().type){
                case Tokens.PLUS:
                case Tokens.MINUS:
                {
                    expr.exprg1 g1 = new expr.exprg1();
                    res.g1.add(g1);
                    expr.exprg2 g2 = new expr.exprg2();
                    g1.g2 = g2;
                    switch(peek().type){
                        case Tokens.PLUS:
                        {
                            g2.which = 1;
                            g2.PLUS = consume(Tokens.PLUS);
                            break;
                        }
                        case Tokens.MINUS:
                        {
                            g2.which = 2;
                            g2.MINUS = consume(Tokens.MINUS);
                            break;
                        }
                        default:{
                            throw new RuntimeException("expecting one of [PLUS,MINUS] got: "+peek());
                        }
                    }
                    g1.mul = mul();
                }
                break;
                default:{
                    flag = false;
                }
            }

        }
        return res;
    }
    public atom atom(){
        atom res = new atom();
        switch(peek().type){
            case Tokens.LP:
            {
                res.which = 1;
                atom.Atom1 atom1 = new atom.Atom1();

                res.atom1 = atom1;

                atom1.LP = consume(Tokens.LP);
                atom1.expr = expr();
                atom1.RP = consume(Tokens.RP);
                break;
            }
            case Tokens.MINUS:
            {
                res.which = 2;
                atom.Atom2 atom2 = new atom.Atom2();

                res.atom2 = atom2;

                atom2.MINUS = consume(Tokens.MINUS);
                atom2.atom = atom();
                break;
            }
            case Tokens.NUMBER:
            {
                res.which = 3;
                res.NUMBER = consume(Tokens.NUMBER);
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [NUMBER,LP,MINUS] got: "+peek());
            }
        }
        return res;
    }
}