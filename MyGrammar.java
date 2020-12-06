import computation.contextfreegrammar.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class MyGrammar {
	public static ContextFreeGrammar makeGrammar() {

    // Terminals - NOTE, some of these seem to differ slightly to data page
    Terminal plus = new Terminal('+'); // +
    Terminal multiply = new Terminal('*'); // ∗
    Terminal negate = new Terminal('-'); // −
    Terminal one = new Terminal('1');
    Terminal zero = new Terminal('0');
    Terminal xT = new Terminal('x'); // 𝑥

    HashSet<Terminal> terminals = new HashSet<>(Arrays.asList(
      plus,
      multiply,
      negate,
      one,
      zero,
      xT
    ));

    // Variables
    Variable S = new Variable('S');
    Variable E = new Variable('E');
    Variable T = new Variable('T');
    Variable F = new Variable('F');
    Variable H = new Variable('H');
    Variable G = new Variable('G');
    Variable C = new Variable('C');
    Variable P = new Variable('P');
    Variable M = new Variable('M');
    Variable N = new Variable('N');

    HashSet<Variable> variables = new HashSet<>(Arrays.asList(
      S,
      E,
      T,
      F,
      G,
      H,
      C,
      P,
      M,
      N
    ));

    // Rules
    ArrayList<Rule> rules = new ArrayList<Rule>(Arrays.asList(
   		new Rule(S, new Word(E, G)),
      new Rule(S, new Word(T, H)),
      new Rule(S, new Word(N, C)),
      new Rule(S, new Word(one)),
      new Rule(S, new Word(zero)),
      new Rule(S, new Word(xT)),
      // new Rule(S, Word.emptyWord),
      new Rule(E, new Word(E, G)),
      new Rule(E, new Word(T, H)),
      new Rule(E, new Word(N, C)),
      new Rule(E, new Word(one)),
      new Rule(E, new Word(zero)),
      new Rule(E, new Word(xT)),
      new Rule(T, new Word(T, H)),
      new Rule(T, new Word(N, C)),
      new Rule(T, new Word(one)),
      new Rule(T, new Word(zero)),
      new Rule(T, new Word(xT)),
      new Rule(F, new Word(N, C)),
      new Rule(F, new Word(one)),
      new Rule(F, new Word(zero)),
      new Rule(F, new Word(xT)),
      new Rule(G, new Word(P, T)),
      new Rule(H, new Word(M, F)),
      new Rule(C, new Word(one)),
      new Rule(C, new Word(zero)),
      new Rule(C, new Word(xT)),
      new Rule(P, new Word(plus)),
      new Rule(M, new Word(multiply)),
      new Rule(N, new Word(negate))   
    ));

    return new ContextFreeGrammar(variables, terminals, rules, S);
  }
  
  public static ContextFreeGrammar makeGrammar2() {
    
    //Variables
    Variable T = new Variable('T');
    Variable F = new Variable('F');
    Variable E = new Variable('E');
    Variable C = new Variable('C');
    Variable H0 = new Variable("H0");
    Variable H1 = new Variable("H1");
    Variable H2 = new Variable("H2");
    Variable H3 = new Variable("H3");
    Variable H4 = new Variable("H4");
    Variable H5 = new Variable("H5");
    //Variables to a Set
    Set<Variable> variables = new HashSet<>();
    variables.add(T);
    variables.add(F);
    variables.add(E);
    variables.add(C);
    variables.add(H0);
    variables.add(H1);
    variables.add(H2);
    variables.add(H3);
    variables.add(H4);
    variables.add(H5);
    //Terminals
    Terminal one = new Terminal('1');
		Terminal zero = new Terminal('0');
		Terminal x = new Terminal('x');
    Terminal plus = new Terminal('+');
		Terminal minus = new Terminal('-');
    Terminal times = new Terminal('*');
		
    //Terminals to a Set
		Set<Terminal> terminals = new HashSet<>();
		terminals.add(one);
		terminals.add(zero);
		terminals.add(x);
    terminals.add(plus);
		terminals.add(minus);
		terminals.add(times);
    
    //ArrayList to store rules
    List<Rule> rules = new ArrayList<>();
	
    //Add Rules for Variable 'H0'
    rules.add(new Rule(H0,new Word(H1,T)));
		rules.add(new Rule(H0,new Word(H2,F)));
		rules.add(new Rule(H0,new Word(H3,C)));
		rules.add(new Rule(H0,new Word(one)));
		rules.add(new Rule(H0,new Word(zero)));
		rules.add(new Rule(H0,new Word(x)));
    
    //Add Rules for Variable 'E'
    rules.add(new Rule(E,new Word(H1,T)));
		rules.add(new Rule(E,new Word(H2,F)));
		rules.add(new Rule(E,new Word(H3,C)));
		rules.add(new Rule(E,new Word(one)));
		rules.add(new Rule(E,new Word(zero)));
		rules.add(new Rule(E,new Word(x)));
    //Add Rules for Variable 'T'
	  rules.add(new Rule(T,new Word(H2,F)));
		rules.add(new Rule(T,new Word(H3,C)));
		rules.add(new Rule(T,new Word(one)));
		rules.add(new Rule(T,new Word(zero)));
		rules.add(new Rule(T,new Word(x)));
    
    //Add Rules for Variable 'F'
    rules.add(new Rule(F,new Word(H3,C)));
		rules.add(new Rule(F,new Word(one)));
		rules.add(new Rule(F,new Word(zero)));
		rules.add(new Rule(F,new Word(x)));
    
    //Add Rules for Variable 'C'
    rules.add(new Rule(C,new Word(one)));
		rules.add(new Rule(C,new Word(zero)));
		rules.add(new Rule(C,new Word(x)));
    //Add Rule for Variable 'H1'
 		rules.add(new Rule(H1,new Word(E,H4)));
		
    //Add Rule for Variable 'H2'
 		rules.add(new Rule(H2,new Word(T,H5)));
    //Add Rule for Variable 'H3'
 		rules.add(new Rule(H3,new Word(minus)));
    //Add Rule for Variable 'H4'
 		rules.add(new Rule(H4,new Word(plus)));
    //Add Rule for Variable 'H5'
 		rules.add(new Rule(H5,new Word(times)));
    //Instantiate my ContextFreeGrammar cfg and return it
    ContextFreeGrammar cfg = new ContextFreeGrammar(variables,terminals,rules,H0);
    return cfg;
  }
}
