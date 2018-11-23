package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;

import java.util.HashSet;
import java.util.Set;

public class EvaluationPipeline extends Pipeline {

	@Override
	void addSteps() {
		addstep("first", Component.LANGUAGE_IDENTIFICATION.name());
		for (Language language : Language.activeLanguages()) {
			if (language.isChinese()) {
				addstep("ZH", "ZH_POS");
				addstep("ZH_POS", "ZH_NER");
				addstep("ZH_NER", Component.AIDA_NO_RESULTS.name());
			} else if (language.isGerman()) {
				addstep("DE", Component.DE_LEMMATIZER.name());
				addstep(Component.DE_LEMMATIZER.name(), Component.DE_POS.name());
				addstep(Component.DE_POS.name(), Component.DE_NER.name());
				addstep(Component.DE_NER.name(), Component.DE_NER2.name());
				addstep(Component.DE_NER2.name(), Component.AIDA_NO_RESULTS.name());

			}  else if (KnowNERLanguage.supports(language)) {
				String next = language.name().toUpperCase();
				if (KnowNERLanguage.requiresLemma(language)) {
					addstep(next, language.name().toUpperCase() + "_LEMMATIZER");
					next = language.name().toUpperCase() + "_LEMMATIZER";
				}
				addstep(next, language.name().toUpperCase() + "_POS");
				addstep(language.name().toUpperCase() + "_POS", Component.EN_NER.name());
				addstep(Component.EN_NER.name(), Component.AIDA_NO_RESULTS.name());
			} else {
				throw new UnsupportedOperationException("Language " + language + " is not supported!");
			}
		}
	}

	@Override public Set<Language> supportedLanguages() {
		return new HashSet<>(Language.activeLanguages());
	}
}
