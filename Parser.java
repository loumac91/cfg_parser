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

    List<Rule> rules = cfg.getRules();
    Map<Symbol, List<Rule>> ruleMap = rules
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable));

    Variable current = cfg.getStartVariable();
    HashSet<Word> previousExpansions = new HashSet<>(){{
      add(new Word(current)); 
    }};

    int i = 1;
    int n = w.length();
    int derivations = 2 * n - 1;
    // On my computer, things really start to grind to a halt after the 14th iteration, and we start running into memory issues
    
    try {
      while (i <= derivations) {
        HashSet<Word> currentExpansions = new HashSet<>();
        // Iterate through each previous expansions
        for (Word word : previousExpansions) {
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
              Word expansion = r.getExpansion();
              Word replaced = word.replace(replaceIndex, expansion);

              if (replaced.equals(w)) {
                // If we find a match, be nice and return early
                return true;
              }
              currentExpansions.add(replaced);
            }
  
            replaceIndex++;
          }
        }
  
        previousExpansions = currentExpansions;
        i++;
      }
    } catch (OutOfMemoryError e) {
      System.out.println("Out of Memory exception: " + e.getMessage());
      System.out.println("The algorithm got the " + i + "th iteration before giving up :(");
    }
    
    return false;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}