package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Constituent.Flag;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.*;

/**
 * A clause is a basic unit of a sentence. In ClausIE, a clause consists of a
 * set of constituents (at least a subject and a verb) and a type.
 */
public class Clause {

  boolean processed = false;

  private SemanticGraph semanticGraph;

  public void removeConstituents(List<Integer> toRemove) {
    Collections.sort(toRemove, Collections.reverseOrder());
    for(Integer cindex: toRemove) {
      constituents.remove((int)cindex);
      if(adverbialFlags.containsKey(cindex)) {
        adverbialFlags.remove((int)cindex);
      }
      dobjects.remove(cindex);
      reaRrangeIndices(cindex, dobjects);
      iobjects.remove(cindex);
      reaRrangeIndices(cindex, iobjects);
      if(complement == cindex) {
        complement = -1;
      } else if(complement > cindex) {
        complement--;
      }
      if(subject == cindex) {
        subject = -1;
      } else if(subject > cindex) {
        subject--;
      }
      if(verb == cindex) {
        verb = -1;
      } else if(verb > cindex) {
        verb--;
      }
      xcomps.remove(cindex);
      reaRrangeIndices(cindex, xcomps);
      ccomps.remove(cindex);
      reaRrangeIndices(cindex, ccomps);
      acomps.remove(cindex);
      reaRrangeIndices(cindex, acomps);
      adverbials.remove(cindex);
      reaRrangeIndices(cindex, adverbials);
      deps.remove(cindex);
      reaRrangeIndices(cindex, deps);
    }
    processed = true;
  }

  private void reaRrangeIndices(Integer index, List<Integer> element) {
    for(int i = 0; i < element.size(); i++) {
      if(index < element.get(i)) {
        element.set(i, element.get(i)-1);
      }
    }
  }

  public SemanticGraph createSemanticGraph(boolean connectHeads) {
    SemanticGraph semanticGraph = new SemanticGraph(this.semanticGraph);
    Set<SemanticGraphEdge> edges = new HashSet<>();
    collectEdges(edges);
    List<SemanticGraphEdge> toRemove = semanticGraph.edgeListSorted();
    for(SemanticGraphEdge edge: toRemove) {
      semanticGraph.removeEdge(edge);
    }
    for(SemanticGraphEdge edge: edges) {
      semanticGraph.addEdge(edge);
    }
    if(connectHeads) {
      connectHeads(semanticGraph);
    }
    return semanticGraph;
  }

  private void connectHeads(SemanticGraph semanticGraph) {
    Constituent vc = constituents.get(verb);
    if(vc instanceof TextConstituent) {
      return;
    }
    IndexedConstituent vic  = (IndexedConstituent)vc;
    for (int i = 0; i < constituents.size(); i++) {
      if (i == verb || constituents.get(i) instanceof TextConstituent) {
        continue;
      }
      IndexedConstituent dic = (IndexedConstituent) constituents.get(i);
      semanticGraph.addEdge(vic.root, dic.root, GrammaticalRelation.DEPENDENT, 0.0, false);
    }
  }

  private void collectEdges(Set<SemanticGraphEdge> edges) {
    for(Constituent c: getConstituents()) {
      if(c instanceof IndexedConstituent) {
        collectEdges(((IndexedConstituent) c).root, ((IndexedConstituent) c).createReducedSemanticGraph(), edges);
      };
    }
  }

  private void collectEdges(IndexedWord root, SemanticGraph semanticGraph, Set<SemanticGraphEdge> edges) {
    for(SemanticGraphEdge edge: semanticGraph.getOutEdgesSorted(root)) {
      if(edges.contains(edge)) {
        break;
      }
      edges.add(edge);
      collectEdges(edge.getDependent(), semanticGraph, edges);
    }
  }

  public SemanticGraph getSemanticGraph() {
    return semanticGraph;
  }

  public void setDictRelation(String dictRelation) {
    this.dictRelation = dictRelation;
  }

  public String getDictRelation() {
    return dictRelation;
  }

  // -- Type definition
  // -------------------------------------------------------------------------

  /** Clause types */
  public enum Type {
    SV, SVC, SVA, SVO, SVOO, SVOC, SVOA, EXISTENTIAL, UNKNOWN;
  }

  ;

  // -- member variables
  // -----------------------------------------------------------------------

  /** Constituents of this clause */
  private List<Constituent> constituents = new ArrayList<Constituent>();

  private Map<Integer, Flag> adverbialFlags = new HashMap<>();

