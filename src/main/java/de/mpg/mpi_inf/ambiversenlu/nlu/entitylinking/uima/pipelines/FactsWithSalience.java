package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class FactsWithSalience extends Pipeline {
    @Override
    void addSteps() {
        addstep("EN", Component.EN_TOKENIZER.name());
        addstep(Component.EN_TOKENIZER.name(), Component.EN_POS.name());
        addstep(Component.EN_POS.name(), Component.KNOW_NER_KB.name());
        addstep(Component.KNOW_NER_KB.name(), Component.AIDA_USE_RESULTS.name());
        addstep(Component.AIDA_USE_RESULTS.name(), Component.SALIENCE.name());
        addstep(Component.SALIENCE.name(), Component.EN_LEMMATIZER.name());
        addstep(Component.EN_LEMMATIZER.name(), Component.EN_PARSERS.name());
        addstep(Component.EN_PARSERS.name(), Component.CLAUSIE.name());
    }

    @Override public Set<Language> supportedLanguages() {
        Set<Language> supported = new HashSet<>();
        supported.add(Language.getLanguageForString("en"));
        return supported;
    }
}
