package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.list.array.TIntArrayList;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilterConceptsByCategories extends JCasAnnotator_ImplBase {

  private Logger logger = LoggerFactory.getLogger(FilterConceptsByCategories.class);
  
  public static final String FILTER_NEs = "filterNEs";
  @ConfigurationParameter(name = FILTER_NEs, mandatory = true)
  private boolean filterNEs;
  
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    Language language = Language.getLanguageForString(aJCas.getDocumentLanguage());
    Mentions addedMentionsC = Mentions.getConceptMentionsFromJCas(aJCas);
    Mentions addedMentionsNE;
    if (filterNEs) {
      addedMentionsNE = Mentions.getNeMentionsFromJCas(aJCas);
    }
    else {
      addedMentionsNE = new Mentions();
    }
    
    RangeSet<Integer> added_before = TreeRangeSet.create();
    for (Map<Integer, Mention> innerMap : addedMentionsC.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        added_before.add(Range.closed(m.getCharOffset(), m.getCharOffset() + m.getCharLength() - 1));
      }
    }
    for (Map<Integer, Mention> innerMap : addedMentionsNE.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (m.getCharLength() != 0) {// workaround for potential issues in knowner
          added_before.add(Range.closed(m.getCharOffset(), m.getCharOffset() + m.getCharLength() - 1));
        }
      }
    }
    
    Collection<WikiType> topTypes = JCasUtil.select(aJCas, WikiType.class);
    
    Collection<ConceptMentionCandidate> conceptMentionCandidatesJcas = JCasUtil.select(aJCas, ConceptMentionCandidate.class);
    
    Set<String> mentions = new HashSet<>();
    for (ConceptMentionCandidate cc : conceptMentionCandidatesJcas) {
      mentions.add(cc.getConceptCandidate());
    }
    Map<String, int[]> mentionTypes;
    try {
      mentionTypes = DataAccess.getCategoryIdsForMentions(mentions, language, false);
    } catch (EntityLinkingDataAccessException e) {
      throw new AnalysisEngineProcessException(e);
    }
    
    for (ConceptMentionCandidate cc : conceptMentionCandidatesJcas) {
      if (added_before.contains(cc.getBegin()) || added_before.contains(cc.getEnd())) {
        continue;
      }
      
      TIntArrayList types = new TIntArrayList();
      if (mentionTypes.containsKey(cc.getConceptCandidate())) {
        types.addAll(mentionTypes.get(cc.getConceptCandidate()));
      }
      boolean inserted = false;
      for (WikiType topType : topTypes) {
        if (types.contains(topType.getId())) {
          ConceptMention conceptMention = new ConceptMention(aJCas, cc.getBegin(), cc.getEnd());
          conceptMention.setConcept(cc.getConceptCandidate());
          conceptMention.addToIndexes();
          inserted = true;
          break;
        }
      }
      if (!inserted) {
        logger.debug("ConceptCandidate filtered out: " + cc.getCoveredText() + "-" + cc.getConceptCandidate());
      }
    }
  }


}
