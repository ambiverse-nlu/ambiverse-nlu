package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaPOSTagger;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * If the posDictionary exists return true, otherwise generates it
  */
public class KnowNERPOSDictionaryChecker extends KnowNERLocalResourceChecker {

	public static final String POS_DICTIONARY_FILE = "posDictionary";
	private final DictionaryEntriesDataProvider dictionaryEntriesDataProvider;

	/**
	 * Does not check the parameters as it should be checked before
	 *
	 * @param mainDir
	 * @param language
	 */
	public KnowNERPOSDictionaryChecker(String mainDir, String language,
									   DictionaryEntriesDataProvider dictionaryEntriesDataProvider) {
		super(mainDir, language);
		this.dictionaryEntriesDataProvider = dictionaryEntriesDataProvider;
	}

	@Override
	public KnowNERResourceResult check() throws KnowNERLanguageConfiguratorException {
		Path path = Paths.get(languageDir, POS_DICTIONARY_FILE);
		if (Files.exists(path)) {
			return new KnowNERResourceResult();
		}

		try {
			new UimaPOSTagger().run(language, dictionaryEntriesDataProvider, path.toString());

		} catch (Exception e) {
			throw new KnowNERLanguageConfiguratorException(e);
		}
		if (!Files.exists(path)) {
			throw new KnowNERLanguageConfiguratorException("posDictionary file was not created");
		} else {
			return new KnowNERResourceResult();
		}
	}
}
