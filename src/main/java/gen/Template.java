package gen;

import nodes.NodeList;
import utils.Helper;

import java.io.IOException;
import java.util.*;

public class Template {
    private final List<String> list;

    public Template(String fileName, String... names) throws IOException {
        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        String str = Helper.read(getClass().getResourceAsStream(fileName));
        List<part> indexes = new ArrayList<>();
        for (String nm : names) {
            String dlr = "$" + nm + "$";
            mark(str, dlr, indexes);
        }
        Collections.sort(indexes, new Comparator<part>() {
            @Override
            public int compare(part o1, part o2) {
                return Integer.compare(o1.index, o2.index);
            }
        });
        int pos = 0;
        list = new ArrayList<>();
        for (part part : indexes) {
            String prev = str.substring(pos, part.index);
            if (!prev.isEmpty()) {
                list.add(prev);
            }
            list.add(part.name);
            pos = part.index + part.name.length();
        }
        list.add(str.substring(pos));
    }

    //mark positions of variable #name
    void mark(String str, String name, List<part> parts) {
        int pos = 0;
        while (pos < str.length()) {
            pos = str.indexOf(name, pos);
            if (pos != -1) {
                parts.add(new part(name, pos));
                pos = pos + name.length();
            }
            else {
                break;
            }
        }
    }

    public void set(String name, String val) {
        if (!name.startsWith("$")) {
            name = "$" + name + "$";
        }
        for (ListIterator<String> iterator = list.listIterator(); iterator.hasNext(); ) {
            if (iterator.next().equals(name)) {
                iterator.set(val);
            }
        }
    }

    @Override
    public String toString() {
        return NodeList.join(list, "");
    }

    static class part {
        String name;
        int index;

        public part(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}
