package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StructuredConstituent extends IndexedConstituent {

  List<Constituent> constituents = new ArrayList<Constituent>();

  private Integer mark = null;

  private Integer aux = null;

  List<Integer> senses = new ArrayList<Integer>();

  List<Integer> prepPhrases = new ArrayList<Integer>();

  List<Integer> xcomp = new ArrayList<Integer>();

  List<Integer> advcl = new ArrayList<Integer>();

  List<Integer> advmods = new ArrayList<Integer>();

  private List<Integer> amods = new ArrayList<Integer>();

  private List<Integer> nn = new ArrayList<Integer>();

  List<Integer> appos = new ArrayList<Integer>();

  List<Integer> conj = new ArrayList<Integer>();

  List<Integer> infmod = new ArrayList<Integer>();

  List<Integer> partmod = new ArrayList<Integer>();

  List<Integer> rcmod = new ArrayList<Integer>();

  private List<Integer> subjects = new ArrayList<Integer>();

  private List<Integer> ccomp = new ArrayList<Integer>();

  public StructuredConstituent(SemanticGraph semanticGraph, IndexedWord root, Type type) {
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
  public StructuredConstituent(SemanticGraph semanticGraph, IndexedWord root, Set<IndexedWord> additionalVertexes, Set<IndexedWord> excludedVertexes,
      Type type) {
    super(semanticGraph, root, additionalVertexes, excludedVertexes, type);
  }

  public StructuredConstituent build() {
    List<SemanticGraphEdge> edges = semanticGraph.outgoingEdgeList(root);
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
      } else if (DpUtils.isMark(edge)) {
        //anothe clause check
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        setMark(getConstituents().size() - 1);
      } else if (DpUtils.isAux(edge)) {
        //anothe clause check
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        setAux(getConstituents().size() - 1);
      } else if (DpUtils.isAnySubj(edge)) {
        //anothe clause check
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        getSubjects().add(getConstituents().size() - 1);
      } else if (DpUtils.isCcomp(edge)) {
        //anothe clause check
        getConstituents().add(new IndexedConstituent(semanticGraph, edge.getDependent(), null));
        getCcomp().add(getConstituents().size() - 1);
      } else if (DpUtils.isXcomp(edge)) {
        //anothe clause check
        getConstituents().add(new StructuredConstituent(semanticGraph, edge.getDependent(), null).build());
        getXcomps().add(getConstituents().size() - 1);
      }
    }
    return this;
  }

  public List<Integer> getPrepPhrases() {
    return prepPhrases;
  }

  public void setPrepPhrases(List<Integer> prepPhrases) {
    this.prepPhrases = prepPhrases;
  }

  public List<Constituent> getConstituents() {
    return constituents;
  }

  public void setConstituents(List<Constituent> constituents) {
    this.constituents = constituents;
  }

  public List<Integer> getNn() {
    return nn;
  }

  public void setNn(List<Integer> nn) {
    this.nn = nn;
  }

  public List<Integer> getAmods() {
    return amods;
  }

  public void setAmods(List<Integer> amods) {
    this.amods = amods;
  }

  public Integer getMark() {
    return mark;
  }

  public void setMark(Integer mark) {
    this.mark = mark;
  }

  public Integer getAux() {
    return aux;
  }

  public void setAux(Integer aux) {
    this.aux = aux;
  }

  public List<Integer> getSubjects() {
    return subjects;
  }

  public void setSubjects(List<Integer> subjects) {
    this.subjects = subjects;
  }

  public List<Integer> getCcomp() {
    return ccomp;
  }

  public List<Integer> getXcomps() {
    return xcomp;
  }

  public void setCcomp(List<Integer> ccomp) {
    this.ccomp = ccomp;
  }

}
