package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extracts feature if entity in heading.
 */
public class EntityFirstSentenceFeature extends EntityBasedFeature {

  public EntityFirstSentenceFeature(Collection<AidaEntity> entityMentions) {
    super(entityMentions);
  }

  @Override protected Features.StaticFeatureRange getRange() {
    return Features.StaticFeatureRange.IN_FIRST_SENTENCE;
  }

  @Override public Map<Integer, Double> extract(JCas jCas) {
    Map<Integer, Double> features = new HashMap<>();

    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
    // Check if the first real sentence contains the entity.
    int firstSentenceBegin = 0;
    int firstSentenceEnd = 0;
    //if (sentences.size() > 3) {
      Iterator<Sentence> itr = sentences.iterator();
      //itr.next();
      //itr.next();
      Sentence sentence = itr.next();
      firstSentenceBegin = sentence.getBegin();
      firstSentenceEnd = sentence.getEnd();
    //}

    boolean inFirstSentence = false;
    for (AidaEntity ae : entityMentions) {
      int offset = ae.getBegin();
      inFirstSentence = (offset >= firstSentenceBegin) && (offset < firstSentenceEnd);
      if (inFirstSentence) {
        break;
      }
      if (ae.getBegin() > firstSentenceEnd) {
        break;
      }
    }


    features.put(getId(), inFirstSentence ? 1.0 : 0.0);
    return features;
  }
}
