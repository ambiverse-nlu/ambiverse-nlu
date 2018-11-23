package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.Map;

public class FilterConceptsByNothing extends JCasAnnotator_ImplBase {
  
  public static final String FILTER_NEs = "filterNEs";
  @ConfigurationParameter(name = FILTER_NEs, mandatory = true)
  private boolean filterNEs;

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    Mentions addedMentionsC = Mentions.getConceptMentionsFromJCas(aJCas);
    Mentions addedMentionsNE;
    if (filterNEs) {
      addedMentionsNE = Mentions.getNeMentionsFromJCas(aJCas);
    }
    else {
      addedMentionsNE = new Mentions();
    }
    Collection<ConceptMentionCandidate> conceptMentionCandidatesJcas = JCasUtil.select(aJCas, ConceptMentionCandidate.class);
    
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
    
    for (ConceptMentionCandidate cc : conceptMentionCandidatesJcas) {
      if (added_before.contains(cc.getBegin()) || added_before.contains(cc.getEnd())) {
        continue;
      }
      
      ConceptMention conceptMention = new ConceptMention(aJCas, cc.getBegin(), cc.getEnd());
      conceptMention.setConcept(cc.getConceptCandidate());
      conceptMention.addToIndexes();
    }
  }

}
