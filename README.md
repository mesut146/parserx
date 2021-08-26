# parserx

lexer &amp; parser generator and grammar toolkit written in java

## Features

- accepts regex like grammar(EBNF)
- epsilon removal
- left recursion removal(direct and indirect)
- left factoring
- ebnf to bnf and vice versa
- LR(0),LR(1),LALR(1) parser generator
- LL(1) recursive descent parser generator
- dot graph of NFA, DFA, LR(0), LR(1), LALR(1)
- DFA minimization
- lexer generator
- precedence tool(removes any precedence conflict)

Examples are in examples folder


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

  <TOKEN_NAME> <seperator> <regex> <SEMICOLON>
  //where seperator is one of ':' , '=' , '::=' , ':=' , '->'

}
```
e.g
```
token{
  NUMBER: [0-9]+;
  IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
}
```

__prefixing token name with '#' makes that token fragment.So that it can be used as reference but no actual dfa generated for it__

### rule definitions

```
<RULE_NAME> <seperator> <regex> <SEMICOLON>
```
e.g
```
assign: left "=" right;
left: ident;
right: ident | literal;
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
use `%empty` or `ε` for epsilon<br>
e.g `rule: a (b | c | %empty);`

#### ranges (token only)
place ranges or single chars inside brackets(without quote)<br>
`[start-end single]`

e.g `id: [a-zA-Z0-9_];`

escape sequences also supported<br>
e.g `ws: [\u00A0\u000A\t];`

negation e.g `lc: "//" [^\n]*;`

#### strings

use double quotes for your strings<br>
e.g `stmt: "if" "(" expr ")" stmt;`

strings in rules will be replaced with token references that are declared in `token` block<br>
so in the example above the strings would be declared like;<br>
```
token{
  IF: "if";
  LP: "(";
  RP: ")";
}
```

#### start directive

in LR parsing you have to specify start rule with `@start`<br>
e.g `@start: expr;`

### skip block

skip tokens will be ignored by the parser so you can use it for comments and whitespaces 

```
skip{
  comment: "//" [^\n]*;
}
```