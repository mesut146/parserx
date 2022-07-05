include "../common.g"

E: A x | a* y;
//A: a A b | c;
A: a a A b c | d;


/*
A: a{n} c b{n}
E: a{n} (A(a{n}) x | a{n}() y) | A_no_a x | y
A(a{n}): a{n}() c b{n}

S0(){
  if(a){
    v0=new E
    v1=new E1
    v2=new A1
    v2_stack=new Stack<A1>
    S3(v1, v2_stack)
  }
}

S3(E1 p0, Stack<A1> p1){
  if(a){
    v0=new A1
    p1.push(v0)
    S3(p0, p1)
  }
  else if(c){
    v0=new A2
    S5(p0, p1, v0)
  }
}

S5(E1 p0, Stack<A1> p1, A2 p2){
  if(b){
    p2.holder.a2=p2
  }
  if(b){
    p1.peek().b=la
    S6(p0, p1)
  }
}

S6(E1 p0, Stack<A1> p1){
  if(b | x){
    tmp=p1.pop
    tmp.holder.a1=tmp
  }
  if(b){
    p1.peek().b=la
    S6(p0, p1)
  }
  else if(x){
    p0.holder.e1=p0
  }
}

*/