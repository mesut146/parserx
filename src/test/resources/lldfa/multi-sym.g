include "../common.g"

E: a x | a y | A B* c?;
A: z;
B: p | t;
//E-> z | a (x | y)

F: a? b? x? | b y;
//F-> x | b (y | x)? | a