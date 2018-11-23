package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.Serializable;

public abstract class FeatureExtractorSpark implements Serializable {
  private static final long serialVersionUID = -335097469898167366L;
  
  private TrainingSettings trainingSettings;
  

  @Deprecated
  public abstract JavaRDD<LabeledPoint> extract(JavaSparkContext sc, JavaRDD<SCAS> documents)
      throws ResourceInitializationException;

  public abstract DataFrame extract(JavaSparkContext jsc, JavaRDD<SCAS> documents, SQLContext sqlContext) throws ResourceInitializationException;

  public TrainingSettings getTrainingSettings() {
    return trainingSettings;
  }
  
  public void setTrainingSettings(TrainingSettings trainingSettings) {
    this.trainingSettings = trainingSettings;
  }
}