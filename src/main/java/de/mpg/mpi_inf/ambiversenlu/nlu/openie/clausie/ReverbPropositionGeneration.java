package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

import java.util.*;
import java.util.Map.Entry;

import static de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.ClauseDetector.excludeVertexes;

public class ReverbPropositionGeneration extends DefaultPropositionGenerator {

  /** Constructs a proposition generator
   * @param clausIE*/
  public ReverbPropositionGeneration(ClausIE clausIE) {
    super(clausIE);
  }

  //From Reverb
  //we normalize each relation phrase by removing inflection, auxiliary
  //verbs, adjectives, and adverbs.
  private static Set<String> EXCLUDE_POS_FROM_RELATIOM = new HashSet<String>();
  {
    EXCLUDE_POS_FROM_RELATIOM.add("RB");
    EXCLUDE_POS_FROM_RELATIOM.add("MD");
  }

  private static Set<String> ALLOWED_NOT_LEMMA = new HashSet<String>();
  {
    EXCLUDE_POS_FROM_RELATIOM.add("VBN");
    EXCLUDE_POS_FROM_RELATIOM.add("VBG");
  }

  @Override public void generate(List<Proposition> result, Clause clause, List<Boolean> include) {
    String relation;
    if(clause.getConstituents().get(clause.getVerb()) instanceof TextConstituent) {
      relation = clause.getConstituents().get(clause.getVerb()).rootString();
    } else {
      relation = ((IndexedConstituent) clause.getConstituents().get(clause.getVerb())).root.lemma();
    }
    clause.setDictRelation(relation);
    Clause tmp = rearrangeClause(clause, include);

    List<Proposition> tmpResult = new ArrayList<>();
    super.generate(tmpResult, tmp, include);
    for(Proposition p: tmpResult) {
      p.setDictRelation(tmp.getDictRelation());
    }
    result.addAll(tmpResult);
  }


  //It reprocess the clause according to the matchingd
  private Clause rearrangeClause(Clause clause, List<Boolean> include) {
    Clause nClause = clause.clone();
    List<Integer> toRemove = new ArrayList<>();
    for(int i = 0; i < include.size(); i++) {
      if(!include.get(i)) {
        toRemove.add(i);
      }
    }
    nClause.removeConstituents(toRemove);
    SemanticGraph semanticGraph = nClause.createSemanticGraph(true);
    Constituent constituent = nClause.getConstituents().get(nClause.getVerb());
    List<IndexedWord> constituentHeads = new ArrayList<>();
    for(Constituent c : nClause.getConstituents()) {
      if(c instanceof TextConstituent) {
        continue;
      }
      constituentHeads.add(((IndexedConstituent)c).getRoot());
    }
    if(constituent instanceof IndexedConstituent) {
      IndexedConstituent ic = (IndexedConstituent) constituent;
      Map<Integer, IndexedWord> included = new TreeMap<>();
      Map<Integer, IndexedWord> definite = new TreeMap<>();
      List<Integer> allowedOmitAlternative = new ArrayList<>();
      generateReverbRelation(nClause, ic.root, semanticGraph, included, definite, true, constituentHeads, allowedOmitAlternative);
      if(!definite.isEmpty()) {
        nClause = processClause(nClause, definite, semanticGraph);
        if(nClause == null) {
          nClause = clause.clone();
          nClause.removeConstituents(toRemove);
        }
      }
    }
    return nClause;
  }

