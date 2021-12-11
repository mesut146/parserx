package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;

public class FirstSetNode extends BaseVisitor<FirstSetNode.SymbolNode, Void> {
    public HashMap<Name, SymbolNode> map = new HashMap<>();
    Tree tree;

    public FirstSetNode(Tree tree) {
        this.tree = tree;
    }

    public static SymbolNode first(Node node, Tree tree) {
        FirstSetNode firstSetNode = new FirstSetNode(tree);
        return node.accept(firstSetNode, null);
    }

    @Override
    public SymbolNode visitGroup(Group group, Void arg) {
        return group.node.accept(this, arg);
    }

    @Override
    public SymbolNode visitOr(Or or, Void arg) {
        SymbolNode node = new SymbolNode(null);
        for (Node ch : or) {
            SymbolNode s = ch.accept(this, arg);
            if (s != null) {
                node.list.add(s);
            }
        }
        return node;
    }

    @Override
    public SymbolNode visitName(Name name, Void arg) {
        if (name.astInfo.isFactored) return null;
        if (name.isRule()) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
            SymbolNode node = new SymbolNode(name);
            map.put(name, node);
            SymbolNode s = tree.getRule(name).rhs.accept(this, arg);
            if (s.name != null) {
                node.list.add(s);
            }
            else {
                node.list = s.list;
            }
            return node;
        }
        else {
            return new SymbolNode(name);
        }
    }

    @Override
    public SymbolNode visitSequence(Sequence seq, Void arg) {
        SymbolNode res = null;
        for (Node ch : seq) {
            SymbolNode s = ch.accept(this, arg);
            if (res == null) {
                res = s;
            }
            else {
                res.list.add(s);
            }
            if (!FirstSet.canBeEmpty(ch, tree)) {
                break;
            }
        }
        return res;
    }

    @Override
    public SymbolNode visitRegex(Regex regex, Void arg) {
        if (regex.astInfo.isFactored) return null;
        return regex.node.accept(this, arg);
    }

    public static class SymbolNode {
        static Set<String> printCache = new HashSet<>();
        public Name name;
        public List<SymbolNode> list = new ArrayList<>();

        public SymbolNode(Name name) {
            this.name = name;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (name != null) {
                sb.append(name.name);
                printCache.add(name.name);
            }
            if (list.isEmpty()) {
                return sb.toString();
            }
            if (name != null && printCache.contains(name.name)) {
                sb.append("{$}");
                return sb.toString();
            }
            sb.append("{");
            boolean first = true;
            for (SymbolNode ch : list) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(ch);
            }
            sb.append("}");
            return sb.toString();
        }
    }

}
