package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class EvaluationOnlyNEDPipeline extends Pipeline {

  @Override void addSteps() {
    for (Language language : Language.activeLanguages()) {
      addstep(language.name().toUpperCase(), Component.AIDA_NO_RESULTS.name());
    }
  }

  @Override public Set<Language> supportedLanguages() {
    return new HashSet<>(Language.activeLanguages());
  }
}
