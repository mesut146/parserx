package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.ll.Type;

import java.util.Set;

public class Variable {
    public Set<Item> children;//children of holder
    public Type type;
    public String name;
    public Item item;
    public Variable holder;
    public boolean isArray;

    //alt or normal
    public Variable(Type type, String name, Item item) {
        this.type = type;
        this.name = name;
        this.item = item;
    }

    //alt
    public Variable(Type type, String name, Item item, Variable holder) {
        this.type = type;
        this.name = name;
        this.item = item;
        this.holder = holder;
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

    public boolean isHolder() {
        return item == null;
    }
}
