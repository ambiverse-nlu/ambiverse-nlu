package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.dictionarygeneration.MapDictionaryGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Checks if gazeteers are present for the given configuration. If no, generate them.
  */
public class KnowNERGazeteersChecker extends KnowNERLocalResourceChecker {

	public static final String GAZETEERS_DIRECTORY_NAME = "gazeteers";
	private static final String DEFAULT_YAGO_TYPES_LOCATION = "src/main/resources/ner/dictionary_generator/yago_types/";
	private static final String DEFAULT_DICTIONARY_PARAMETERS_PATH = "src/main/resources/ner/dictionary_generator/dictionary_parameters.csv";
	private final boolean skipConceptDictionaries;

	public KnowNERGazeteersChecker(String mainDir, String language,
								   boolean skipConceptDictionaries) {
		super(mainDir, language);
		this.skipConceptDictionaries = skipConceptDictionaries;

	}

	@Override
	public KnowNERResourceResult check() throws KnowNERLanguageConfiguratorException {
		if (!skipConceptDictionaries) {
			throw new UnsupportedOperationException();
		}

		Path gazeteersPath = Paths.get(languageDir, GAZETEERS_DIRECTORY_NAME);

		try {

			if (Files.exists(gazeteersPath) &&
					Files.isDirectory(gazeteersPath) &&
					Files.list(gazeteersPath).count() > 0) {
				return new KnowNERResourceResult();
			}

			MapDictionaryGenerator.main(new String[]{
					language,
					DEFAULT_YAGO_TYPES_LOCATION,
					gazeteersPath.toString(),
					DEFAULT_DICTIONARY_PARAMETERS_PATH});

			if (Files.exists(gazeteersPath) &&
					Files.isDirectory(gazeteersPath) &&
					Files.list(gazeteersPath).count() > 0) {
				return new KnowNERResourceResult();
			}

			throw new KnowNERLanguageConfiguratorException("No gazeteers generated!");
			} catch (EntityLinkingDataAccessException | IOException e) {
				throw new KnowNERLanguageConfiguratorException(e);
			}

	}
}
