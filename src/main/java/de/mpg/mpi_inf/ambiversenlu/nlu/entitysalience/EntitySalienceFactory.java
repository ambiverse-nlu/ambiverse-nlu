package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.EntitySalienceProcessorAnalysisEngineSpark;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkSerializableAnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.Serializable;

public class EntitySalienceFactory implements Serializable {
  private static final long serialVersionUID = 4535984496154338977L;

  public static SparkSerializableAnalysisEngine createEntitySalienceEntityAnnotator( TrainingSettings.EntitySalienceEntityAnnotator annotator)
      throws ResourceInitializationException
  {
    switch (annotator) {
      case DISAMBIGUATION:
        return new EntitySalienceProcessorAnalysisEngineSpark();
      default:
        throw new IllegalStateException(
            "Case '" + annotator + "' not covered, make sure all EntitySalienceEntityAnnotator values are present.");
    }
  }
}