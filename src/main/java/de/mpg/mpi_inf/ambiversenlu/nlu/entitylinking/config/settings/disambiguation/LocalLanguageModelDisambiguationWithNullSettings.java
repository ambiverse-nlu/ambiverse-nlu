package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;

import java.io.IOException;

/**
 * Preconfigured settings for the {@see Disambiguator} using only the 
 * language model similarity.
 *
 * Also does thresholding to determine out-of-knowledge-base / null entities.
 */
public class LocalLanguageModelDisambiguationWithNullSettings extends LocalLanguageModelDisambiguationSettings {

  private static final long serialVersionUID = -8458216249693970790L;

  public LocalLanguageModelDisambiguationWithNullSettings()
      throws MissingSettingException, NoSuchMethodException, ClassNotFoundException, IOException {
    super();
    setComputeConfidence(true);
    setNullMappingThreshold(0.05);
  }
}
