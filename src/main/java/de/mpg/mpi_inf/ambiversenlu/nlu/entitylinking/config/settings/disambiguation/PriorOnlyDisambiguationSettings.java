package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.Settings.TECHNIQUE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;

/**
 * Preconfigured settings for the {@see Disambiguator} using only the 
 * mention-entity prior.
 */
public class PriorOnlyDisambiguationSettings extends DisambiguationSettings {

  private static final long serialVersionUID = 2212272023159361340L;

  public PriorOnlyDisambiguationSettings() throws MissingSettingException {
    setDisambiguationTechnique(TECHNIQUE.LOCAL);

    SimilaritySettings priorSettings = new SimilaritySettings(null, null, 1.0);
    priorSettings.setIdentifier("Prior");
    setSimilaritySettingsNE(priorSettings);
    setSimilaritySettingsC(priorSettings);
  }
}


