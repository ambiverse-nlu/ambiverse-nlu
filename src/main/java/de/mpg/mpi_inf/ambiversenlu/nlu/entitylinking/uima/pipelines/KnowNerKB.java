package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;

import java.util.Set;



public class KnowNerKB extends Pipeline {

  @Override void addSteps() {
    addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
    for (Language language : supportedLanguages()) {
      String upperCaseLanguage = language.name().toUpperCase();
      String next = upperCaseLanguage + "_TOKENIZER";
      addstep(upperCaseLanguage, next);
      if (KnowNERLanguage.requiresLemma(language)) {
        addstep(next, upperCaseLanguage + "_LEMMATIZER");
        next = upperCaseLanguage + "_LEMMATIZER";
      }
      addstep(next, upperCaseLanguage + "_POS");
      addstep(upperCaseLanguage + "_POS", Component.KNOW_NER_KB.name());
    }
  }

  @Override public Set<Language> supportedLanguages() {
    return KnowNERLanguage.supportedLanguages();
  }
}
