import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;

public class Parser implements IParser {

  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {

    // On an input w for Grammar G
    // List all derivations with 2n - 1 steps where n = |w|, unless n = 0, then list
    // all derivatins with 1 step
    // If any of these derivations generate w, then accept. Otherwise reject

    // Algorithm #1
    // Iterate through each set of expansions, computing all possible derivations
    // i = 0, [ S ]
    // i = 1, [ EG | TH | NC | 1 | 0 | x ]
    // i = 2, [ EEG, EPT | TTH, TMF | -C, N1, N0, Nx ]
    // i = 3, [ EGEG, EEGG, EEPT ... etc ]

    List<Rule> rules = cfg.getRules();
    Map<Symbol, List<Rule>> ruleMap = rules
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable));

    // Ignore whitespace
    Word cleaned = new Word(w.toString().replaceAll("\\s",""));
    int n = cleaned.length();
    int derivations = 2 * n - 1;

    Variable current = cfg.getStartVariable();
    LinkedList<Word> previousExpansions = new LinkedList<>(){{
      push(new Word(current.toString())); 
    }};

    int i = 1;
    while (i <= derivations) { // On my computer, things really start to grind to a halt after the 10th iteration, and we start running into memory issues
      LinkedList<Word> currentExpansions = new LinkedList<>();
      // Iterate through each previous expansions
      while (true) {
        Word word = previousExpansions.peek();
        if (word == null) break;

        word = previousExpansions.pop();
        // If the whole derivation is a word, then go to next derivation
        if (word.isTerminal()) {
          continue;
        }

        // Iterate through each symbol of the derivation
        int replaceIndex = 0;
        Iterator<Symbol> symbols = word.iterator();
        while (symbols.hasNext()) { 
          Symbol currentSymbol = symbols.next();
          // If the symbol is a terminal, then skip
          if (currentSymbol.isTerminal()) {
            replaceIndex++;
            continue;
          }

          // Get all the expansions for the symbol and apply them
          List<Rule> currentRules = ruleMap.getOrDefault(currentSymbol, new ArrayList<Rule>());
          for (Rule r : currentRules) {
            Word replaced = word.replace(replaceIndex, r.getExpansion());
            if (replaced.equals(cleaned)) return true; // If we get out result, return early
            currentExpansions.push(replaced);
          }

          replaceIndex++;
        }
      }

      previousExpansions = currentExpansions;
      i++;
    }

    return false;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}