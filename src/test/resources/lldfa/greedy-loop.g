include "../common.g"

E: a* a b | a x;
F: a* a a a b | a x;

E1: E2* a b | a x;
E2: a;

A1: B* a c | a x;
A2: B* a b d | a x;
B: a b; 