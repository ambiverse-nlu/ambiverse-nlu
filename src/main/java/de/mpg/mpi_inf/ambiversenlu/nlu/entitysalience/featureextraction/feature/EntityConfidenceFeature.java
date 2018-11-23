package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity confidence as feature.
 *
 * The confidence is the maximum confidence of any mention in the document.
 */
public class EntityConfidenceFeature extends EntityBasedFeature {

  Logger logger = LoggerFactory.getLogger(EntityConfidenceFeature.class);

  public EntityConfidenceFeature(Collection<AidaEntity> entityMentions) {
    super(entityMentions);
  }

  @Override
  protected Features.StaticFeatureRange getRange() {
    return Features.StaticFeatureRange.CONFIDENCE;
  }

  @Override
  public Map<Integer, Double> extract(JCas jCas) {
    double maxConfidence = 0.0;
    for (AidaEntity ae : entityMentions) {
      maxConfidence = Math.max(maxConfidence, ae.getScore());
    }

    Map<Integer, Double> features = new HashMap<>();
    features.put(getId(), maxConfidence);
    logger.debug("EntityConfidenceFeature: " + maxConfidence);
    return features;
  }
}
