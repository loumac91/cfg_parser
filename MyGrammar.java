import computation.contextfreegrammar.*;
import java.util.ArrayList;
import java.util.HashSet;

public class MyGrammar {
	public static ContextFreeGrammar makeGrammar() {

    // Terminals
    HashSet<Terminal> terminals = new HashSet<>();
    Terminal plus = new Terminal('+');
    Terminal multiply = new Terminal('*');
    Terminal negate = new Terminal('−');
    Terminal one = new Terminal('1');
    Terminal zero = new Terminal('0');
    Terminal xT = new Terminal('x'); // 𝑥
    terminals.add(plus);
    terminals.add(multiply);
    terminals.add(negate);
    terminals.add(one);
    terminals.add(zero);
    terminals.add(xT);

    // Variables
    HashSet<Variable> variables = new HashSet<>();
    Variable S = new Variable('S');
    Variable S1 = new Variable("S1");
    Variable S2 = new Variable("S2");
    Variable E = new Variable('E');
    Variable E1 = new Variable("E1");
    Variable E2 = new Variable("E2");
    Variable T = new Variable('T');
    Variable T1 = new Variable('T');
    Variable F = new Variable('F');
    Variable C = new Variable('C');
    Variable P = new Variable('P');
    Variable M = new Variable('M');
    Variable N = new Variable('N');
    // Variable O = new Variable('O');
    // Variable Z = new Variable('Z');
    // Variable X = new Variable('X');
    variables.add(S);
    variables.add(S1);
    variables.add(S2);
    variables.add(E);
    variables.add(E1);
    variables.add(E2);
    variables.add(T);
    variables.add(T1);
    variables.add(F);
    variables.add(C);
    variables.add(P);
    variables.add(M);
    variables.add(N);
    // variables.add(O);
    // variables.add(Z);
    // variables.add(X);

    // Rules
    ArrayList<Rule> rules = new ArrayList<Rule>();
		rules.add(new Rule(S, new Word(E, S1)));
		rules.add(new Rule(S, new Word(T, S2)));
		rules.add(new Rule(S, new Word(N, C)));
		rules.add(new Rule(S, new Word(one)));
		rules.add(new Rule(S, new Word(zero)));
		rules.add(new Rule(S, new Word(xT)));
		rules.add(new Rule(S1, new Word(P, T)));
		rules.add(new Rule(S2, new Word(M, F)));

		rules.add(new Rule(E, new Word(E, E1)));
		rules.add(new Rule(E, new Word(T, E2)));
		rules.add(new Rule(E, new Word(N, C)));
		rules.add(new Rule(E, new Word(one)));
		rules.add(new Rule(E, new Word(zero)));
		rules.add(new Rule(E, new Word(xT)));
		rules.add(new Rule(E1, new Word(P, T)));
    rules.add(new Rule(E2, new Word(M, F)));
    
    rules.add(new Rule(T, new Word(T, T1)));
		rules.add(new Rule(T, new Word(N, C)));
		rules.add(new Rule(T, new Word(one)));
		rules.add(new Rule(T, new Word(zero)));
		rules.add(new Rule(T, new Word(xT)));
    rules.add(new Rule(T1, new Word(M, F)));
    
    rules.add(new Rule(F, new Word(N, C)));
		rules.add(new Rule(F, new Word(one)));
		rules.add(new Rule(F, new Word(zero)));
		rules.add(new Rule(F, new Word(xT)));
    
		rules.add(new Rule(C, new Word(one)));
		rules.add(new Rule(C, new Word(zero)));
		rules.add(new Rule(C, new Word(xT)));

    rules.add(new Rule(P, new Word(plus)));
		rules.add(new Rule(M, new Word(multiply)));
    rules.add(new Rule(N, new Word(negate)));
    // rules.add(new Rule(O, new Word(one)));
		// rules.add(new Rule(Z, new Word(zero)));
    // rules.add(new Rule(X, new Word(xT)));    


    return new ContextFreeGrammar(variables, terminals, rules, S);
	}
}
