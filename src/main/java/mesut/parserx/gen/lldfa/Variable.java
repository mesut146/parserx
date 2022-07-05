package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.ll.Type;

import java.util.Set;

public class Variable {
    public Set<Item> children;//children of holder
    Type type;
    String name;
    Item item;
    public boolean isArray;

    //alt or normal
    public Variable(Type type, String name, Item item) {
        this.type = type;
        this.name = name;
        this.item = item;
    }

    //holder
    public Variable(Type type, String name, Set<Item> children) {
        this.type = type;
        this.name = name;
        this.children = children;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
