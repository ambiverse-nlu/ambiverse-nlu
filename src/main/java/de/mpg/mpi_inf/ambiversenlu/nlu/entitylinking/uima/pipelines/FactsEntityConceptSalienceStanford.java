package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class FactsEntityConceptSalienceStanford extends Pipeline {
    @Override
    void addSteps() {
        generateEnglishSteps();
        generateGermanSteps();
    }

    @Override public Set<Language> supportedLanguages() {
        Set<Language> supported = new HashSet<>();
        supported.add(Language.getLanguageForString("en"));
        supported.add(Language.getLanguageForString("de"));
        return supported;
    }

    private void generateEnglishSteps() {
        addstep("EN", Component.EN_TOKENIZER.name());
        addstep(Component.EN_TOKENIZER.name(), Component.EN_POS.name());
        addstep(Component.EN_POS.name(), Component.EN_NER.name());
        addstep(Component.EN_NER.name(), Component.AIDA_NO_RESULTS.name());
        addstep(Component.AIDA_NO_RESULTS.name(), Component.CONCEPT_SPOTTER_EXACT.name());
        addstep(Component.CONCEPT_SPOTTER_EXACT.name(), Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES_NEs.name());
        addstep(Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES_NEs.name(), Component.CD_USE_RESULTS.name());
        addstep(Component.CD_USE_RESULTS.name(), Component.SALIENCE.name());
        addstep(Component.SALIENCE.name(), Component.EN_PARSERS.name());
        addstep(Component.EN_PARSERS.name(), Component.CLAUSIE.name());
    }

    // CAREFUL, German does not have facts! Fallback for better demo purposes.
    private void generateGermanSteps() {
        addstep("DE", Component.DE_TOKENIZER.name());
        addstep(Component.DE_TOKENIZER.name(), Component.DE_POS.name());
        addstep(Component.DE_POS.name(), Component.DE_NER.name());
        addstep(Component.DE_NER.name(), Component.DE_NER2.name());
        addstep(Component.DE_NER2.name(), Component.AIDA_NO_RESULTS.name());
        addstep(Component.AIDA_NO_RESULTS.name(), Component.CONCEPT_SPOTTER_EXACT.name());
        addstep(Component.CONCEPT_SPOTTER_EXACT.name(), Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES_NEs.name());
        addstep(Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES_NEs.name(), Component.CD_USE_RESULTS.name());
        addstep(Component.CD_USE_RESULTS.name(), Component.SALIENCE.name());
    }
}
