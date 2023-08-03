token{
 a: "a" -> mode1;
 b: "b";

 mode1{
   b: "c" -> DEFAULT;
   c: "cd";
 }
}

/*mode DEFAULT = 0;
mode mode1 = 1;
final = 2(b), 3(c), 4(a), 5(b)

0 -> 4  , a
0 -> 5  , b
1 -> 2  , c
2 -> 3  , d*/