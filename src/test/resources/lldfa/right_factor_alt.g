include "../common.g"

E: a E | a x | y;
//E_decide: y #3 | a (a)* (y #3 | x #2);

F: a b F | a b x | a b y;