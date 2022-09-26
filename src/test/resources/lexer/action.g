token{
  A: "ab" @A -> skip;
  B: "ac" @B;
}

action{
  A: %begin System.out.println("found A"); %end
  B: %begin System.out.println("found B"); %end
}

//E: A;
