package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.Collections;
import java.util.Set;

public class LanguageDetectionPipeline extends Pipeline {

  @Override void addSteps() {
    addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
  }

  @Override public Set<Language> supportedLanguages() {
    return Collections.EMPTY_SET;
  }
}
