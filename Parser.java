import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;

public class Parser implements IParser {

  private static Map<Symbol, List<Word>> expansionsMap;

  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {

    expansionsMap = cfg
      .getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
               Collectors.mapping(Rule::getExpansion, Collectors.toList())));

    if (!isValidInput(cfg, w)) return false;

    List<Node> computedDerivations = getComputedDerivations(cfg, w);
    
    return 
      computedDerivations != null
      && computedDerivations.size() > 0;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {

    expansionsMap = cfg
      .getRules()
      .stream()
      .collect(Collectors.groupingBy(Rule::getVariable, 
              Collectors.mapping(Rule::getExpansion, Collectors.toList())));

    if (!isValidInput(cfg, w)) return null;

    List<Node> computedDerivations = getComputedDerivations(cfg, w);

    if (computedDerivations == null) return null;


    Node node = computedDerivations.get(0);
    
    
    List<Node> treeNodes = new ArrayList<Node>();
    while(true) {
      treeNodes.add(node);
      Node parent = node.getParent();
      node = parent;
      if (node.getParentSymbol() == null) break;
    }   
  
    int i = 0;
    ParseTreeNode[] firstChildren = new ParseTreeNode[2];
    Iterator<Symbol> firstChildIterator = node.getWord().iterator();
    while (firstChildIterator.hasNext()) {
      Symbol currentSymbol = firstChildIterator.next();
      Node nextChild = getLeftMostChild(currentSymbol, treeNodes);
      treeNodes.remove(nextChild);
      ParseTreeNode[] childrenOfFirstChild = recurseChildren(nextChild, treeNodes);
      firstChildren[i++] = new ParseTreeNode(currentSymbol, resolveChildArray(childrenOfFirstChild));
    }

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

    return cfg.isInChomskyNormalForm();
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
  
  private Node getLeftMostChild(Symbol parentSymbol, List<Node> treeNodes) {

    List<Node> childNodes = treeNodes
      .stream()
      .filter(n -> n.getParentSymbol().equals(parentSymbol))
      .collect(Collectors.toList());
    
    Collections.sort(childNodes, new Comparator<Node>(){
      public int compare(Node n1, Node n2) {
        Integer a = n1.getReplacementIndex() + n1.getDepth();
        Integer b = n2.getReplacementIndex() + n2.getDepth();
        return a.compareTo(b);
      }
    });

    return childNodes.get(0);
  }

  private ParseTreeNode[] recurseChildren(Node parent, List<Node> treeNodes) {

    ParseTreeNode[] children = new ParseTreeNode[2];
    if (treeNodes.size() == 0) return children;
    
    int i = 0;
    Iterator<Symbol> symbolIterator = parent.getExpansion().iterator();
    while (symbolIterator.hasNext()) {
      Symbol currentSymbol = symbolIterator.next();
      if (currentSymbol.isTerminal()) {
        children[i++] = new ParseTreeNode(currentSymbol);
        continue;
      }
      
      Node nextChild = getLeftMostChild(currentSymbol, treeNodes);
      treeNodes.remove(nextChild);
      ParseTreeNode[] childrenOfChild = resolveChildArray(recurseChildren(nextChild, treeNodes));
      if (childrenOfChild[0] != null) {
        children[i++] = new ParseTreeNode(currentSymbol, childrenOfChild);
      } else {
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

    public void addChild(Word childWord) {
      this.children.add(new Node(this, childWord, depth + 1));
    }

    public void addChild(Node childNode) {
      this.children.add(childNode);
    }

    public void addChildren(List<Node> childNodes) {
      this.children.addAll(childNodes);
    }
  }
}