import mesut.parserx.gen.CodeWriter;
import org.junit.Test;

public class CodeWriterTest {

    @Test
    public void append() {
        CodeWriter writer = new CodeWriter(true);

        writer.append("if(){");
        writer.append("int x = 5;");
        writer.append("}");
        writer.append("//hello");
        System.out.println(writer.get());
    }

    @Test
    public void all() {
        CodeWriter writer = new CodeWriter(true);

        writer.append("if(){");
        writer.all("int x = 5;\nx = 1;");
        writer.append("}");
        writer.append("//hello");
        System.out.println(writer.get());
    }
}
