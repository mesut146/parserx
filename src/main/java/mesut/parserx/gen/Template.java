package mesut.parserx.gen;

import mesut.parserx.nodes.NodeList;
import mesut.parserx.utils.IOUtils;

import java.io.IOException;
import java.util.*;

public class Template {
    private final List<String> list = new ArrayList<>();
    Map<String, String> varMap = new HashMap<>();
    String content;

    public Template(String fileName) throws IOException {
        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        content = IOUtils.read(getClass().getResourceAsStream(fileName));
    }

    //mark positions of variable
    void mark(String str, String var, List<part> parts) {
        int pos = 0;
        while (pos < str.length()) {
            pos = str.indexOf(var, pos);
            if (pos != -1) {
                parts.add(new part(var, pos));
                pos = pos + var.length();
            }
            else {
                break;
            }
        }
    }

    public void set(String name, String val) {
        varMap.put("$" + name + "$", val);
    }

    void parts() {
        List<part> indexes = new ArrayList<>();
        //partition
        for (String key : varMap.keySet()) {
            mark(content, key, indexes);
        }
        //sort
        Collections.sort(indexes, new Comparator<part>() {
            @Override
            public int compare(part o1, part o2) {
                return Integer.compare(o1.index, o2.index);
            }
        });
        //replace
        int pos = 0;
        for (part part : indexes) {
            String prev = content.substring(pos, part.index);
            if (!prev.isEmpty()) {
                list.add(prev);
            }
            list.add(varMap.get(part.name));
            pos = part.index + part.name.length();
        }
        list.add(content.substring(pos));
    }

    @Override
    public String toString() {
        parts();
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
