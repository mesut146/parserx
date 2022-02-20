include "../common.g"

%start = E;

E: A d | a* e;
A: a A b | c;