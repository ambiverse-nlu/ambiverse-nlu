package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class TokenizationPipeline extends Pipeline {

  @Override void addSteps() {
    for (Language language : Language.activeLanguages()) {
      addstep(language.name().toUpperCase(), language.name().toUpperCase() + "_TOKENIZER");
    }
  }

  @Override public Set<Language> supportedLanguages() {
    return new HashSet<>(Language.activeLanguages());
  }
}
