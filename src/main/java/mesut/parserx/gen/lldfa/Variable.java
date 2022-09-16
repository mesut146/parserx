package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.ll.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Variable {
    public List<Item> children;
    public Type type;
    public String name;
    public Item item;
    public Variable holder;
    public List<Variable> prevs = new ArrayList<>();//this is param

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
    public Variable(Type type, String name, List<Item> children) {
        this.type = type;
        this.name = name;
        this.children = children;
        this.children.sort(Comparator.comparingInt(it -> it.rule.which));
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    public boolean isHolder() {
        return item == null;
    }
}
