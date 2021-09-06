package mesut.parserx.gen.ll;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type outerCls;
    public boolean isFactor;//no assign
    public boolean isFactored;//epsilon
    public boolean isGroup;
    public boolean isFactorGroup;//follows a factor
    public boolean isInLoop;
    public String code;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.outerCls = outerCls;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.isInLoop = isInLoop;
        res.code = code;
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ");
        }
        sb.append(String.format("var=%s cls=%s v2=%s", varName, outerCls, outerVar));
        if (code != null) {
            sb.append(" code=").append(code.replace("\n", "\\n"));
        }
        sb.append('}');
        return sb.toString();
    }
}
