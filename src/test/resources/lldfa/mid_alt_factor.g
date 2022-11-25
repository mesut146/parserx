include "../common.g"

//broken, bc second time entering rule sees prev suffix and decides wrong alt bcmid.g we cant eat from end
F: a F b | a F c | d;
//F_decide: d #3 | a (a)* d F_3?;
//F_3: c F_3? | b F_3?;
