package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity count as feature.
 *
 * The count is relative to the total count of entity mentions in the document.
 */
public class EntityCountFeature extends EntityBasedFeature {

  Logger logger = LoggerFactory.getLogger(EntityCountFeature.class);

  public EntityCountFeature(Collection<AidaEntity> entityMentions) {
    super(entityMentions);
  }

  @Override
  protected Features.StaticFeatureRange getRange() {
    return Features.StaticFeatureRange.COUNT;
  }

  @Override
  public Map<Integer, Double> extract(JCas jCas) {
    Collection<AidaEntity> allMentions = JCasUtil.select(jCas, AidaEntity.class);
    double relativeFrequence = (double) entityMentions.size() / (double) allMentions.size();

    Map<Integer, Double> features = new HashMap<>();
    features.put(getId(), relativeFrequence);
    logger.debug("EntityCountFeature: " + relativeFrequence);
    return features;
  }
}
