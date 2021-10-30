package parser;

import common.Env;
import org.junit.Test;

public class AstPrint {
    @Test
    public void test() throws Exception {
        DescTester.check(Env.tree("ll/ast.g"), "E", "123", "1+2", "1+2*3");
    }
}
