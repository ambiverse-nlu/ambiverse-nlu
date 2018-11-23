package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.Set;

public class EntitySalienceStanford extends Pipeline {
  
  @Override void addSteps() {
    addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
    for (Language language : supportedLanguages()) {
      if (language.isGerman()) {
        generateGermanSteps();
      } else if (language.isChinese()) {
        generateChineseSteps();
      } else if (EntityLinkingConfig.getLanguages().contains(language.name())) {
        String upperCaseLanguage = language.name().toUpperCase();
        String next = upperCaseLanguage + "_TOKENIZER";
        addstep(upperCaseLanguage, next);

        addstep(next, upperCaseLanguage + "_POS");
        addstep(upperCaseLanguage + "_POS", upperCaseLanguage + "_NER");
        addstep(upperCaseLanguage + "_NER", Component.AIDA_NO_RESULTS.name());
        addstep(Component.AIDA_NO_RESULTS.name(), Component.SALIENCE.name());
      } else {
        throw new UnsupportedOperationException("Language " + language + " is not supported!");
      }
    }
  }

  @Override public Set<Language> supportedLanguages() {
    return PipelineUtil.getStanfordSupportedLanguages();
  }

  private void generateGermanSteps() {
    addstep("DE", Component.DE_TOKENIZER.name());
    addstep(Component.DE_TOKENIZER.name(), Component.DE_POS.name());
    addstep(Component.DE_POS.name(), Component.DE_NER.name());
    addstep(Component.DE_NER.name(), Component.DE_NER2.name());
    addstep(Component.DE_NER2.name(), Component.AIDA_NO_RESULTS.name());
    addstep(Component.AIDA_NO_RESULTS.name(), Component.SALIENCE.name());
  }

  private void generateChineseSteps() {
    addstep("ZH", Component.ZH_TOKENIZER.name());
    addstep(Component.ZH_TOKENIZER.name(), Component.ZH_POS.name());
    addstep(Component.ZH_POS.name(), Component.ZH_NER.name());
    addstep(Component.ZH_NER.name(), Component.AIDA_NO_RESULTS.name());
    addstep(Component.AIDA_NO_RESULTS.name(), Component.SALIENCE.name());
  }
}
