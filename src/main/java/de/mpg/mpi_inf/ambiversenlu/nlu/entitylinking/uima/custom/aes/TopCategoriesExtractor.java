package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.*;

public class TopCategoriesExtractor extends JCasAnnotator_ImplBase {
  
  static final int MAX_TOP_TYPE = 10;
  private static final Set<String> EXCLUDE_TYPES = 
      new HashSet<>(Arrays.asList("<wikicat_People>", "<wikicat_Abbreviations>", "<wikicat_Language>", "<wikicat_Academic_disciplines>",
                                  "<wikicat_Punctuation>", "<wikicat_Pronouns>", "<wikicat_Parts_of_speech>")); 
  private static TIntObjectHashMap<Type> ALL_WIKIPEDIA_CATEGORY_TYPES;
  
  @Override
  public void initialize(org.apache.uima.UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    
    try {
      ALL_WIKIPEDIA_CATEGORY_TYPES = DataAccess.getAllWikiCategoryTypes();
    } catch (EntityLinkingDataAccessException e) {
      System.err.println("Could not get Wikipedia Types from database.");
    }
  }
    
    
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException{
    Integer runningId = RunningTimer.recordStartTime("TopCategoriesExtractor");
    Language language = Language.getLanguageForString(aJCas.getDocumentLanguage());
    Collection<ConceptMentionCandidate> conceptMentionCandidatesJcas = JCasUtil.select(aJCas, ConceptMentionCandidate.class);
    Mentions addedMentionsNE = Mentions.getNeMentionsFromJCas(aJCas);
    RangeSet<Integer> added_before = TreeRangeSet.create();
    for (Map<Integer, Mention> innerMap : addedMentionsNE.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (m.getCharLength() != 0) { // workaround for issue in knowner.
          added_before.add(Range.closed(m.getCharOffset(), m.getCharOffset() + m.getCharLength() - 1));
        }
      }
    }
    
    Set<String> mentions = new HashSet<>();
    for (ConceptMentionCandidate cc : conceptMentionCandidatesJcas) {
      if (added_before.contains(cc.getBegin()) || added_before.contains(cc.getEnd())) {
        continue;
      }
      mentions.add(cc.getConceptCandidate());
    }
    
    Map<String, int[]> mentionTypes;
    try {
      mentionTypes = DataAccess.getCategoryIdsForMentions(mentions, language, false);
    } catch (EntityLinkingDataAccessException e) {
      throw new AnalysisEngineProcessException(e);
    }
    
    Set<Integer> topTypes = getTopWikipediaTypes(mentionTypes, MAX_TOP_TYPE);
    
    for (Integer typeId : topTypes) {
      WikiType type = new WikiType(aJCas);
      type.setId(typeId);
      type.setName(ALL_WIKIPEDIA_CATEGORY_TYPES.get(typeId).getName());
      type.setKnowledgebase(ALL_WIKIPEDIA_CATEGORY_TYPES.get(typeId).getKnowledgeBase());
      type.addToIndexes();
    }
    RunningTimer.recordEndTime("TopCategoriesExtractor", runningId);
  }

  static Set<Integer> getTopWikipediaTypes(Map<String, int[]> mentionTypes, int n) {
    Map<Integer, Integer> typeCounts = new TreeMap<Integer, Integer>();
    
    for (String mention : mentionTypes.keySet()) {
      int[] a = mentionTypes.get(mention);
      for (int t : a) {
        if (! ALL_WIKIPEDIA_CATEGORY_TYPES.containsKey(t)) {
          continue;
        }
        String typeName = ALL_WIKIPEDIA_CATEGORY_TYPES.get(t).getName();
        if (EXCLUDE_TYPES.contains(typeName)) {
          continue;
        }
        if (typeName.matches("<wikicat_People_associated_with.*>")) {
          continue;
        }

        typeCounts.merge(t, 1,  (oldValue, one) -> oldValue + one);
      }
    }
    
    SortedSet<Map.Entry<Integer, Integer>> sortedTypeCounts = entriesSortedByValues(typeCounts);
    Map<Integer, Integer> top = new HashMap<>();
    
    int lastEntry = -1;
    for (Map.Entry<Integer, Integer> e : sortedTypeCounts) {
      Integer count = e.getValue();
      if (top.size() < n) {
        top.put(e.getKey(), count);
        lastEntry = count;
      }
      else {
        // If add more than N top types if the counts are equal.
        if (count == lastEntry) {
          top.put(e.getKey(), count);
        }
        else {
          break;
        }
      }
    }
    
    System.out.println("TOP TYPE EX " + top.keySet());
    return top.keySet();
    
  }
  
  static <K,V extends Comparable<? super V>>
  SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
      SortedSet<Map.Entry<K,V>> sortedEntries = 
          new TreeSet<Map.Entry<K,V>>(
          new Comparator<Map.Entry<K,V>>() {
              @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                  int res = e2.getValue().compareTo(e1.getValue());
                  return res != 0 ? res : 1;
              }
          }
      );
      sortedEntries.addAll(map.entrySet());
      return sortedEntries;
  }

}
