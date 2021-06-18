package mesut.parserx.gen.lr;

import mesut.parserx.gen.Template;

import java.io.IOException;

public class CodeGen {
    public String parser_class;
    public String lexer_class;
    public String lexer_method;
    LrDFA<?> dfa;

    public void gen() throws IOException {
        //File file = new File();

        Template template = new Template("lalr1.java.template");
        template.set("parser_class", parser_class);
        template.set("lexer_class", lexer_class);
        template.set("lexer_method", lexer_method);
    }
}
