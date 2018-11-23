package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public enum PipelineUtil {
  INSTANCE;

  private Set<Language> stanfordSupportedLanguages = new HashSet<>();

  PipelineUtil() {
    String[] languages = { "en", "es", "de", "zh"};

    for (String l : languages) {
      if (Language.isActiveLanguage(l)) {
        stanfordSupportedLanguages.add(Language.getLanguageForString(l));
      }
    }
  }

  public static Set<Language> getStanfordSupportedLanguages() {
    return INSTANCE.stanfordSupportedLanguages;
  }
}
