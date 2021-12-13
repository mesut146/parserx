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
    public boolean isPrimary;//recursion lhs
    public boolean isSecondary;//recursion rhs
    public String factorName;
    public int which = -1;
    public boolean createNode;
    public boolean substitution;
    public String subVar;
    public boolean assignOuter;
    public AstInfo loopExtra;
    public AstInfo loopBound;
    public AstInfo factor;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.outerCls = outerCls;
        res.nodeType = nodeType;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.isInLoop = isInLoop;
        res.which = which;
        res.createNode = createNode;
        res.substitution = substitution;
        res.subVar = subVar;
        res.assignOuter = assignOuter;
        res.factorName = factorName;
        res.isPrimary = isPrimary;
        res.isSecondary = isSecondary;
        res.loopExtra = loopExtra;
        res.loopBound = loopBound;
        res.factor = factor;
        return res;
    }

    public String writeNode() {
        if (assignOuter) {
            return String.format("%s %s = %s.%s = new %s();", nodeType, varName, outerVar, varName, nodeType);
        }
        return String.format("%s %s = new %s();", nodeType, varName, nodeType);
    }

    public String writeNodeCpp() {
        if (assignOuter) {
            return String.format("%s* %s = new %s();\n%s->%s = %s;", nodeType.cpp(), varName, nodeType.cpp(), outerVar, varName, varName);
        }
        return String.format("%s* %s;", nodeType.cpp(), varName);
    }

    public String writeWhich() {
        return String.format("%s.which = %d;", outerVar, which);
    }

    public String writeWhichCpp() {
        return String.format("%s->which = %d;", outerVar, which);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ").append(factorName);
        }
        else {
            sb.append(String.format("%s.%s", outerVar, varName));
            if (isFactored) {
                sb.append("=").append(factorName);
            }
        }
        if (which != -1) {
            sb.append(" which");
        }
        sb.append('}');
        return sb.toString();
    }

    public void from(AstInfo other) {
        if (other.which != -1) {
            which = other.which;
            varName = other.varName;
            outerVar = other.outerVar;
            outerCls = other.outerCls;
            nodeType = other.nodeType;
            assignOuter = other.assignOuter;
            createNode = other.createNode;
        }
    }
}
