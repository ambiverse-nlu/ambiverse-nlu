package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.Set;

public class DisambiguationStanfordPipeline extends Pipeline {

    @Override
    void addSteps() {
        addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
        for (Language language : supportedLanguages()) {
            if (language.isChinese()) {
                addstep("ZH", "ZH_TOKENIZER");
                addstep("ZH_TOKENIZER", "ZH_POS");
                addstep("ZH_POS", "ZH_NER");
                addstep("ZH_NER", Component.AIDA_USE_RESULTS.name());
            } else if (language.isGerman()) {
                addstep("DE", Component.DE_TOKENIZER.name());
                addstep(Component.DE_TOKENIZER.name(), Component.DE_POS.name());
                addstep(Component.DE_POS.name(), Component.DE_NER.name());
                addstep(Component.DE_NER.name(), Component.DE_NER2.name());
                addstep(Component.DE_NER2.name(), Component.AIDA_USE_RESULTS.name());
            }  else  {
                String upperCaseLanguage = language.name().toUpperCase();
                String next = upperCaseLanguage + "_TOKENIZER";
                addstep(upperCaseLanguage, next);
                addstep(next, upperCaseLanguage + "_POS");
                addstep(upperCaseLanguage + "_POS", upperCaseLanguage + "_NER");
                addstep(upperCaseLanguage + "_NER", Component.AIDA_USE_RESULTS.name());
            }
        }
    }



    @Override public Set<Language> supportedLanguages()
    {
        return PipelineUtil.getStanfordSupportedLanguages();
    }
}
