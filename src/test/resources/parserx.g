tree::= includeStatement*
        (tokenBlock  | skipBlock)*
        startDecl?
        ruleDecl*;

includeStatement::= "include" <STRING_LITERAL>;

tokenBlock::= "token" "{" tokenDecl* "}";
skipBlock::= "skip" "{" tokenDecl* "}";

declSeparator::= ":" | "=" | ":=" | ":==" | "->";
tokenDecl::= "#"? name declSeparator rhs;
ruleDecl::= name declSeparator rhs;

rhs::= sequence ("|" sequence)*;
sequence::= regex+;
regex::= simple ("*" | "+" | "?")?
simple::= group | ref | stringNode | bracketNode | untilNode | dotNode;

group::= "(" rhs ")";
stringNode::= <STRING_LITERAL>
bracketNode::= <BRACKET_LIST>//easier to handle as token
untilNode::= "~" regex;
dotNode::= "."

startDecl::= "@start" "=" name;



ref::= lexerRef | name;
lexerRef::= "{" name "}";
name::= <IDENT>;