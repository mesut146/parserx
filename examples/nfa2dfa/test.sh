sh ../run.sh -nfa2dfa -dot dfa.dot -in in.nfa -out out.dfa
dot -Tpng -O dfa.dot
dot -Tpng -O in.nfa-nfa.dot