  private Clause processClause(Clause clause, Map<Integer, IndexedWord> included, SemanticGraph semanticGraph) {
    Set<IndexedWord> heads = new HashSet<>();
    Set<IndexedWord> descendantsToInclude = new HashSet<>();
    for(Constituent c: clause.getConstituents()) {
      if(c instanceof TextConstituent) {
        continue;
      }
      IndexedConstituent ic = (IndexedConstituent) c;
      heads.add(ic.root);
    }

    List<Integer> toRemove = new ArrayList<>();
    IndexedConstituent relation = (IndexedConstituent) clause.getConstituents().get(clause.getVerb());
    for(int i = 0; i < clause.getConstituents().size(); i++) {
      if(toRemove.contains(i)) {
        continue;
      }
      Constituent c = clause.getConstituents().get(i);
      if(c instanceof TextConstituent) {
        continue;
      }
      IndexedConstituent ic = (IndexedConstituent) c;
      if(ic.equals(relation)) {
        ic.root.equals(relation.root);
        continue;
      }
      Set<IndexedWord> descendants = semanticGraph.descendants(ic.root);
      for(IndexedWord iw: included.values()) {
        if(ic.root.equals(iw)) {
          IndexedWord nRoot = getNewRoot(included, ic, heads, ic.root, semanticGraph);
          if(nRoot == null) {
            toRemove.add(i);
            ic.setRoot(nRoot);
            break;
          }
          Set<IndexedWord> nDescendants = semanticGraph.descendants(nRoot);
          for(IndexedWord diw: descendants) {
            if(diw.equals(nRoot) || nDescendants.contains(diw)) {
              continue;
            }
            descendantsToInclude.add(diw);
          }
          ic.setRoot(nRoot);
        } else if(descendants.contains(iw)) {
          ic.getExcludedVertexes().add(iw);
        }
      }
    }
    if(clause.getConstituents().size() - toRemove.size() < 3) {
      return null;
    }
    postProcessClause(clause, toRemove);
    relation.additionalVertexes.addAll(included.values());
    for(IndexedWord excluded: relation.excludedVertexes) {
      descendantsToInclude.removeAll(semanticGraph.descendants(excluded));
    }
    relation.additionalVertexes.addAll(descendantsToInclude);
   clause.setDictRelation(generateString(included));
    return clause;
  }

  private String generateString(Map<Integer, IndexedWord> included) {
    included = new TreeMap<>(included);
    StringJoiner sj = new StringJoiner(" ");
    Iterator<IndexedWord> it = included.values().iterator();
    while(it.hasNext()) {
      sj.add(it.next().lemma());
    }
    return sj.toString();
  }

  private void postProcessClause(Clause clause, List<Integer> toRemove) {
    clause.removeConstituents(toRemove);
    excludeVertexes(clause);
  }

  private IndexedWord getNewRoot(Map<Integer, IndexedWord> included,
      IndexedConstituent ic, Set<IndexedWord> heads, IndexedWord start, SemanticGraph semanticGraph) {
    List<SemanticGraphEdge> outEdges = semanticGraph.getOutEdgesSorted(start);
    IndexedWord nHead;
    for(SemanticGraphEdge edge: outEdges) {
      if(heads.contains(edge.getDependent())) {
        continue;
      }
      if(included.values().contains(edge.getDependent())
          || ic.excludedVertexes.contains(edge.getDependent())) {
        if((nHead = getNewRoot(included, ic, heads, edge.getDependent(), semanticGraph)) == null) {
          continue;
        } else {
          return nHead;
        }
      } else if(!included.values().contains(edge.getDependent())
          && edge.getDependent().get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("N")) {
        return edge.getDependent();
      }
    }
    return null;
  }

