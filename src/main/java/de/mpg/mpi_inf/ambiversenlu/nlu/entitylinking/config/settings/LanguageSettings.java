package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.io.Serializable;

/**
 * This class contains TrainingSettings specific for a Language. At the moment the fuzzy configuration is hard coded (for German is always on)
 */
public class LanguageSettings implements Serializable {

  private boolean fuzzyEntityMatching = false;

  private double fuzzyLsHMinsim = 0.8;

  private Language language; //This is needed for lsh fuzzy maching

  private LanguageSettings(Language language) {
    this.language = language;
    setFuzzyMatching(language);
  }

  public Language getLanguage() throws AidaUnsupportedLanguageException {
    if (language == null) {
      throw new AidaUnsupportedLanguageException("Language not set in the dissambiguation settings");
    }
    return language;
  }

  public void setFuzzyMatching(Language language) {
    if (language == null || !language.isGerman()) {
      fuzzyEntityMatching = false;
    } else {
      fuzzyEntityMatching = true;
      fuzzyLsHMinsim = EntityLinkingConfig.getDouble(EntityLinkingConfig.DICTIONARY_FUZZY_LSH_MINSIM);
    }
  }

  public boolean getFuzzyMatching() {
    return fuzzyEntityMatching;
  }

  public double getFuzzyLshMinsim() {
    return fuzzyLsHMinsim;
  }

  public void setFuzzyLsHMinsim(double fuzzyLsHMinsim) {
    this.fuzzyLsHMinsim = fuzzyLsHMinsim;
  }

  public static class LanguageSettingsFactory {

    public static LanguageSettings getLanguageSettingsForLanguage(Language language) {
      return new LanguageSettings(language);
    }

  }

}
