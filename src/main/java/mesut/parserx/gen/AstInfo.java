package mesut.parserx.gen;

import mesut.parserx.gen.lldfa.Type;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type nodeType;
    public boolean isFactor;//no assign
    public boolean isFactored;//epsilon
    public boolean isInLoop;
    public boolean isPrimary;
    public int which = -1;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.nodeType = nodeType;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.isInLoop = isInLoop;
        res.which = which;
        res.isPrimary = isPrimary;
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ").append(varName);
        }
        else {
            if (isFactored) {
                sb.append("factored ");
            }
            sb.append(String.format("%s.%s", outerVar, varName));
        }
        if (which != -1) {
            sb.append(" #").append(which);
        }
        sb.append('}');
        return sb.toString();
    }
}
