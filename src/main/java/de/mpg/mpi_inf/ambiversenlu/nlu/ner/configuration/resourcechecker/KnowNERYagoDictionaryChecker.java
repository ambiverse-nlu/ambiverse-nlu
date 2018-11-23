package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.yago.YagoLabelsToYagoDictionary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KnowNERYagoDictionaryChecker extends KnowNERLocalResourceChecker {

	static final String FILE_SUFFIX = "yagoDictionary";
	private final DictionaryEntriesDataProvider dictionaryEntriesDataProvider;
	private final boolean createNew;

	/**
	 * Does not check the parameters as it should be checked before
	 *
	 * @param mainDir
	 * @param language
	 */
	public KnowNERYagoDictionaryChecker(String mainDir, String language,
										DictionaryEntriesDataProvider dictionaryEntriesDataProvider,
										boolean createNew) {
		super(mainDir, language);
		this.dictionaryEntriesDataProvider = dictionaryEntriesDataProvider;
		this.createNew = createNew;
	}

	@Override
	public KnowNERResourceResult check() throws KnowNERLanguageConfiguratorException {
		Path path = Paths.get(languageDir, getAidaPrefix() + FILE_SUFFIX);
		if (Files.exists(path)) {
			if (createNew) {
				return new KnowNERResourceResult(path + " already exists!");
			}
			return new KnowNERResourceResult();
		} else {
			YagoLabelsToYagoDictionary.generateYagoDictionary(language, dictionaryEntriesDataProvider, path);
			if (Files.exists(path)) {
				return new KnowNERResourceResult();
			}
			throw new KnowNERLanguageConfiguratorException("File " + path + " was not created!");
		}
	}
}
