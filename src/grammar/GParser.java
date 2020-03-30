/* GParser.java */
/* Generated By:JavaCC: Do not edit this line. GParser.java */
package grammar; 

import nodes.*;
import rule.*;
import java.util.*;

public class GParser implements GParserConstants {

  final public Tree tree() throws ParseException {Tree tree=new Tree();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case TOKEN_:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      tokenDecl(tree);
    }
    label_2:
    while (true) {
      ruleDecl(tree);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENT:{
        ;
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
    }
{if ("" != null) return tree;}
    throw new Error("Missing return statement in function");
}

  final public void tokenDecl(Tree tree) throws ParseException {String name;
    jj_consume_token(TOKEN_);
    label_3:
    while (true) {
      name = name();
tree.add(new TokenDecl(name));
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENT:{
        ;
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
    }
    jj_consume_token(SEMI);
}

  final public void ruleDecl(Tree tree) throws ParseException {RuleDecl decl=new RuleDecl();
  String name;
  Rule rhs;
    name = name();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case EQ:{
      jj_consume_token(EQ);
      break;
      }
    case COLON:{
      jj_consume_token(COLON);
      break;
      }
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    rhs = rhs();
    jj_consume_token(SEMI);
tree.add(decl);
  decl.rhs=rhs;
}

//or series
  final public Rule rhs() throws ParseException {Rule rule;
  OrRule or=new OrRule();
  boolean more=false;
    rule = rhs_list();
or.add(rule);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 29:{
        ;
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        break label_4;
      }
      jj_consume_token(29);
      rule = rhs_list();
or.add(rule);
   more=true;
    }
{if ("" != null) return more?or:rule;}
    throw new Error("Missing return statement in function");
}

//sequence no or
  final public Rule rhs_list() throws ParseException {Sequence s=new Sequence();
  Rule r;
    label_5:
    while (true) {
      r = regex();
s.add(r);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case LPAREN:
      case IDENT:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_5;
      }
    }
{if ("" != null) return s.normal();}
    throw new Error("Missing return statement in function");
}

  final public Rule regex() throws ParseException {Rule rule;
    rule = simple();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case STAR:
    case PLUS:
    case QUES:{
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case STAR:{
        jj_consume_token(STAR);
{if ("" != null) return new StarRule(rule);}
        break;
        }
      case PLUS:{
        jj_consume_token(PLUS);
{if ("" != null) return new PlusRule(rule);}
        break;
        }
      case QUES:{
        jj_consume_token(QUES);
{if ("" != null) return new OptionalRule(rule);}
        break;
        }
      default:
        jj_la1[6] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
      }
    default:
      jj_la1[7] = jj_gen;
      ;
    }
{if ("" != null) return rule;}
    throw new Error("Missing return statement in function");
}

  final public Rule simple() throws ParseException {Rule rule;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case LPAREN:{
      rule = groupRule();
      break;
      }
    case IDENT:{
      rule = nameRule();
      break;
      }
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return rule;}
    throw new Error("Missing return statement in function");
}

  final public Rule groupRule() throws ParseException {GroupRule group=new GroupRule();
  Rule rule;
    jj_consume_token(LPAREN);
    rule = rhs();
    jj_consume_token(RPAREN);
group.rhs=rule;
    {if ("" != null) return group;}
    throw new Error("Missing return statement in function");
}

  final public Rule nameRule() throws ParseException {String name;
    name = name();
{if ("" != null) return new RuleRef(name);}
    throw new Error("Missing return statement in function");
}

  final public String name() throws ParseException {Token token;
    token = jj_consume_token(IDENT);
{if ("" != null) return token.image;}
    throw new Error("Missing return statement in function");
}

  /** Generated Token Manager. */
  public GParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[9];
  static private int[] jj_la1_0;
  static {
	   jj_la1_init_0();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x4000000,0x8000000,0x8000000,0x480000,0x20000000,0x8001000,0x3800000,0x3800000,0x8001000,};
	}

  /** Constructor with InputStream. */
  public GParser(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public GParser(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new GParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public GParser(java.io.Reader stream) {
	 jj_input_stream = new JavaCharStream(stream, 1, 1);
	 token_source = new GParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new JavaCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new GParserTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public GParser(GParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(GParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
	 Token oldToken;
	 if ((oldToken = token).next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 if (token.kind == kind) {
	   jj_gen++;
	   return token;
	 }
	 token = oldToken;
	 jj_kind = kind;
	 throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[30];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 9; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 30; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
