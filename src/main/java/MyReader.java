import java.io.FileNotFoundException;
import java.io.FileReader;

public class MyReader {

    public StringBuilder backup;
    public StringBuilder buf;
    FileReader reader;

    public MyReader(String path) throws FileNotFoundException {
        reader = new FileReader(path);
        buf = new StringBuilder();
        backup = new StringBuilder();
    }

    public int read(){
        return 0;
    }
}
