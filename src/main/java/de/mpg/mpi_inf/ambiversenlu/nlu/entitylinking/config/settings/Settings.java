package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import java.io.Serializable;

/**
 * Combined PreparationSettings and DisambiguationSettings, useful for
 * a calling API.
 */
public class Settings implements Serializable {

  public enum TECHNIQUE {
    LOCAL, LOCAL_ITERATIVE, GRAPH, CHAKRABARTI
  }

  public enum ALGORITHM {
    COCKTAIL_PARTY, COCKTAIL_PARTY_SIZE_CONSTRAINED, SIMPLE_GREEDY, RANDOM_WALK
  }

  private static final long serialVersionUID = -6602287193597852191L;

  private DisambiguationSettings disambiguationSettings = null;

  public DisambiguationSettings getDisambiguationSettings() {
    return disambiguationSettings;
  }

  public void setDisambiguationSettings(DisambiguationSettings disambiguationSettings) {
    this.disambiguationSettings = disambiguationSettings;
  }
}
