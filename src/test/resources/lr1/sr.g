token{
 a: "a";
 b: "b";
 c: "c";
 d: "d";
}

%start: E;

//b causes shift/reduce error in LrUtils.epsilon
E: a? b c | b d;

//makes this
//E: a_opt b c | b d;
//a_opt: a | %empty;

//but should be this
//E: a b c | b c | b d;