# parserx
[![](https://jitpack.io/v/mesut146/parserx.svg)](https://jitpack.io/#mesut146/parserx)

lexer &amp; parser generator and grammar toolkit written in java

## Features
- accepts regex like grammar(EBNF)
- lexer generator
- Recursive descent parser generator that supports left recursion
- LR(1),LALR(1) parser generator
- DFA minimization
- Outputs CST
- dot graph of NFA, DFA, LR(1), LALR(1)

## Transformations 
- left recursion remover(direct and indirect)
- precedence remover
- ebnf to bnf
- epsilon remover

***Examples are in examples folder***


## Grammar Format

### comments

`//this is a line comment`

```
/* this is a
multine comment */
```

### top level
to include another grammar use;<br>

`include "<grammar_name>"`

e.g `include "lexer.g"`

### token definitions

```
token{
  <TOKEN_NAME> : <regex> ;
}
```
e.g
```
token{
  #LETTER: [a-zA-Z]
  #DIGIT: [0-9];
  NUMBER: DIGIT+;
  IDENT: (LETTER | '_') (LETTER | DIGIT | '_')*;
}
```

__prefixing token name with '#' makes that token fragment so that it can be used as only reference__

### rule definitions

```
<RULE_NAME> : <regex> ;
```
e.g
```
assign: left "=" right;
left: IDENT;
right: IDENT | LITERAL;
```

### regex types

#### alternation
`r1 | r2 | r3`

#### sequence
`r1 r2 r3`

#### repetition
`r*` = zero or more times(kleene star)<br>
`r+` = one or more times(kleene plus><br>
`r?` = zero or one time(optional)<br>

#### grouping
`(r)` you can group complex regexes in tokens and rules<br>
e.g `a (b | c+)`

#### epsilon
use `%empty`, `%epsilon` or `ε` for epsilon<br>
e.g `rule: a (b | c | %epsilon);`

#### ranges (token only)
place ranges or single chars inside brackets(without quote)<br>
`[start-end single]`

e.g `id: [a-zA-Z0-9_];`

escape sequences also supported<br>
e.g `ws: [\u00A0\u000A\t];`

negation e.g `lc: "//" [^\n]*;`

#### strings

use single or double quotes for your strings<br>
e.g `stmt: "if" "(" expr ")" stmt;`

e.g `stmt: 'if' '(' expr ')' stmt;`

strings in rules will be replaced with token references that are declared in `token` block<br>
so in the example above the strings would need to be declared like;<br>
```
token{
  IF: "if";
  LP: "(";
  RP: ")";
}
```

#### start directive

in LR parsing you have to specify start rule with `%start`<br>
e.g `%start: expr;`

#### assoc directives
use `%left` or `%right` to specify associativity
```
E: E "*" E %left | E "+" E %right | NUM;
```

#### precedence
precedence handled by picking the alternation declared before
e.g `E: E "*" E | E "+" E | NUM;`
<br>multiplication takes precedence over addition in the example


### modes
you can use modes to create more complex lexer

```
token{
 LT: "<" -> attr;
 attr{
   TAG_NAME: [:ident:] -> attr;
 }
 attr{
   WS: [\r\n\t ] -> skip;
   GT: ">" -> DEFAULT;
   SLASH_GT: "/>" -> DEFAULT;
   ATTR_NAME: [:ident:] -> eq;
 }
 attr_eq{
   EQ: "=" -> attr_val;
 }
 attr_val{
   VAL: [:string:] -> attr;
 }
}
```
*note:* default mode is used to exit from modes
#### skip mode
tokens marked with skip mode will be ignored by the parser so you can use it for comments and whitespaces
```
token{
  comment: "//" [^\n]* -> skip;
  ws: [ \r\n\t]+;
}
```
