sh ../run.sh -nfa2dfa -dot -in in.nfa -out out.dfa
dot -Tpng -O in-dfa.dot
dot -Tpng -O in-nfa.dot