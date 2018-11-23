package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

import java.util.List;
import java.util.Set;

public class PrepositionalPhraseIndexedConstituent extends StructuredConstituent {

  private IndexedWord pobj;

  public PrepositionalPhraseIndexedConstituent(SemanticGraph semanticGraph, IndexedWord root, Type type) {
    super(semanticGraph, root, type);
  }

  /** Constructs a new indexed constituent for any noun-phrase relation.
   *
   * @param semanticGraph Semantic graph for this constituent ({@see #semanticGraph})
   * @param root The root vertex of this constituent ({@see {@link #root})
   * @param additionalVertexes Additional root vertexes that form this constituent ({@see
   *            {@link #additionalVertexes})
   * @param excludedVertexes Vertexes that are excluded from this constituent ({@see
   *            {@link #excludedVertexes})
   * @param type type of this constituent
   * * @param clauses derived from this constituent*/
  public PrepositionalPhraseIndexedConstituent(SemanticGraph semanticGraph, IndexedWord root, Set<IndexedWord> additionalVertexes,
      Set<IndexedWord> excludedVertexes, Type type) {
    super(semanticGraph, root, additionalVertexes, excludedVertexes, type);
  }

  public PrepositionalPhraseIndexedConstituent build() {
    if (!semanticGraph.getChildList(root).isEmpty()) setPobj(semanticGraph.getChildList(root).get(0));
    else return this;
    List<SemanticGraphEdge> edges = semanticGraph.outgoingEdgeList(getPobj());
    for (SemanticGraphEdge edge : edges) {
      if (DpUtils.isAnyPrep(edge)) {
        getConstituents().add(new PrepositionalPhraseIndexedConstituent(semanticGraph, edge.getDependent(), null).build());
        getPrepPhrases().add(getConstituents().size() - 1);
      } else if (DpUtils.isAdvmod(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        advmods.add(getConstituents().size() - 1);
      } else if (DpUtils.isAMod(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        getAmods().add(getConstituents().size() - 1);
      } else if (DpUtils.isNN(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        getNn().add(getConstituents().size() - 1);
      } else if (DpUtils.isAppos(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        appos.add(getConstituents().size() - 1);
      } else if (DpUtils.isAnyConj(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        conj.add(getConstituents().size() - 1);
      } else if (DpUtils.isInfmod(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        infmod.add(getConstituents().size() - 1);
      } else if (DpUtils.isPartMod(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        partmod.add(getConstituents().size() - 1);
      } else if (DpUtils.isRcmod(edge)) {
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        rcmod.add(getConstituents().size() - 1);
      } else if (DpUtils.isAdvcl(edge)) {
        //anothe clause check
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        advcl.add(getConstituents().size() - 1);
      }

    }
    return this;
  }

  public IndexedWord getPobj() {
    return pobj;
  }

  public void setPobj(IndexedWord pobj) {
    this.pobj = pobj;
  }
}
