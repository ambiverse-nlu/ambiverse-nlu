package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset;



import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;

import java.io.Serializable;
import java.util.Collection;


public class FeatureSetFactory implements Serializable {
  private static final long serialVersionUID = 8014662621901022363L;

  /**
   * Create the features to be used based on the settings.
   *
   * @return FeatureSet
   */
  public static FeatureSet createFeatureSet(TrainingSettings.FeatureExtractor featureExtractor) {
    return createFeatureSet(featureExtractor, null);
  }
  
  /**
   * Create the features to be used based on the settings.
   *
   * @param o Payload parameter, will be passed to the FeatureSet when appropriate.
   * @return FeatureSet
   */
  @SuppressWarnings("unchecked")
  public static FeatureSet createFeatureSet(TrainingSettings.FeatureExtractor featureExtractor, Object o) {
    
    switch (featureExtractor) {
      case ENTITY_SALIENCE:
      case ANNOTATE_AND_ENTITY_SALIENCE:
        return new EntitySalienceFeatureSet((Collection<AidaEntity>) o);
      default:
      throw new IllegalStateException(
            "Case '" + featureExtractor + "' not covered, make sure all FeatureExtractor values are present.");
    }
  }
  
}
