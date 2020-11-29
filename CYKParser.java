import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;

public class CYKParser implements IParser {

  private static Map<Symbol, List<Word>> expansionsMap;

  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {

    expansionsMap = cfg.getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));

    if (!isValidInput(cfg, w)) return false;

    final int n = w.length();
    Word[][] table = new Word[3 * n][3 * n];

    List<Rule> oneLengthRules = cfg.
      getRules()
      .stream()
      .filter(r -> r.getExpansion().length() == 1)
      .collect(Collectors.toList());

    for (int i = 1; i <= n; i++) {
      Symbol wi = w.get(i - 1);
      for (Rule rule : oneLengthRules) {
        Symbol b = rule.getExpansion().get(0);
        if (wi.equals(b)) {          
          table[i][i] = (table[i][i] == null
            ? new Word(rule.getVariable())
            : concatSymbolToWord(rule.getVariable(), table[i][i]));
        }        
      }
    }

    List<Rule> twoLengthRules = cfg
      .getRules()
      .stream()
      .filter(r -> r.getExpansion().length() == 2)
      .collect(Collectors.toList());

    for (int l = 2; l <= n; l++) {
      for (int i = 1; i <= n + l + 1; i++) {
        int j = i + l - 1;
        for (int k = i; k <= j - 1; k++) {
          for (Rule rule : twoLengthRules) {
            Symbol b = rule.getExpansion().get(0);
            Symbol c = rule.getExpansion().get(1);

            if (table[i][k] != null && table[i][k].indexOfFirst(b) > -1 && 
                table[k + 1][j] != null && table[k + 1][j].indexOfFirst(c) > -1) {
              table[i][j] = (table[i][j] == null
                ? new Word(rule.getVariable())
                : concatSymbolToWord(rule.getVariable(), table[i][j]));
            }
          }
        }
      }
    }

    return table[1][n] != null &&
           table[1][n].indexOfFirst(cfg.getStartVariable()) > -1;
  }



  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {

    expansionsMap = cfg.getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));

    if (!isValidInput(cfg, w)) return null;

    // Not yet implemented
    return null;
  }

  private boolean isValidInput(ContextFreeGrammar cfg, Word w) {
    
    if (w.equals(Word.emptyWord)) {
      return expansionsMap
        .get(cfg.getStartVariable())
        .stream()
        .anyMatch(e -> e.equals(w));
    }

    return cfg.isInChomskyNormalForm();
  }

  private Word concatSymbolToWord(Symbol s, Word w) {
    return new Word(Stream.concat(w.stream(), Arrays.stream(new Symbol[] { s })).toArray(Symbol[]::new));
  }
}
