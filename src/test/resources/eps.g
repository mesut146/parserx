include "common.g"

A1: a b? c;
A2: a B1 c;
A3: a* b;
A4: B2 b;
A5: B1 B1 c;
A6: (a | b*)? c;

B1: b?;
B2: b*;

C: a? b? D?;
D: d? e*;