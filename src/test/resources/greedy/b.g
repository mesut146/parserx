include "../common.g"

E: A a;
A: c B?;
B: a b;

/*
E: c A(c) a;
E: c a A(c,a) a | c A_c_no_a(c) a;
E: c a (c(c) a(a) A(c,a) a | c(c) A_c_no_a(c) a(a));
A(c,a): c(c) B(a);
A_c_no_a(c): c(c) €;

E: c (c(c) a c(c) a(a) b a | c(c) c(c) ε a);
E: c (a b a | a)
E: c a (b a | €)
*/