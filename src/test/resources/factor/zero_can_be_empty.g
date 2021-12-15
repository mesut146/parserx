include "../common.g"

E: A+ b | a+ c;
A: a d?;

/*
E: (a+ (A_a_eps(a)+() (a A_a_noe(a) A*)? b | a+() c)) | (a A_a_noe(a) A* b);
E: a ((a() a* (A_a_eps(a)+() (a A_a_noe(a) A*)? b | a+() c)) | (a() A_a_noe(a) A* b));

A: a d?;
A_a(a): a() d?;
A_a_eps(a): a() ε;
A_a_noe(a): a() d;
*/