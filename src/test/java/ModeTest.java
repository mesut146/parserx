import common.Env;
import lexer.RealTest;
import mesut.parserx.nodes.Tree;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ModeTest {

    static String readBlock(File path) throws IOException {
        var str = Files.readString(path.toPath());
        var p= Pattern.compile("/\\*(.*)\\*/",Pattern.DOTALL);
        var m=p.matcher(str);
        if(m.find()){
            return m.group(1);
        }
        throw new IOException("don't match");
    }

    @Test
    public void dfaTest() throws IOException {
        var path = Env.getResFile("lexer/mode.g");
        Tree tree = Tree.makeTree(path);
        var nfa = tree.makeNFA();
        var dfa = nfa.dfa();
        var dumped=dfa.toString().trim();
        var expected = readBlock(path);
        Assert.assertEquals(expected,dumped);
    }

    @Test
    public void lexerTest() throws Exception {
        RealTest.check(Env.tree("lexer/mode.g"), "bbac", "bacd");
        RealTest.check(Env.tree("lexer/xml-mode.g"), "<tag a=\"val\">");
    }
}
