package de.mpg.mpi_inf.ambiversenlu.nlu.ner.training;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.AidaAnalysisEngine;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.CorpusConfiguration;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.KnowNEREvaluation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.BmeowTypeAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.DictionaryFeaturesAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.DictionaryMatchAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.LanguageAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors.BmeowTag;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors.dictionary.CountryLowercaseMatch;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors.dictionary.DictionariesExtractor;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors.dictionary.PosSequenceMatch;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class KnowNERTrainingUtils {

	private static final Map<KnowNERLanguage, Map<String, CorpusConfiguration>> defaultCorpusConfigurationMap;

	static {
		try {
		Map<KnowNERLanguage, Map<String, CorpusConfiguration>> defaultCorpusConfigurationMap_ = new HashMap<>();
		for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {
			String corpusPath = KnowNERSettings.getLangResourcesPath(language.name());
			Map<String, CorpusConfiguration> corpusConfigurationMap = new HashMap<>();
			Files.walkFileTree(Paths.get(corpusPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.getFileName().toString().equals(CorpusConfiguration.DEFAULT_CORPUS_CONFIG_NAME)) {
						try (JsonReader jsonReader = new JsonReader(new FileReader(file.toFile()))) {
							corpusConfigurationMap.put(file.getParent().getFileName().toString(),
									new Gson().fromJson(jsonReader,
											new TypeToken<CorpusConfiguration>(){}
													.getType()));

						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					return super.visitFile(file, attrs);
				}
			});
			defaultCorpusConfigurationMap_.put(language, Collections.unmodifiableMap(corpusConfigurationMap));

		}
		defaultCorpusConfigurationMap = Collections.unmodifiableMap(defaultCorpusConfigurationMap_);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static CorpusConfiguration getDefaultCorpusConfigurationForLanguage(KnowNERLanguage language) {
		return defaultCorpusConfigurationMap.get(language).get(CorpusConfiguration.DEFAULT_CORPUS_NAME);
	}

	public static CorpusConfiguration getCorpusConfigurationForLanguage(String corpusName, KnowNERLanguage language) {
		return defaultCorpusConfigurationMap.get(language).get(corpusName);
	}

	public static int[] getDatasetRangeForCorpus(String corpusName, KnowNERLanguage language, CorpusConfiguration.Range range) {
		return defaultCorpusConfigurationMap.get(language).get(corpusName).getRangeMap().get(range);
	}

	/**
	 * Here you need to define, which annotators should be
	 * run in the pre-processing. This is needed for features
	 * like BmeowTag
	 */
	public static AnalysisEngineDescription getPreprocessing(String language,
															 List<String> featureList,
															 KnowNEREvaluation.EntityStandard entityStandard,
															 boolean useLemmatizer,
															 boolean usePOSTagger
	) throws ResourceInitializationException {

		List<AnalysisEngineDescription> engineDescList = new ArrayList<>();
		engineDescList.add(AnalysisEngineFactory.createEngineDescription(LanguageAnnotator.class, LanguageAnnotator.LANGUAGE, language));

		if (useLemmatizer) {
			Component lemmatizerComponent = Component.valueOf(language.toUpperCase() + "_LEMMATIZER");
			engineDescList.add(createEngineDescription(lemmatizerComponent.component,
					lemmatizerComponent.params));
		}
		if (usePOSTagger) {
			Component posComponent = Component.valueOf(language.toUpperCase() + "_POS");
			engineDescList.add(createEngineDescription(posComponent.component,
					"language", language));
		}

		if (featureList.contains(BmeowTag.class.getCanonicalName())) {
			if (entityStandard == KnowNEREvaluation.EntityStandard.AIDA) {
				engineDescList.add(createEngineDescription(AidaAnalysisEngine.class,
								AidaAnalysisEngine.USE_RESULTS, false));
			}
			engineDescList.add(AnalysisEngineFactory.createEngineDescription(BmeowTypeAnnotator.class,
					BmeowTypeAnnotator.GOLD, entityStandard == KnowNEREvaluation.EntityStandard.GOLD));
		}

		if (featureList.contains(CountryLowercaseMatch.class.getCanonicalName()) ||
				featureList.contains(PosSequenceMatch.class.getCanonicalName())) {
			engineDescList.add(AnalysisEngineFactory.createEngineDescription(DictionaryMatchAnnotator.class));
		}

		if (featureList.contains(DictionariesExtractor.class.getCanonicalName())) {
			engineDescList.add(AnalysisEngineFactory.createEngineDescription(DictionaryFeaturesAnnotator.class));
		}

		if (!engineDescList.isEmpty()) {

			AnalysisEngineDescription[] analysisEngineDescriptions = new AnalysisEngineDescription[engineDescList.size()];
			for (int i = 0; i < engineDescList.size(); i++) {
				analysisEngineDescriptions[i] = engineDescList.get(i);
			}
			return createEngineDescription(analysisEngineDescriptions);
		}
		return null;
	}

}
