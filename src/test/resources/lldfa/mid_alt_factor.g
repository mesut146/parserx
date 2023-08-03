include "../common.g"

//broken, bc second time entering rule sees prev suffix and decides wrong alt bc we cant eat from end
//we need regex consumer to solve this
F: a F b | a F c | d;
//F_decide: d #3 | a (a)* d F_3?;
//F_3: c F_3? | b F_3?;
