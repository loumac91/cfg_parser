import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;
import computation.derivation.*;

public class Parser implements IParser {
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {

    int length = w.length();

    // derivations of x+x
    // S => ES1 => xE1 => xPT => xPT => x+T => x+x
    Iterator<Symbol> s = w.iterator();
    Variable startVariable = cfg.getStartVariable();
    List<Rule> rules = cfg.getRules();

    ArrayList<Rule> startingVariableRules = rules
      .stream()
      .filter(rule -> rule.getVariable().equals(startVariable))
      .collect(Collectors.toCollection(ArrayList::new));

    for (Rule startingRule : startingVariableRules) {
      Iterator<Symbol> ws = startingRule.getExpansion().iterator();
      while (ws.hasNext()) {

      }
    }

    while (s.hasNext()) {
      Symbol symbol = s.next();
      if (symbol == null || symbol.isTerminal())
      cfg.getRules().get(0);
      System.out.println(symbol);
    }

    return false;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}