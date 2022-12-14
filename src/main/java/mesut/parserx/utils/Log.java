package mesut.parserx.utils;

import java.util.logging.Level;

public class Log {
    public static Level curLevel = Level.OFF;

    public static void log(Level level, String msg) {
        if (curLevel.intValue() <= level.intValue()) {
            System.out.println(msg);
        }
    }

}
