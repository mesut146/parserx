package mesut.parserx.gen.ll;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type outerCls;
    public boolean isFactor;//no assign
    public boolean isFactored;//epsilon
    public boolean isFactorGroup;//follows a factor
    public boolean isInLoop;
    public boolean isPrimary;//recursion left
    public String code;
    public String factorName;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.outerCls = outerCls;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.isInLoop = isInLoop;
        res.code = code;
        res.factorName = factorName;
        res.isPrimary = isPrimary;
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ");
        }
        sb.append(String.format("%s.%s", outerVar, varName));
        if (code != null) {
            sb.append(" code");
        }
        sb.append('}');
        return sb.toString();
    }
}
