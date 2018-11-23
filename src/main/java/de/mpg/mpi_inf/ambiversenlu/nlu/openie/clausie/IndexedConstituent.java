package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** A constituent of a clause described by a {@link SemanticGraph}.
 *
 * Each constituent has a root vertex. The root together with its the descendants form the
 * constituent. In some cases, additional vertexes need to be included or excluded; 
 * these vertexes are also recorded within this class.
 *
 * Note that the {@link SemanticGraph} may or may not match the graph of the input sentences or the
 * other constituents of the same clause. For example, the semantic graphs are modified when
 * processing of coordinating conjunctions.
 *
 * @date $LastChangedDate: 2014-04-04 10:50:46 +0200 (Fri, 04 Apr 2014) $
 * @version $LastChangedRevision: 1554 $ */
public class IndexedConstituent extends Constituent {

  // -- member variables ------------------------------------------------------------------------

  /** Semantic graph for this constituent */
  protected SemanticGraph semanticGraph;

  /** The root vertex of this constituent in {@link #semanticGraph}. This vertex and all its
   * descendants are part of the constituent (unless they appear in {@link #excludedVertexes}). */
  protected IndexedWord root;

  /** Additional root vertexes that form this constituent. These vertexes and all their descendants
   * are part of the constituent (unless they appear in {@link #excludedVertexes}). */
  protected Set<IndexedWord> additionalVertexes;

  /** Vertexes that are excluded from this constituent. All descendants are excluded as well
   * (unless they appear in {@link #root} or {@link additionalRoots}). */
  protected Set<IndexedWord> excludedVertexes;

  // -- construction ----------------------------------------------------------------------------

  protected IndexedConstituent() {
  }

  /** Constructs a new indexed constituent.
   *
   * @param semanticGraph Semantic graph for this constituent ({@see #semanticGraph})
   * @param root The root vertex of this constituent ({@see {@link #root})
   * @param additionalVertexes Additional root vertexes that form this constituent ({@see
   *            {@link #additionalVertexes})
   * @param excludedVertexes Vertexes that are excluded from this constituent ({@see
   *            {@link #excludedVertexes})
   * @param type type of this constituent */
  public IndexedConstituent(SemanticGraph semanticGraph, IndexedWord root,
      Set<IndexedWord> additionalVertexes, Set<IndexedWord> excludedVertexes,
      Type type) {
    super(type);
    this.semanticGraph = semanticGraph;
    this.root = root;
    this.additionalVertexes = new TreeSet<IndexedWord>(additionalVertexes);
    this.excludedVertexes = new TreeSet<IndexedWord>(excludedVertexes);
  }

  /** Constructs a simple indexed constituent without additional additional or excluded vertexes.
   *
   * @param semanticGraph Semantic graph for this constituent ({@see #semanticGraph})
   * @param root The root vertex of this constituent ({@see {@link #root})
   * @param type type of this constituent */
  public IndexedConstituent(SemanticGraph semanticGraph, IndexedWord root, Type type) {
    this(semanticGraph, root, new TreeSet<IndexedWord>(), new TreeSet<IndexedWord>(), type);
  }

  /** Creates a deep copy of this indexed constituent. */
  @Override public IndexedConstituent clone() {
    IndexedConstituent clone = new IndexedConstituent();
    clone.type = type;
    clone.semanticGraph = new SemanticGraph(semanticGraph);
    clone.root = this.root;
    clone.additionalVertexes = new TreeSet<IndexedWord>(this.additionalVertexes);
    clone.excludedVertexes = new TreeSet<IndexedWord>(this.excludedVertexes);
    return clone;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IndexedConstituent that = (IndexedConstituent) o;

    if (semanticGraph != null ? !semanticGraph.equals(that.semanticGraph) : that.semanticGraph != null) return false;
    if (root != null ? !root.equals(that.root) : that.root != null) return false;
    if (additionalVertexes != null ? !additionalVertexes.equals(that.additionalVertexes) : that.additionalVertexes != null) return false;
    return excludedVertexes != null ? excludedVertexes.equals(that.excludedVertexes) : that.excludedVertexes == null;
  }

  @Override public int hashCode() {
    int result = semanticGraph != null ? semanticGraph.hashCode() : 0;
    result = 31 * result + (root != null ? root.hashCode() : 0);
    result = 31 * result + (additionalVertexes != null ? additionalVertexes.hashCode() : 0);
    result = 31 * result + (excludedVertexes != null ? excludedVertexes.hashCode() : 0);
    return result;
  }
  // -- getters/setters -------------------------------------------------------------------------

  /** Returns the semantic graph for this constituent ({@see #semanticGraph}). */
  public SemanticGraph getSemanticGraph() {
    return semanticGraph;
  }

  /** Sets the semantic graph for this constituent ({@see #semanticGraph}). */
  public void setSemanticGraph(SemanticGraph newSemanticGraph) {
    semanticGraph = newSemanticGraph;
  }

  /** Returns the root vertex of this constituent ({@see {@link #root}). */
  public IndexedWord getRoot() {
    return root;
  }

  /** Sets the root vertex of this constituent ({@see {@link #root}). */
  public void setRoot(IndexedWord newRoot) {
    root = newRoot;
  }

  /** Returns additional root vertexes that form this constituent ({@see
   * {@link #additionalVertexes}). */
  public Set<IndexedWord> getAdditionalVertexes() {
    return additionalVertexes;
  }

  /** Returns vertexes that are excluded from this constituent ({@see {@link #excludedVertexes}). */
  public Set<IndexedWord> getExcludedVertexes() {
    return excludedVertexes;
  }

  /** Checks whether this constituent is a prepositional phrase (i.e., starts with a preposition). */
  public boolean isPrepositionalPhrase() { //This is a mess, find other way of fixing. This is purelly heuristic. It needs to know the semantic graph for the sentence after this is fixed the member variable sentSemanticGraph can be removed
    List<IndexedWord> parents = semanticGraph.getParentList(
        root); //This is not the cleanest way semantics messed up. specially with the rel we cannot just check if the head is a preposition (return root.tag().equals("IN")) because the parser some times includes a preposition in the verbal phrase "He is about to win"
    for (IndexedWord parent : parents) {
      SemanticGraphEdge edge = semanticGraph.getEdge(parent, root);
      if (DpUtils.isRel(edge)) return true;
      if (DpUtils.isAnyPrep(edge)) {
        List<IndexedWord> ancestors = semanticGraph.getParentList(parent);
        for (IndexedWord ancestor : ancestors) {
          SemanticGraphEdge ed = semanticGraph.getEdge(ancestor, parent);
          if (DpUtils.isRcmod(ed)) return true;
        }
      }
    }
    return false;
    //return root.tag().equals("IN");
  }

  // -- utility methods -------------------------------------------------------------------------

  /** Returns a textual representation of the root word of this constituent. */
  public String rootString() {
    return root.word();
  }

  /** Returns a copy of the semantic graph of this constituent in which all edges (from any
   * included vertex) to excluded vertexes have been removed. Useful for proposition generation. */
  public SemanticGraph createReducedSemanticGraph() {
    SemanticGraph result = new SemanticGraph(semanticGraph);
    DpUtils.removeEdges(result, root, excludedVertexes);
    for (IndexedWord v : additionalVertexes) {
      DpUtils.removeEdges(result, v, excludedVertexes);
    }
    return result;
  }

  public String toString() {
    return root.word() + "#" + type.toString();
  }

}
