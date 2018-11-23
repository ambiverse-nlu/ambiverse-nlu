package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Features set for predicting entity salience.
 */
public class EntitySalienceFeatureSet extends FeatureSet {

  private Collection<AidaEntity> entityMentions;

  public EntitySalienceFeatureSet(Collection<AidaEntity> entityMentions) {
    if (entityMentions == null) {
      entityMentions = new ArrayList<>();
    }
    this.entityMentions = entityMentions;
  }

  @Override
  public List<Feature> features() {
    List<Feature> features = new ArrayList<>();
    features.add(new EntityCountFeature(entityMentions));
    //features.add(new EntityFirstOccurrenceAbsoluteFeature(entityMentions));
    features.add(new EntityFirstOccurrenceRelativeFeature(entityMentions));
    features.add(new EntityHeadingFeature(entityMentions));
    features.add(new EntityFirstSentenceFeature(entityMentions));
    features.add(new EntityConfidenceFeature(entityMentions));
    //features.add(new EntityTemporalImportanceFeature(entityMentions));
    features.addAll(categoricalFeatures());
    return features;
  }

  @Override public List<CategoricalFeature> categoricalFeatures() {
    List<CategoricalFeature> features = new ArrayList<>();
    return features;
  }
}
