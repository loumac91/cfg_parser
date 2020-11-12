import computation.contextfreegrammar.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class MyGrammar {
	public static ContextFreeGrammar makeGrammar() {

    // Terminals
    Terminal plus = new Terminal('+');
    Terminal multiply = new Terminal('*');
    Terminal negate = new Terminal('−');
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
}
