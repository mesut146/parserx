statement = varDecl |
            exprStmt |
            ifStatement|
            whileStatement |
            doWhileStatement |
            forStatement |
            forEachStatement|
            block |
            throwStatement|
            ";";


block = "{" statement* "}";

ifStatement = "if" "(" expr ")" statement ("else" statement)?;

whileStatement = "while" "(" expr ")" statement;

doWhileStatement = "do" block "while" "(" expr ")" ";";

forStatement = "for" "(" forInits? ";" expr? ";" updaters? ")" statement;
forInits = TypeName varFrags;
updaters = expr ("," expr)*;

forEachStatement = "for" "(" "final"? Type IDENT ":" expr ")" statement;

exprStmt = expr ";";

tryStatement = "try" block (catchStatement | finallyStatement)*;
tryResourcesStatement = "try" "(" varDecl ")" block;
catchStatement = "catch" "(" "final" ReferenceType IDENT ")" block;
finallyStatement = "finally" block;

throwStatement = "throw" expr;