  public int generateReverbRelation(Clause clause, IndexedWord root, SemanticGraph semanticGraph,
      Map<Integer, IndexedWord> included, Map<Integer, IndexedWord> definite,
      boolean top, List<IndexedWord> constituentHeads, List<Integer> allowOmitAlternative) {
    included.put(root.beginPosition(), root);
    if(!isPresent(included, definite, allowOmitAlternative)) {
      included.remove(root.beginPosition());
      return 0;
    } else {
      List<SemanticGraphEdge> outGoingEdges = semanticGraph.getOutEdgesSorted(root);
      int result = 0;
      for (SemanticGraphEdge edge : outGoingEdges) {

        if (DpUtils.isAux(edge) //avoid auxiliaries
            || DpUtils.isNeg(edge) //avoid negations
            || DpUtils.isAnySubj(edge) //avoid subject
            || edge.getDependent().beginPosition() < edge.getGovernor().beginPosition() //avoid left edges (anything before the root)
            || DpUtils.isPunctuation(edge)) { //avoid punctuation
          continue;
        }
        String pos = edge.getDependent().get(CoreAnnotations.PartOfSpeechAnnotation.class);
        if (EXCLUDE_POS_FROM_RELATIOM.contains(pos)) {
          continue;
        }
        if(DpUtils.isAuxPass(edge)) {
          allowOmitAlternative.add(edge.getDependent().beginPosition());
        }
        int success = generateReverbRelation(clause, edge.getDependent(), semanticGraph, included, definite, false, constituentHeads, allowOmitAlternative);
        if(success == 0
            && constituentHeads.contains(edge.getDependent())
            && root.beginPosition() < edge.getDependent().beginPosition()) {
          return 0;
        }
        result = Math.max(result, success);
        if (edge.getDependent().get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("IN") && result < 2) {
          break;
        }
        boolean stop = false;
        if (top && success == 0) {
          for (Constituent c : clause.getConstituents()) {
            if(c instanceof TextConstituent) {
              continue;
            }
            IndexedConstituent ic = (IndexedConstituent) c;
            if (edge.getDependent().equals(ic.root) && (ic.type.equals(Constituent.Type.DOBJ)
                || ic.type.equals(Constituent.Type.ACOMP) || (ic.type.equals(Constituent.Type.COMPLEMENT) && clause.getCop()))) {
              definite.clear();
              stop = true;
            }
          }
        }
        if(stop) {
          break;
        }
      }
      return 1 + result;
    }
  }

  public boolean isPresent(Map<Integer, IndexedWord> included, Map<Integer, IndexedWord> includeDefinite, List<Integer> allowOmitAlternative) {
    Set<String> candidates = generateCandidates(included, allowOmitAlternative);
    List<String> definite = getRelationsFrom(candidates, clausIE.getOptions().dictReverbFull);
    if(!definite.isEmpty()) {
      includeDefinite.putAll(included);
      return true;
    }
    List<String> partial = getRelationsFrom(candidates, clausIE.getOptions().dictReverbPartial);
    if(!partial.isEmpty()) {
      return true;
    }
    return false;
  }

  private List<String> getRelationsFrom(Set<String> candidates, Dictionary relCollections) {
    List<String> result = new ArrayList<>();
    for(String candidate: candidates) {
      if(relCollections.contains(candidate)) {
        result.add(candidate);
      }
    }
    return result;
  }

  private Set<String> generateCandidates(Map<Integer, IndexedWord> included, List<Integer> allowOmitAlternative) {
    List<StringJoiner> variants = new ArrayList<>();
    variants.add(new StringJoiner(" "));
    for(Entry<Integer, IndexedWord> entry: included.entrySet()) {
      IndexedWord iw = entry.getValue();
      String pos = iw.get(CoreAnnotations.PartOfSpeechAnnotation.class);
      List<String> alternatives = new ArrayList<>();
      alternatives.add(iw.lemma());
      if(ALLOWED_NOT_LEMMA.contains(pos)) {
        alternatives.add(iw.word());
      }
      if(allowOmitAlternative.contains(iw.beginPosition())) {
        alternatives.add("");
      }
      int copies = alternatives.size() -1;
      List<StringJoiner> copy = new ArrayList<>();
      while(copies > 0) {
        for(StringJoiner variant: variants) {
          StringJoiner sj = new StringJoiner(" ");
          sj.add(variant.toString());
          copy.add(sj);
        }
        copies--;
      }
      variants.addAll(copy);
      int range = variants.size()/ alternatives.size();
      Iterator<String> it = alternatives.iterator();
      String word = it.next();
      for(int i = 0; i < variants.size(); i++) {
        if(i != 0 && i % range == 0) {
          word = it.next();
        }
        if(word.isEmpty()) {
          continue;
        }
        variants.get(i).add(word);
      }
    }
    Set<String> result = new HashSet<>();
    for(StringJoiner sj: variants) {
      result.add(sj.toString().trim());
    }
    return result;
  }





}
