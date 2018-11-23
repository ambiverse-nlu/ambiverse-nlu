package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.EntitySalienceAnnotatorAndFeatureExtractorSpark;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.EntitySalienceFeatureExtractorSpark;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.FeatureExtractorSpark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Creates different feature extractors.
 * For now only Entity Salience feature extractor is supported.
 */
public class FeatureExtractionFactory implements Serializable {
  private static final long serialVersionUID = -1669106188875564300L;

  private static Logger logger = LoggerFactory.getLogger(FeatureExtractionFactory.class);

  /**
   * Create specific feature extractor based on the training settings.
   * @param trainingSettings
   * @return
   */
  public static FeatureExtractorSpark createFeatureExtractorSparkRunner(TrainingSettings trainingSettings) {
    switch (trainingSettings.getFeatureExtractor()) {
      case ENTITY_SALIENCE:
            return new EntitySalienceFeatureExtractorSpark(trainingSettings);
        case ANNOTATE_AND_ENTITY_SALIENCE:
            return new EntitySalienceAnnotatorAndFeatureExtractorSpark(trainingSettings);
      default:
        throw new IllegalStateException(
            "Case '" + trainingSettings.getFeatureExtractor() + "' not covered, make sure all FeatureExtractor values are present.");
    }
  }
}