include "../common.g"

%start = B;

A: A1 A2*;
A3: A5 A2*;
A4: A6 A2*;
A1: B1 b | c;
A5: c;
A6: B4 b;
A2: B2 b;
B: d (A4 a | %epsilon) | A3 a;
B1: d;
B4: %epsilon;
B2: a;


