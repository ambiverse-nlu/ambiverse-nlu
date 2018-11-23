package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature.CategoricalFeature;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature.Feature;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class FeatureSet implements Serializable {
  private static final long serialVersionUID = 5303489874731463484L;

  private boolean preprocessing;
  
  //private TrainingSettings trainingSettings;

  
  /**
   * NOT THREADSAFE!
   *
   * @return All features used. 
   */
  public abstract List<Feature> features() throws ResourceInitializationException;

  /**
   *
   * @return Categorical features used - subset of features()
   */
  public List<CategoricalFeature> categoricalFeatures() {
    List<CategoricalFeature> features = new ArrayList<>();
    return features;
  }

  /**
   *
   * @return Maximum ID of any feature used in this feature set.
   */
  public int maxFeatureId() throws ResourceInitializationException {
    int maxId = 0;
    for (Feature f : features()) {
      maxId = Math.max(maxId, f.getMaxFeatureId());
    }
    return maxId;
  }

  /**
   * The feature vector needs to be +1 of the max feature id.
   * @return  Feature Vector size.
   * @throws ResourceInitializationException 
     */
  public int getFeatureVectorSize() throws ResourceInitializationException {
    return maxFeatureId() + 1;
  }

  protected boolean isPreprocessing() {
    return preprocessing;
  }

  public void setPreprocessing(boolean preprocessing) {
    this.preprocessing = preprocessing;
  }
  
//  public TrainingSettings getTrainingSettings() {
//    return trainingSettings;
//  }
//
//  public void setTrainingSettings(TrainingSettings trainingSettings) {
//    this.trainingSettings = trainingSettings;
//  }
}
