include "../common.g"

%start: E;

//E: a? b+;
//E: a* x;
//E: a+ x;
E: a? b* c+;
