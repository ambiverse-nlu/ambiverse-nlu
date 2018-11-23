package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.yago.YagoLabelsToClassProbabilitiesBmeow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * If createNew parameter is true: creates a new file and throws exception if it already exists
 * If createNew parameter is false: returns positive result if it already exists and create a new file if it does not
  */
public class KnowNERTokenClassProbBmeowChecker extends KnowNERLocalResourceChecker {

	static final String FILE_SUFFIX = "tokenClassProbs-bmeow.tsv";
	private final DictionaryEntriesDataProvider dictionaryEntriesDataProvider;
	private final boolean createNew;

	/**
	 * Does not check the parameters as it should be checked before
	 *
	 * @param mainDir
	 * @param language
	 */
	public KnowNERTokenClassProbBmeowChecker(String mainDir, String language,
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
			Language language = Language.getLanguageForString(this.language);
			try {
				YagoLabelsToClassProbabilitiesBmeow.storeClassTypeProbabilities(this.language, dictionaryEntriesDataProvider, path.toString(),
						language.getPathStopWords(),
						language.getPathSymbols());
			} catch (Exception e) {
				throw new KnowNERLanguageConfiguratorException(e);
			}
			if (Files.exists(path)) {
				return new KnowNERResourceResult();
			}
			throw new KnowNERLanguageConfiguratorException("File " + path + " was not created!");
		}
	}
}