  public Flag getFlag(int index) {
    return adverbialFlags.get(index);
  }

  public void setFlag(int index, Flag flag) {
    adverbialFlags.put(index, flag);
  }


  /** Type of this clause */
  private Type type = Type.UNKNOWN;

  /** Position of subject in {@link #constituents} */
  private int subject = -1;

  /** Position of verb in {@link #constituents} */
  private int verb = -1;

  private boolean cop;

  private boolean passive;

  // They are lists because some times the parsers (probably an error)
  // generates more than one constituent of each type
  // e.g., more than one dobj produced by parser for
  // "The man who I told the fact is dead."

  /** Position(s) of direct object(s) in {@link #constituents}. */
  private List<Integer> dobjects = new ArrayList<Integer>();

  /** Position(s) of indirect object in {@link #constituents} */
  private List<Integer> iobjects = new ArrayList<Integer>();

  /** Position of complement in {@link #constituents} (for SVC / SVOC) */
  private int complement = -1;

  /** Position(s) of xcomps in {@link #constituents} */
  private List<Integer> xcomps = new ArrayList<Integer>();

  /** Position(s) of ccomps in {@link #constituents} */
  private List<Integer> ccomps = new ArrayList<Integer>();

  /** Position(s) of acomps in {@link #constituents} */
  private List<Integer> acomps = new ArrayList<Integer>();

  /** Position(s) of adverbials in {@link #constituents} */
  private List<Integer> adverbials = new ArrayList<Integer>();

  /** Non identified dependencies */
  private List<Integer> deps = new ArrayList<Integer>();

  /** If a relative pronoun refers to an adverbial */
  private boolean relativeAdverbial = false;
  /** Agent (for passive voice). Currently unused. */
  IndexedWord agent;

  /** Root of the clause. */
  IndexedWord root;

  private String dictRelation;

  // -- construction
  // ----------------------------------------------------------------------------

  // make package private
  public Clause(SemanticGraph semanticGraph) {
    this.semanticGraph = semanticGraph;
  }

  ;

  @Override public Clause clone() {
    Clause clause = new Clause(new SemanticGraph(semanticGraph));
    List<Constituent> nConstituents = new ArrayList<>();
    for(Constituent c: getConstituents()) {
      nConstituents.add(c.clone());
    }
    clause.setConstituents(nConstituents);
    clause.setType(type);
    clause.setSubject(subject);
    clause.setVerb(verb);
    clause.setRoot(root);
    clause.setDobjects(new ArrayList<Integer>(getDobjects()));
    clause.setIobjects(new ArrayList<Integer>(getIobjects()));
    clause.setComplement(complement);
    clause.setXcomps(new ArrayList<Integer>(getXcomps()));
    clause.setCcomps(new ArrayList<Integer>(getCcomps()));
    clause.setAcomps(new ArrayList<Integer>(getAcomps()));
    clause.setAdverbials(new ArrayList<Integer>(getAdverbials()));
    clause.setRelativeAdverbial(relativeAdverbial);
    clause.agent = agent;
    clause.setFlags(new HashMap<>(adverbialFlags));
    clause.setDictRelation(dictRelation);

    return clause;
  }

  // -- methods
  // ---------------------------------------------------------------------------------

  /** Determines the type of this clause, if still unknown. */
  void detectType(Options options) {
    if (getType() != Type.UNKNOWN) return;

    // count the total number of complements (dobj, ccomp, xcomp)
    int noComplements = noComplements();

    // sometimes the parsers gives ccomp and xcomp instead of direct objects
    // e.g., "He is expected to tell the truth."
    IndexedWord root = ((IndexedConstituent) getConstituents().get(getVerb())).getRoot();
    boolean hasDirectObject = getDobjects().size() > 0 || (getComplement() < 0 && noComplements > 0 && !options.isCop(root));
    boolean hasIndirectObject = !getIobjects().isEmpty();

    // Q1: Object?
    if (hasDirectObject || hasIndirectObject) {
      // Q7: dir. and indir. object?
      if (noComplements > 0 && hasIndirectObject) {
        setType(Type.SVOO);
        return;
      }

      // Q8: Complement?
      if (noComplements > 1) {
        setType(Type.SVOC);
        return;
      }

      // Q9: Candidate adverbial and direct objects?
      if (!(hasCandidateAdverbial() && hasDirectObject)) {
        setType(Type.SVO);
        return;
      }

      // Q10: Potentially complex transitive?
      if (options.isComTran(root)) {
        setType(Type.SVOA);
        return;
      }

      // Q11: Conservative?
      if (options.conservativeSVOA) {
        setType(Type.SVOA);
        return;
      } else {
        setType(Type.SVO);
        return;
      }
    } else {
      // Q2: Complement?
      // not sure about acomp, can a copular be transitive?
      if (getComplement() >= 0 || noComplements > 0 && options.isCop(root) || !getAcomps().isEmpty()) {
        setType(Type.SVC);
        return;
      }

      // Q3: Candidate adverbial
      if (!hasCandidateAdverbial()) {
        setType(Type.SV);
        return;
      }

      // Q4: Known non ext. copuular
      if (options.isNotExtCop(root)) {
        setType(Type.SV);
        return;
      }

      // Q5: Known ext. copular
      if (options.isExtCop(root)) {
        setType(Type.SVA);
        return;
      }

      // Q6: Conservative
      if (options.conservativeSVA) {
        setType(Type.SVA);
        return;
      } else {
        setType(Type.SV);
        return;
      }
    }
  }

