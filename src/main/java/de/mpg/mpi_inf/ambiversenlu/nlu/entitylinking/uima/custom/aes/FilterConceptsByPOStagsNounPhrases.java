package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.*;


public class FilterConceptsByPOStagsNounPhrases extends JCasAnnotator_ImplBase {

  public static final String FILTER_NEs = "filterNEs";
  @ConfigurationParameter(name = FILTER_NEs, mandatory = true)
  private boolean filterNEs;
  
  public static Comparator<ConceptMentionCandidate> lengthComparator = new Comparator<ConceptMentionCandidate>(){
    
    @Override
    public int compare(ConceptMentionCandidate o1, ConceptMentionCandidate o2) {
      if (o2.getEnd() > o1.getEnd()) 
        return 1;
      else if (o2.getEnd() < o1.getEnd())
        return -1;
      else
        return ((o2.getEnd() - o2.getBegin()) - (o1.getEnd() - o1.getBegin()));
    }
  };

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Tokens tokens = Tokens.getTokensFromJCas(jCas);
    Mentions addedMentionsC = Mentions.getConceptMentionsFromJCas(jCas);
    Mentions addedMentionsNE;
    if (filterNEs) {
      addedMentionsNE = Mentions.getNeMentionsFromJCas(jCas);
    }
    else {
      addedMentionsNE = new Mentions();
    }
    
    Collection<ConceptMentionCandidate> conceptMentionCandidatesJcas = JCasUtil.select(jCas, ConceptMentionCandidate.class);
    List<ConceptMentionCandidate> ccMentionsSorted = new ArrayList<>(conceptMentionCandidatesJcas);
    Collections.sort(ccMentionsSorted, lengthComparator);
    
    Map<Integer, Map<Integer, String>> tokensPOStags = new HashMap<Integer, Map<Integer,String>>();
    for (Token t: tokens) {
      tokensPOStags.putIfAbsent(t.getBeginIndex(), new HashMap<>());
      tokensPOStags.get(t.getBeginIndex()).put(t.getEndIndex(), t.getPOS());
    }
    
    RangeSet<Integer> added_before = TreeRangeSet.create();
    for (Map<Integer, Mention> innerMap : addedMentionsC.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        added_before.add(Range.closed(m.getCharOffset(), m.getCharOffset() + m.getCharLength() - 1));
      }
    }
    for (Map<Integer, Mention> innerMap : addedMentionsNE.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (m.getCharLength() != 0) { // workaround for potential issue in knowner
          added_before.add(Range.closed(m.getCharOffset(), m.getCharOffset() + m.getCharLength() - 1));
        }
      }
    }
    
    RangeSet<Integer> added = TreeRangeSet.create();
    //ccMentionsSorted is sorted based on: firstly end position of the mention and secondly the length of the mention.
    for (ConceptMentionCandidate cc : ccMentionsSorted) {
      if (added_before.contains(cc.getBegin()) || added_before.contains(cc.getEnd())) {
        continue;
      }
      
      // Avoid overlaps.
      if (added.contains(cc.getBegin()) || added.contains(cc.getEnd())) {
        continue;
      }
      
      //Exists at least one noun in the phrase (POS tags).
      if (!isNounPhrase(tokensPOStags, cc.getBegin(), cc.getEnd())) {
        continue;
      }
      
      ConceptMention conceptMention = new ConceptMention(jCas, cc.getBegin(), cc.getEnd());
      conceptMention.setConcept(cc.getConceptCandidate());
      conceptMention.addToIndexes();
      added.add(Range.closed(cc.getBegin(), cc.getEnd()));
      
    }    
  }

  public static boolean isNounPhrase(Map<Integer, Map<Integer, String>> tokenPos, int begin, int end) {
    for (int s = begin; s<=end; s++) {
      for (int e = begin; e<=end; e++) {
        if (tokenPos.get(s) != null && tokenPos.get(s).get(e) != null) {
          if (tokenPos.get(s).get(e).toLowerCase().startsWith("n")) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
