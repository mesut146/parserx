token{
a: "a";
b: "b";
}

@start -> S;
S -> B b b | a a b | b B a;
B -> a;