import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;

public class Parser implements IParser {

  private static Map<Symbol, List<Word>> expansionsMap;

  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {
    return generateParseTree(cfg, w) != null;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {

    // Set static property expansionsMap:
    // This groups all the expansions under each rule symbol
    // which allows simple lookup via ".get(symbol)"
    setExpansionMap(cfg);

    if (!isValidInput(cfg, w)) return null;

    List<Node> computedDerivations = getComputedDerivations(cfg, w);

    if (computedDerivations == null || computedDerivations.size() == 0) return null;

    // 1. Pick any given node in the list of all possible derivation paths
    Node node = computedDerivations.get(0);

    if (w.length() == 1) {
      return new ParseTreeNode(cfg.getStartVariable(), new ParseTreeNode(node.getExpansion().get(0)));
    }
    
    // 2. Walk back up the tree from end to start, adding each node to a list
    // this represents the derivation path
    List<Node> derivationNodes = new ArrayList<Node>();
    while(true) {
      derivationNodes.add(node);
      Node parent = node.getParent();
      node = parent;
      if (node.getParentSymbol() == null) break;
    }   
  
    // 3. Recursively determine the children of the cfgs start variable
    int i = 0;
    ParseTreeNode[] firstChildren = new ParseTreeNode[2];
    Iterator<Symbol> firstChildIterator = node.getWord().iterator();
    while (firstChildIterator.hasNext()) {
      Symbol currentSymbol = firstChildIterator.next();
      Node nextChild = getLeftMostChild(currentSymbol, derivationNodes);
      derivationNodes.remove(nextChild); // Remove the node so that its not picked up again by above method
      ParseTreeNode[] childrenOfFirstChild = resolveChildArray(recurseChildren(nextChild, derivationNodes));
      if (childrenOfFirstChild[0] != null) {
        firstChildren[i++] = new ParseTreeNode(currentSymbol, childrenOfFirstChild);
      } else {
        firstChildren[i++] = new ParseTreeNode(currentSymbol, new ParseTreeNode(nextChild.getExpansion().get(0)));
      }  
    }

    // 4. Create the final top level parse tree node and return
    ParseTreeNode result = new ParseTreeNode(cfg.getStartVariable(), firstChildren);

    return result;
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

  private void setExpansionMap(ContextFreeGrammar cfg) {
    expansionsMap = cfg
      .getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));
  }

  // region Algorithm #1

  private List<Node> getComputedDerivations(ContextFreeGrammar cfg, Word w) {

    // On an input w for Grammar G
    // List all derivations with 2n - 1 steps where n = |w|, unless n = 0, then list
    // all derivatins with 1 step
    // If any of these derivations generate w, then accept. Otherwise reject

    // Algorithm #1
    // Iterate through each set of expansions, computing all possible derivations
    // i = 0, [ S ]
    // i = 1, [ EG | TH | NC | 1 | 0 | x ]
    // i = 2, [ EEG, THG, NCG, 1G, 0G, xG, EPT, TTH, NCH, 1H, 0H, 0x ... etc ]

    final int startingDepth = 0;
    final int n = w.length();
    final int derivationDepth = 2 * n - 1;

    Symbol startVariable = cfg.getStartVariable();
    final Node root = new Node(new Word(startVariable), startingDepth);

    if (n == 1) {
      Word matchingWordFromStartVariable = expansionsMap
        .get(startVariable)
        .stream()
        .filter(startingExpansion -> startingExpansion.isTerminal() && startingExpansion.equals(w))
        .findFirst()
        .orElse(null);

      return matchingWordFromStartVariable.equals(null)
        ? null
        : new ArrayList<>(Arrays.asList(
          new Node(root, matchingWordFromStartVariable, startingDepth + 1) {{
            setParentSymbol(startVariable);
            setReplacementIndex(0);
            setExpansion(matchingWordFromStartVariable);
          }}
        ));
    }
    
    try {
      List<Node> matchingNodes = expansionsMap
        .get(startVariable)
        .stream()
        .map(startingExpansion -> recurseNode(root, startingExpansion, startingDepth, derivationDepth))
        .map(childDerivation -> recurseMatchingChildren(childDerivation, w, derivationDepth))
        .flatMap(HashSet::stream)
        .collect(Collectors.toList());

      return matchingNodes;

    } catch (OutOfMemoryError e) {
      // On my computer, I would usually hit the ~14th iteration before this would occur
      // The total number of derivations starts to explode after 10 or so, consequently
      // out of memory exception is inevitable
      System.out.println("Out of Memory exception: " + e.getMessage());
    }

    return null;
  }

