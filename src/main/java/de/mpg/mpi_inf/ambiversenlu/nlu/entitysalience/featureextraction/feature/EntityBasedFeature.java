package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;

import java.util.Collection;

/**
 * Feature extracting information from Entities.
 */
public abstract class EntityBasedFeature extends StaticFeature {

  final protected Collection<AidaEntity> entityMentions;

  public EntityBasedFeature(Collection<AidaEntity> entityMentions) {
    this.entityMentions = entityMentions;
  }

  protected int getFirstOffset() {
    int minBegin = Integer.MAX_VALUE;
    for (AidaEntity e : entityMentions) {
      int begin = e.getBegin();
      minBegin = Math.min(begin, minBegin);
    }
    // Will be cast to Double.
    return minBegin;
  }
}
