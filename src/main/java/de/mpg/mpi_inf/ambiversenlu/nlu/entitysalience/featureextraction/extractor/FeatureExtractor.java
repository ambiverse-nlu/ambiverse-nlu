package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.EntityInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.TrainingInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import org.apache.uima.jcas.JCas;

import java.util.List;


public interface FeatureExtractor {
  
  public List<TrainingInstance> getTrainingInstances(JCas jCas, TrainingSettings.FeatureExtractor extractor, int positiveInstanceScalingFactor) throws Exception;

  public List<EntityInstance> getEntityInstances(JCas jCas, TrainingSettings.FeatureExtractor extractor) throws Exception;
}
