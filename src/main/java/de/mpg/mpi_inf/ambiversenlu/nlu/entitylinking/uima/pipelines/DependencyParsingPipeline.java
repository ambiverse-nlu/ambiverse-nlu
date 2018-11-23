package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class DependencyParsingPipeline extends Pipeline {

  @Override void addSteps() {
    addstep("DE", Component.DE_TOKENIZER.name());
    addstep(Component.DE_TOKENIZER.name(), Component.DE_POS.name());
    addstep(Component.DE_POS.name(), Component.DE_DEPPARSE.name());
  }

  @Override public Set<Language> supportedLanguages() {
    Set<Language> supported = new HashSet<>();
    supported.add(Language.getLanguageForString("de"));
    return supported;
  }
}