  /**
   * Checks whether this clause has a candidate adverbial, i.e., an adverbial
   * that can potentially be obligatory.
   */
  public boolean hasCandidateAdverbial() {
    if (getAdverbials().isEmpty()) return false;
    if (isRelativeAdverbial()) return true;

    // is there an adverbial that occurs after the verb?
    if (((IndexedConstituent) getConstituents().get(getAdverbials().get(getAdverbials().size() - 1))).getRoot().index()
        > ((IndexedConstituent) getConstituents().get(getVerb())).getRoot().index()) return true;
    return false;
  }

  /**
   * Determines the total number of complements (includes direct objects,
   * subject complements, etc.) present in this clause.
   */
  int noComplements() {
    return getDobjects().size() + (getComplement() < 0 ? 0 : 1) + getXcomps().size() + getCcomps().size();
  }

  @Override public String toString() {
    return toString(null);
  }

  public String toString(Options options) {
    Clause clause = this;
    StringBuffer s = new StringBuffer();
    s.append(clause.getType().name());
    s.append(" (");
    String sep = "";
    for (int index = 0; index < getConstituents().size(); index++) {
      Constituent constituent = getConstituents().get(index);
      s.append(sep);
      sep = ", ";
      switch (constituent.getType()) {
        case ACOMP:
          s.append("ACOMP");
          break;
        case ADVERBIAL:
          s.append("A");
          if (options != null) {
            switch (getFlag(index, options)) {
              case IGNORE:
                s.append("-");
                break;
              case OPTIONAL:
                s.append("?");
                break;
              case REQUIRED:
                s.append("!");
                break;
            }
          }
          break;
        case CCOMP:
          s.append("CCOMP");
          break;
        case COMPLEMENT:
          s.append("C");
          break;
        case DOBJ:
          s.append("O");
          break;
        case IOBJ:
          s.append("IO");
          break;
        case SUBJECT:
          s.append("S");
          break;
        case UNKOWN:
          s.append("?");
          break;
        case VERB:
          s.append("V");
          break;
        case XCOMP:
          s.append("XCOMP");
          break;
      }
      s.append(": ");
      if (!(constituent instanceof IndexedConstituent)) {
        s.append("\"");
      }
      s.append(constituent.rootString());
      if (constituent instanceof IndexedConstituent) {
        s.append("@");
        s.append(((IndexedConstituent) constituent).getRoot().index());
      } else {
        s.append("\"");
      }
    }
    s.append(")");
    return s.toString();
  }



  public Flag getFlagForIndex(int index) {
    return adverbialFlags.get(index);
  }

  public Map<Integer, Flag> getFlags() {
    return adverbialFlags;
  }

  public void setFlags(Map<Integer, Flag> flags) {
    this.adverbialFlags = flags;
  }

  /**
   * Determines the flag of the adverbial at position {@code index} in
   * {@link #adverbials}, i.e., whether the adverbial is required, optional,
   * or to be ignored.
   */
  public Flag getFlag(int index, Options options) {

    boolean first = true;
    for (int i : getAdverbials()) {
      if (i == index && isIgnoredAdverbial(i, options)) return Flag.IGNORE;
      else if (i == index && isIncludedAdverbial(i, options)) return Flag.REQUIRED;
      int adv = ((IndexedConstituent) getConstituents().get(i)).getRoot().index();
      if (getConstituents().get(getVerb()) instanceof IndexedConstituent && adv < ((IndexedConstituent) getConstituents().get(getVerb())).getRoot()
          .index() && !isRelativeAdverbial()) {
        if (i == index) {
          return Flag.OPTIONAL;
        }
      } else {
        if (i == index) {
          if (!first) return Flag.OPTIONAL;
          return !(Type.SVA.equals(getType()) || Type.SVOA.equals(getType())) ? Flag.OPTIONAL : Flag.REQUIRED;
        }
        first = false;
      }
    }
    return Flag.REQUIRED;
  }

