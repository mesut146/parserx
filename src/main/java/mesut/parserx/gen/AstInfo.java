package mesut.parserx.gen;

import mesut.parserx.gen.ll.Type;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type outerCls;
    public Type nodeType;
    public boolean isFactor;//no assign
    public boolean isFactored;//epsilon
    public boolean isInLoop;
    public boolean isPrimary;//recursion left
    public String factorName;
    public int which = -1;
    public boolean createNode;
    public boolean substitution;
    public String subVar;
    public boolean assign;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.outerCls = outerCls;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.isInLoop = isInLoop;
        res.which = which;
        res.createNode = createNode;
        res.nodeType = nodeType;
        res.substitution = substitution;
        res.subVar = subVar;
        res.assign = assign;
        res.factorName = factorName;
        res.isPrimary = isPrimary;
        return res;
    }

    public String writeNode() {
        if (assign) {
            return String.format("%s %s = %s.%s = new %s();", nodeType, varName, outerVar, varName, nodeType);
        }
        return String.format("%s %s = new %s();", nodeType, varName, nodeType);
    }

    public String writeWhich() {
        return String.format("%s.which = %d;", outerVar, which);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ");
        }
        sb.append(String.format("%s.%s", outerVar, varName));
        if (which != -1) {
            sb.append(" code");
        }
        sb.append('}');
        return sb.toString();
    }
}
