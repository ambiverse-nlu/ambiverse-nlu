package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.FeatureExtractionException;
import org.apache.uima.jcas.JCas;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * Feature for machine learning.
 */
public abstract class Feature implements Serializable {
  private static final long serialVersionUID = 3014515810672727863L;

  public enum FeatureValueComputation {
    FREQUENCY,
    BINARY
  }
  
  public enum FeatureValueNormalization {
    NONE,
    RELATIVE,
    GLOBAL
  }

  //Default values FREQUENCY and RELATIVE
  private FeatureValueComputation computation = FeatureValueComputation.FREQUENCY;
  private FeatureValueNormalization normalization = FeatureValueNormalization.RELATIVE;
  
  /**
   * Extracts the features.
   *
   * @param jCas  JCas containing all required data.
   * @return  Map of feature-id->feature-values.
   * @throws Exception
   */
  public abstract Map<Integer, Double> extract(JCas jCas) throws FeatureExtractionException, EntityLinkingDataAccessException;
  
  /**
   * Extracts the features.
   *
   * @param internalID  Internal ID of the target class
   * @param jCas  JCas containing all required data.
   * @return  Map of feature-id->feature-values.
   * @throws FeatureExtractionException
   */
  public Map<Integer, Double> extract(Integer internalID, JCas jCas) throws FeatureExtractionException, EntityLinkingDataAccessException {
    return extract(jCas);
  }
  
  public FeatureValueComputation getComputation() {
    return computation;
  }
  
  public void setComputation(FeatureValueComputation computation) {
    this.computation = computation;
  }

  public Optional<FeatureValueNormalization> getNormalization() {
    return normalization == null ? Optional.empty() : Optional.of(normalization);
  }
  
  public void setNormalization(FeatureValueNormalization normalization) {
    this.normalization = normalization;
  }

  /**
   *
   * @return Maximum feature id of this feature (range).
   */
  public abstract int getMaxFeatureId();

  /**
   * Returns the full string id identifying the collection + feature.
   *
   * @param collectionId  Identifier for collection - make sure this is the same only if the exact same documents are present.
   * @param featureClass  Class for which the id should be generated.
   * @return
   */
  public static String getCollectionFeatureId(String collectionId, Class<? extends Feature> featureClass) {
    return collectionId + "_" + featureClass.getCanonicalName();
  }
}
