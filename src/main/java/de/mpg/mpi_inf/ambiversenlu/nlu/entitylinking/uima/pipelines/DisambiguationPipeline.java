package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;

import java.util.HashSet;
import java.util.Set;

public class DisambiguationPipeline extends Pipeline {

    @Override void addSteps() {
        addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
        for (Language language : Language.activeLanguages()) {
            if (language.isChinese()) {
                addstep("ZH", "ZH_TOKENIZER");
                addstep("ZH_TOKENIZER", "ZH_POS");
                addstep("ZH_POS", "ZH_NER");
                addstep("ZH_NER", Component.AIDA_USE_RESULTS.name());
            } else if (language.isGerman()) {
                addstep("DE", Component.DE_TOKENIZER.name());
                addstep(Component.DE_TOKENIZER.name(), Component.DE_LEMMATIZER.name());
                addstep(Component.DE_LEMMATIZER.name(), Component.DE_POS.name());
                addstep(Component.DE_POS.name(), Component.DE_KNOW_NER_KB.name());
                addstep(Component.DE_KNOW_NER_KB.name(), Component.DE_NER.name());
                addstep(Component.DE_NER.name(), Component.DE_NER2.name());
                addstep(Component.DE_NER2.name(), Component.AIDA_USE_RESULTS.name());
            }  else if (KnowNERLanguage.supports(language)) {
                String upperCaseLanguage = language.name().toUpperCase();
                String next = upperCaseLanguage + "_TOKENIZER";
                addstep(upperCaseLanguage, next);

                if (KnowNERLanguage.requiresLemma(language)) {
                    addstep(next, upperCaseLanguage + "_LEMMATIZER");
                    next = upperCaseLanguage + "_LEMMATIZER";
                }
                addstep(next, upperCaseLanguage + "_POS");
                addstep(upperCaseLanguage + "_POS", "KNOW_NER_KB");
                addstep("KNOW_NER_KB", Component.AIDA_USE_RESULTS.name());
            } else {
                throw new UnsupportedOperationException("Language " + language + " is not supported!");
            }
        }
    }

    @Override public Set<Language> supportedLanguages() {
        return new HashSet<>(Language.activeLanguages());
    }


}