  /**
   * Checks whether the adverbial at position {@code index} in
   * {@link #adverbials} is to be ignored by ClausIE.
   */
  private boolean isIgnoredAdverbial(int index, Options options) {
    Constituent constituent = getConstituents().get(index);
    String s;
    if (constituent instanceof IndexedConstituent) {
      IndexedConstituent indexedConstituent = (IndexedConstituent) constituent;
      IndexedWord root = indexedConstituent.getRoot();
      if (indexedConstituent.getSemanticGraph().hasChildren(root)) {
        // ||IndexedConstituent.sentSemanticGraph.getNodeByIndexSafe(root.index()
        // + 1) != null
        // &&
        // IndexedConstituent.sentSemanticGraph.getNodeByIndexSafe(root.index()
        // + 1).tag().charAt(0) == 'J') { //do not ignore if it modifies
        // an adjective. Adverbs can modify verbs or adjective no reason
        // to ignore them when they refer to adjectives (at lest in
        // triples). This is important in the case of adjectival
        // complements
        return false;
      }
      s = root.lemma();
    } else {
      s = constituent.rootString();
    }

    if (options.dictAdverbsIgnore.contains(s) || (options.processCcNonVerbs && options.dictAdverbsConj.contains(s))) return true;
    else return false;
  }

  /**
   * Checks whether the adverbial at position {@code index} in
   * {@link #adverbials} is required to be output by ClausIE (e.g., adverbials
   * indicating negation, such as "hardly").
   */
  private boolean isIncludedAdverbial(int index, Options options) {
    Constituent constituent = getConstituents().get(index);
    String s;
    if (constituent instanceof IndexedConstituent) {
      IndexedConstituent indexedConstituent = (IndexedConstituent) constituent;
      IndexedWord root = indexedConstituent.getRoot();
      if (indexedConstituent.getSemanticGraph().hasChildren(root)) {
        return false;
      }
      s = root.lemma();
    } else {
      s = constituent.rootString();
    }
    return options.dictAdverbsInclude.contains(s);
  }

  public int getVerb() {
    return verb;
  }

  public void setVerb(int verb) {
    this.verb = verb;
  }

  public void setRoot(IndexedWord root) {
    this.root = root;
  }

  public int getComplement() {
    return complement;
  }

  public void setComplement(int complement) {
    this.complement = complement;
  }

  public List<Constituent> getConstituents() {
    return constituents;
  }

  public void setConstituents(List<Constituent> constituents) {
    this.constituents = constituents;
  }

  public int getSubject() {
    return subject;
  }

  public void setSubject(int subject) {
    this.subject = subject;
  }

  public List<Integer> getIobjects() {
    return iobjects;
  }

  public void setIobjects(List<Integer> iobjects) {
    this.iobjects = iobjects;
  }

  public List<Integer> getDobjects() {
    return dobjects;
  }

  public void setDobjects(List<Integer> dobjects) {
    this.dobjects = dobjects;
  }

  public List<Integer> getCcomps() {
    return ccomps;
  }

  public List<Integer> getDeps() {
    return deps;
  }

  public void setCcomps(List<Integer> ccomps) {
    this.ccomps = ccomps;
  }

  public List<Integer> getXcomps() {
    return xcomps;
  }

  public void setXcomps(List<Integer> xcomps) {
    this.xcomps = xcomps;
  }

  public List<Integer> getAcomps() {
    return acomps;
  }

  public void setAcomps(List<Integer> acomps) {
    this.acomps = acomps;
  }

  public List<Integer> getAdverbials() {
    return adverbials;
  }

  public void setAdverbials(List<Integer> adverbials) {
    this.adverbials = adverbials;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public boolean getCop() {
    return cop;
  }

  public boolean getPassive() {
    return passive;
  }

  public IndexedWord getRoot() {
    return root;
  }

  public void setCop(boolean cop) {
    this.cop = cop;
  }

  public boolean isRelativeAdverbial() {
    return relativeAdverbial;
  }

  public void setRelativeAdverbial(boolean relativeAdverbial) {
    this.relativeAdverbial = relativeAdverbial;
  }
}
