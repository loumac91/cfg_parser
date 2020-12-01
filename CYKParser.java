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
    return generateParseTree(cfg, w) != null;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {

    setExpansionMap(cfg);

    if (!isValidInput(cfg, w)) return null;

    Cell[][] cykTable = computeCyk(cfg, w);

    final int n = w.length();
    Cell startingCell = cykTable[1][n];
    Variable startingVariable = cfg.getStartVariable();
    Boolean isValid = startingCell != null && 
                      startingCell.cellWord != null &&
                      startingCell.cellWord.indexOfFirst(cfg.getStartVariable()) > -1;

    if (!isValid) return null;

    return new ParseTreeNode(startingVariable, resolveChildArray(recurseChildren(startingVariable, startingCell)));
  } 

  private void setExpansionMap(ContextFreeGrammar cfg) {
    expansionsMap = cfg
      .getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));
  }

  private boolean isValidInput(ContextFreeGrammar cfg, Word w) {
    
    if (w.equals(Word.emptyWord)) {
      return expansionsMap
        .get(cfg.getStartVariable())
        .stream()
        .anyMatch(e -> e.equals(w));
    }

    if (w.count(new Terminal(' ')) == w.length()) return false;

    return cfg.isInChomskyNormalForm();
  }

  private Cell[][] computeCyk(ContextFreeGrammar cfg, Word w) {
    
    // Algorithm 2
    
    final int n = w.length();

    // max i value is 2n + 1
    // max l value is n
    // max j value is i + j - 1 = 3n

    final int rows = 2 * n + 2; // SHOULD BE 3n?
    final int columns = 3 * n;
    Cell[][] table = createCellTable(rows, columns);        

    List<Rule> oneLengthRules = cfg
      .getRules()
      .stream()
      .filter(r -> r.getExpansion().length() == 1)
      .collect(Collectors.toList());

    for (int i = 1; i <= n; i++) {
      Symbol wi = w.get(i - 1);
      for (Rule rule : oneLengthRules) {
        Symbol b = rule.getExpansion().get(0);
        if (wi.equals(b)) {
          table[i][i].cellWord = (table[i][i].cellWord == null
            ? new Word(rule.getVariable())
            : concatSymbolToWord(rule.getVariable(), table[i][i].cellWord));
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

            if (table[i][k].cellWord != null && table[i][k].cellWord.indexOfFirst(b) > -1 && 
                table[k + 1][j].cellWord != null && table[k + 1][j].cellWord.indexOfFirst(c) > -1) {

              table[i][j].cellWord = (table[i][j].cellWord == null
                ? new Word(rule.getVariable())
                : concatSymbolToWord(rule.getVariable(), table[i][j].cellWord));
              
              table[i][j].leftCell = table[i][k];
              table[i][j].rightCell = table[k + 1][j];
              table[i][j].left = b;
              table[i][j].right = c;
              table[i][j].leftCell.resolve = w.subword(i - 1, k);
              table[i][j].rightCell.resolve = w.subword(i, j);
            }
          }
        }
      }
    }

    return table;
  }

  private Cell[][] createCellTable(int rows, int columns) {
    Cell[][] table = new Cell[rows][columns];
    for (int i = 0; i <= rows - 1; i++)
      for (int j = 0; j <= columns - 1; j++)
        table[i][j] = new Cell();

    return table;
  }

  private Word concatSymbolToWord(Symbol s, Word w) {
    return new Word(Stream.concat(w.stream(), Arrays.stream(new Symbol[] { s })).toArray(Symbol[]::new));
  }

  private ParseTreeNode[] resolveChildArray(ParseTreeNode[] childArray) {
    return childArray[1] == null ? new ParseTreeNode[] { childArray[0] } : childArray;
  }
  
  private ParseTreeNode[] recurseChildren(Symbol parentSymbol, Cell cykCell) {

    ParseTreeNode[] children = new ParseTreeNode[2];

    Boolean leftNull = cykCell.left == null;
    Boolean rightNull = cykCell.right == null;

    if (leftNull && rightNull) {
      children[0] = new ParseTreeNode((Terminal)cykCell.resolve.get(0));
      return children;
    }

    if (!leftNull) {
      children[0] = new ParseTreeNode(cykCell.left, resolveChildArray(recurseChildren(cykCell.left, cykCell.leftCell)));
    }

    if (!rightNull) {
      children[1] = new ParseTreeNode(cykCell.right, resolveChildArray(recurseChildren(cykCell.right, cykCell.rightCell)));
    }

    return children;
  }

  // Simple class just to store state
  private class Cell {
    public Word cellWord;
    public Cell leftCell;
    public Cell rightCell;
    public Symbol left;
    public Symbol right;
    public Word resolve;
  }
}
