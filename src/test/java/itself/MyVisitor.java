package itself;

public class MyVisitor extends ParserVisitor<Object, Object> {

    @Override
    public Object visitTree(Ast.tree node, Object o) {
        return super.visitTree(node, o);
    }
}