  private Node recurseNode(Node parent, Word word, int depth, int maxDepth) {
    Node node = new Node(parent, word, ++depth);
    if (depth == maxDepth || word.isTerminal()) {
      return node;
    }

    int replaceIndex = 0;
    Iterator<Symbol> symbols = word.iterator();
    while (symbols.hasNext()) {
      Symbol currentSymbol = symbols.next();
      if (currentSymbol.isTerminal()) {
        replaceIndex++;
        continue;
      }

      List<Word> currentExpansions = expansionsMap.get(currentSymbol);
      for (Word expansion : currentExpansions) {
        Symbol parentSymbol = word.get(replaceIndex);
        Word replaced = word.replace(replaceIndex, expansion);
        Node childNode = recurseNode(node, replaced, depth, maxDepth);
        childNode.setParentSymbol(parentSymbol);
        childNode.setReplacementIndex(replaceIndex);
        childNode.setExpansion(expansion);
        node.addChild(childNode);
      }

      replaceIndex++;
    }

    return node;
  }

  private HashSet<Node> recurseMatchingChildren(Node node, Word wordToMatch, int derivationDepth) {
    HashSet<Node> matchingChildren = new HashSet<Node>();

    Word word = node.getWord();
    if (word.equals(wordToMatch) && node.getDepth() == derivationDepth) {
      matchingChildren.add(node);
      return matchingChildren;
    }

    for (Node child : node.getChildren()) {
      matchingChildren.addAll(recurseMatchingChildren(child, wordToMatch, derivationDepth));
    }

    return matchingChildren;
  }

  // region generateParseTrees
  
  private Node getLeftMostChild(Symbol parentSymbol, List<Node> derivationNodes) {

    List<Node> childNodes = derivationNodes
      .stream()
      .filter(n -> n.getParentSymbol().equals(parentSymbol))
      .collect(Collectors.toList());
    
    // Replacement index represents the index of where the substitution occurs between parent and child
    // Depth refers to the number of recursions made to compute the derivation
    // The children of the child are sorted here by both these properties to always return the first
    // occurrence of a given symbol. We do this as the outer algorithm is working from top level down to 
    // bottom level - and there can of course be multiple children within the derivation tree with the same
    // symbol

    Collections.sort(childNodes, new Comparator<Node>(){
      public int compare(Node n1, Node n2) {
        Integer a = n1.getReplacementIndex() + n1.getDepth();
        Integer b = n2.getReplacementIndex() + n2.getDepth();
        return a.compareTo(b);
      }
    });

    return childNodes.get(0);
  }

  private ParseTreeNode[] recurseChildren(Node parent, List<Node> derivationNodes) {

    ParseTreeNode[] children = new ParseTreeNode[2];
    // Base condition is to recurse until no derivation nodes remain
    if (derivationNodes.size() == 0) return children;
    
    int i = 0;
    Iterator<Symbol> symbolIterator = parent.getExpansion().iterator();
    while (symbolIterator.hasNext()) {
      Symbol currentSymbol = symbolIterator.next();
      if (currentSymbol.isTerminal()) {
        children[i++] = new ParseTreeNode(currentSymbol);
        continue;
      }
      
      Node nextChild = getLeftMostChild(currentSymbol, derivationNodes);
      derivationNodes.remove(nextChild);
      ParseTreeNode[] childrenOfChild = resolveChildArray(recurseChildren(nextChild, derivationNodes));
      if (childrenOfChild[0] != null) { // child of parent has children
        children[i++] = new ParseTreeNode(currentSymbol, childrenOfChild);
      } else { // child of parent has no children
        children[i++] = new ParseTreeNode(currentSymbol, new ParseTreeNode(nextChild.getExpansion().get(0)));
      }      
    }

    return children;
  }

  private ParseTreeNode[] resolveChildArray(ParseTreeNode[] childArray) {
    return childArray[1] == null ? new ParseTreeNode[] { childArray[0] } : childArray;
  }

  private class Node {
    private final Node parent;
    private final Word word;
    private final Integer depth;
    private final HashSet<Node> children;
    private Symbol parentSymbol;
    private Integer replacementIndex;
    private Word expansion;

    public Node(Word w, int depth) {
      this(null, w, depth);
    }

    public Node(Node parent, Word w, int depth) {
      this.parent = parent;
      this.word = w;
      this.depth = depth;
      this.children = new HashSet<Node>();
    }

    public Node getParent() {
      return this.parent;
    }

    public Word getWord() {
      return this.word;
    }

    public Integer getDepth() {
      return this.depth;
    }

    public HashSet<Node> getChildren() {
      return this.children;
    }
    
    public void setParentSymbol(Symbol pParentSymbol) {
      this.parentSymbol = pParentSymbol;
    }

    public Symbol getParentSymbol() {
      return this.parentSymbol;
    }

    public void setReplacementIndex(int pReplacementIndex) {
      this.replacementIndex = pReplacementIndex;
    }

    public Integer getReplacementIndex() {
      return this.replacementIndex;
    }

    public void setExpansion(Word pEexpansion) {
      this.expansion = pEexpansion;
    }

    public Word getExpansion() {
      return this.expansion;
    }

    public void addChild(Node childNode) {
      this.children.add(childNode);
    }
  }
}