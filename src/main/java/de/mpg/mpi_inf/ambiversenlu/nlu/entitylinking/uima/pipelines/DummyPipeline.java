package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.Collections;
import java.util.Set;

public class DummyPipeline extends Pipeline {

  @Override void addSteps() {
  }

  @Override public Set<Language> supportedLanguages() {
    return Collections.EMPTY_SET;
  }
}
