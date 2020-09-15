package nodes;

import java.util.Objects;

//right side
//can refer to rule or token
public class NameNode extends Node {

    public String name;
    public boolean isToken;//if we reference to a token

    public NameNode(String name) {
        this.name = name;
    }

    public NameNode(String name, boolean isToken) {
        this.name = name;
        this.isToken = isToken;
    }

    @Override
    public String toString() {
        if (isToken) {
            return "{" + name + "}";
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameNode nameNode = (NameNode) o;
        return isToken == nameNode.isToken &&
                Objects.equals(name, nameNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isToken);
    }
}
