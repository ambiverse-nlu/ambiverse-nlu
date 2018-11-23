package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.*;

/** Handles the generation of propositions out of a given clause
 *
 * @date $ $
 * @version $ $ */
public abstract class PropositionGenerator {

  ClausIE clausIE;

  protected static final Set<Integer> inactiveConstituents = new HashSet<>();

  /** Relations to be excluded in every constituent of a clause except the verb */
  protected static final Set<GrammaticalRelation> EXCLUDE_RELATIONS;

  /** Relations to be excluded in the verb */
  protected static final Set<GrammaticalRelation> EXCLUDE_RELATIONS_VERB;

  protected static final Set<GrammaticalRelation> EXCLUDE_POS_ARGUMENTS;

  static {
    EXCLUDE_RELATIONS = new HashSet<GrammaticalRelation>();
    EXCLUDE_RELATIONS.add(EnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER);
    EXCLUDE_RELATIONS.add(EnglishGrammaticalRelations.APPOSITIONAL_MODIFIER);
    EXCLUDE_RELATIONS.add(EnglishGrammaticalRelations.PARATAXIS);
    EXCLUDE_RELATIONS.add(EnglishGrammaticalRelations.PUNCTUATION);

    EXCLUDE_RELATIONS_VERB = new HashSet<GrammaticalRelation>();
    EXCLUDE_RELATIONS_VERB.addAll(EXCLUDE_RELATIONS);
    EXCLUDE_RELATIONS_VERB.add(EnglishGrammaticalRelations.valueOf("dep")); //without this asome adverbs or auxiliaries will end up in the relation
    EXCLUDE_RELATIONS_VERB.add(EnglishGrammaticalRelations.PUNCTUATION);
    EXCLUDE_RELATIONS_VERB.add(EnglishGrammaticalRelations.SUBJECT);
    EXCLUDE_RELATIONS_VERB.add(EnglishGrammaticalRelations.COORDINATION);

    EXCLUDE_POS_ARGUMENTS = new HashSet<GrammaticalRelation>();
  }

  /** Constructs a proposition generator*/
  public PropositionGenerator(ClausIE clausIE) {
    this.clausIE = clausIE;
  }

  /** Generates propositions for a given clause*/
  public abstract void generate(List<Proposition> result, Clause clause, List<Boolean> include);

  /** Generates a textual representation of a given constituent plus a set of words*/
  private String generatePhrase(IndexedConstituent constituent, Collection<IndexedWord> words) {
    StringBuffer result = new StringBuffer();
    String separator = "";
    result.append(separator);
    if (constituent.isPrepositionalPhrase()) {
      if (clausIE.options.lemmatize) {
        result.append(constituent.getRoot().lemma());
      } else {
        result.append(constituent.getRoot().word());
      }
      separator = " ";
    }

    for (IndexedWord word : words) {
      result.append(separator);
      if (clausIE.options.lemmatize) {
        result.append(word.lemma());
      } else {
        result.append(word.word());
      }
      separator = " ";
    }
    return result.toString();
  }

  /** Generates a textual representation of a given constituent in a given clause*/
  public void generate(Clause clause, int constituentIndex, int factPosition, Proposition proposition) {
    Set<GrammaticalRelation> excludeRelations = EXCLUDE_RELATIONS;
    if (clause.getVerb() == constituentIndex) {
      excludeRelations = EXCLUDE_RELATIONS_VERB;
    }
    generate(clause, constituentIndex, factPosition, excludeRelations, Collections.<GrammaticalRelation>emptySet(), proposition);
  }

  /** Generates a textual representation of a given constituent in a given clause*/
  private void generate(Clause clause, int constituentIndex, int factPosition, Collection<GrammaticalRelation> excludeRelations,
      Collection<GrammaticalRelation> excludeRelationsTop, Proposition proposition) {
    Constituent constituent = clause.getConstituents().get(constituentIndex);
    if (constituent instanceof TextConstituent) {
      IndexedWord iw = new IndexedWord();
      iw.setWord(((TextConstituent) constituent).text());
      iw.setValue(((TextConstituent) constituent).text());
      proposition.constituents.put(factPosition, ((TextConstituent) constituent).text());
      proposition.addTokens(factPosition, iw);
      proposition.addHead(factPosition, iw);
    } else if (constituent instanceof IndexedConstituent) {
      IndexedConstituent iconstituent = (IndexedConstituent) constituent;
      proposition.addHead(factPosition, iconstituent.getRoot());
      SemanticGraph subgraph = clause.createSemanticGraph(false);
      DpUtils.removeEdges(subgraph, iconstituent.getRoot(), excludeRelations, excludeRelationsTop);
      Set<IndexedWord> words = new TreeSet<IndexedWord>(subgraph.descendants(iconstituent.getRoot()));
      for (IndexedWord v : iconstituent.getAdditionalVertexes()) {
        words.addAll(subgraph.descendants(v));
      }
      if (iconstituent.isPrepositionalPhrase()) words.remove(iconstituent.getRoot());
      proposition.constituents.put(factPosition, generatePhrase(iconstituent, words));
      proposition.addTokens(factPosition, words);
    } else {
      throw new IllegalArgumentException();
    }
  }
}
