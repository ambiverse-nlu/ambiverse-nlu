package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import java.util.Map;

/**
 * Holds an entity together with feature-values pairs
 */
public class EntityInstance extends FeatureValueInstance {
  private String entityId;

  public EntityInstance(String entityId, Map<Integer, Double> featureValues) {
    super(featureValues);
    this.entityId = entityId;
  }

  public String getEntityId() {
    return entityId;
  }
}
