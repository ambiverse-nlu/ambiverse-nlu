package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity count as feature.
 */
public class EntityFirstOccurrenceRelativeFeature extends EntityBasedFeature {

  public EntityFirstOccurrenceRelativeFeature(Collection<AidaEntity> entityMentions) {
    super(entityMentions);
  }

  @Override protected Features.StaticFeatureRange getRange() {
    return Features.StaticFeatureRange.FIRST_OCCURRENCE_RELATIVE;
  }

  @Override public Map<Integer, Double> extract(JCas jCas) {
    Map<Integer, Double> features = new HashMap<>();
    int totalLength = jCas.getDocumentText().length();
    double firstOffsetRelative = (double) getFirstOffset() / (double) totalLength;
    features.put(getId(), firstOffsetRelative);
    return features;
  }
}
