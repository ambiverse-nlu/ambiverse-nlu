package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction;

/**
 * Exception thrown by FeatureExtractors.
 */
public class FeatureExtractionException extends Exception {
  private static final long serialVersionUID = -1776065680667984418L;

  public FeatureExtractionException(Throwable cause) {
    super(cause);
  }
  
  public FeatureExtractionException(String message) {
    super(message);
  }
}
