package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/** An {@code XcompConstituent} of a clause formed out of an xcomp.
 *
 * Note that the xcomp relation refers to a clause with an external subject.
 * The constituent stores the set of clauses that can be derived from the xcomp 
 * clause. 
 *
 * @date $LastChangedDate: 2013-04-23 00:04:28 +0200 (Tue, 23 Apr 2013) $
 * @version $LastChangedRevision: 734 $ */
public class XcompConstituent extends StructuredConstituent {

  /** Clauses derived from this constituent */
  private Set<Clause> clauses;

  /** Constructs a new constituent for the xcomp relation.
   *
   * @param semanticGraph Semantic graph for this constituent ({@see #semanticGraph})
   * @param root The root vertex of this constituent ({@see {@link #root})
   * @param type type of this constituent
   * @param clauses derived from this constituent*/
  public XcompConstituent(SemanticGraph semanticGraph, IndexedWord root, Type type, Set<Clause> clauses) {
    super(semanticGraph, root, type);
    this.setClauses(clauses);
  }

  /** Constructs a new indexed constituent for the xcomp relation.
   *
   * @param semanticGraph Semantic graph for this constituent ({@see #semanticGraph})
   * @param root The root vertex of this constituent ({@see {@link #root})
   * @param additionalVertexes Additional root vertexes that form this constituent ({@see
   *            {@link #additionalVertexes})
   * @param excludedVertexes Vertexes that are excluded from this constituent ({@see
   *            {@link #excludedVeClausrtexes})
   * @param type type of this constituent
   * * @param clauses derived from this constituent*/
  public XcompConstituent(SemanticGraph semanticGraph, IndexedWord root, Set<IndexedWord> additionalVertexes, Set<IndexedWord> excludedVertexes,
      Type type, Set<Clause> clauses) {
    super(semanticGraph, root, additionalVertexes, excludedVertexes, type);
    this.setClauses(clauses);
  }

  /** Returns the clauses derived from the constituent. */
  public Set<Clause> getClauses() {
    return clauses;
  }

  /** Sets the clauses derived from the constituent. */
  public void setClauses(Set<Clause> clauses) {
    this.clauses = clauses;
  }

  @Override public XcompConstituent clone() {
    XcompConstituent clone = new XcompConstituent(semanticGraph, root, type, clauses);
    return clone;
  }

}
