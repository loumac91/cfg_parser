import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
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
    // i = 2, [ EEG, THG, NCG, 1G, 0G, xG, EPT, TTH, NCH, 1H, 0H, 0x ... etc ]

    // 1. Group all the expansions under each symbol ".get(symbol)" all given expansions
    List<Rule> rules = cfg.getRules();
    Map<Symbol, List<Word>> expansionsMap = rules
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));
    
    // 2. Construct set of derivations to compute - starting with grammars starting variable
    HashSet<Word> previousDerivations = new HashSet<>(){{
      add(new Word(cfg.getStartVariable()));
    }};

    // 3. Compute number of derivations
    int i = 1;
    int n = w.length();
    int derivations = 2 * n - 1;

    try {
      // 4. Attempt to iterate up to 2 * n - 1 derivations
      while (i <= derivations) {
        HashSet<Word> currentDerivations = new HashSet<>();
        // 5. Iterate through each previous expansions (skip terminals)
        for (Word derivation : previousDerivations) {
          if (derivation.isTerminal()) {
            continue;
          }
  
          // 6. Iterate through each symbol of the derivation (skip terminals)
          int replaceIndex = 0;
          Iterator<Symbol> symbols = derivation.iterator();
          while (symbols.hasNext()) { 
            Symbol currentSymbol = symbols.next();
            if (currentSymbol.isTerminal()) {
              replaceIndex++;
              continue;
            }
  
            // 7. Get all the expansions for the symbol and replace each expansion in the current derivation
            // - If this produces the word we are looking for, return true
            // - else add the product to a collection of derivations that represent i + 1
            List<Word> currentExpansions = expansionsMap.getOrDefault(currentSymbol, new ArrayList<Word>());
            for (Word expansion : currentExpansions) {
              Word replaced = derivation.replace(replaceIndex, expansion);
              if (replaced.equals(w)) {
                return true;
              }

              currentDerivations.add(replaced);
            }
  
            replaceIndex++;
          }
        }
  
        // 8. Copy all produced derivations to previous derivations collections to compute on next loop
        previousDerivations = currentDerivations;
        i++;
      }
    } catch (OutOfMemoryError e) {
      // On my computer, I would usually hit the ~14th iteration before this would occur
      // The total number of derivations starts to explode after 10 or so, consequently
      // out of memory exception is inevitable
      System.out.println("Out of Memory exception: " + e.getMessage());
      System.out.println("The algorithm got the " + i + "th iteration before giving up :(");
    }
    
    return false;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}