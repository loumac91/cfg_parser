import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    List<Rule> rules = cfg.getRules();
    Map<Symbol, List<Rule>> ruleMap = rules
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable));

    // i = 0, [ S ]
    // i = 1, [ EG | TH | NC | 1 | 0 | x ]
    // i = 2, [ EEG, EPT | TTH, TMF | -C, N1, N0, Nx ]
    // i = 3, [ EGEG, EEGG, EEPT | EG | ]

    // Start at start variable
    // i = 0,

    // derivations of x+x
    // S => EG => xG => xPT => x+T => x+x
    Word cleaned = new Word(w.toString().replaceAll("\\s",""));
    int n = cleaned.length();
    int derivations = 2 * n - 1;

    // derivationIndex, hashset of derivations
    Variable current = cfg.getStartVariable();
    ArrayList<Word> previousExpansions = new ArrayList<>(){{
      add(new Word(current.toString())); 
    }};

    // LinkedHashMap<Integer, LinkedHashSet<Word>> derivationMap = new LinkedHashMap<Integer, LinkedHashSet<Word>>() {{
    //   put(0, new LinkedHashSet<Word>() {{ 
    //     add(new Word(current.toString())); 
    //   }});
    // }};

    int i = 1;
    while (i <= derivations) {
      ArrayList<Word> currentExpansions = new ArrayList<>();
      // For previous index, get all of the derived words
      // LinkedHashSet<Word> previousWords = derivationMap.get(i++);
      // Get all of the derivations of the words
      for (Word word : previousExpansions) {
        if (i == 5) {
          String x = "";
        }
        if (word.isTerminal()) {
          // currentExpansions.add(word);
          continue;
        }

        // then there are derivations
        // iterate through each symbol, and get the unique set of derivations
        int k = 0;
        Iterator<Symbol> symbols = word.iterator();
        while (symbols.hasNext()) { 
          Symbol currentSymbol = symbols.next();
          if (currentSymbol.isTerminal()) {
            // currentExpansions.add(word);
            k++;
            continue;
          }
          List<Rule> currentRules = ruleMap.getOrDefault(currentSymbol, new ArrayList<Rule>());
          for (Rule r : currentRules) {
            // replace current symbol with expansion
            Word replaced = word.replace(k, r.getExpansion());

            currentExpansions.add(replaced);
          }
          k++;
        }
      }

      // derivationMap.put(i, newWords);
      previousExpansions = currentExpansions;
      i++;
    }

    // return derivationMap
    //   .get(--i)
    //   .contains(cleaned);
    return previousExpansions.contains(cleaned);
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}