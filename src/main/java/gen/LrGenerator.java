package gen;

//lr(1),lalr(1)
public class LrGenerator extends IndentWriter {
    String dir;
    LexerGenerator lexerGenerator;

    public LrGenerator(LexerGenerator lexerGenerator, String dir) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
    }


    public void generate() {

    }
}
