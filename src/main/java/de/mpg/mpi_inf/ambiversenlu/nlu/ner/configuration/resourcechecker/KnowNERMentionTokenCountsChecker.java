package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.MentionTokenFrequencyCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KnowNERMentionTokenCountsChecker extends KnowNERLocalResourceChecker {

	private Logger logger = LoggerFactory.getLogger(KnowNERMentionTokenCountsChecker.class);

	public static final String FILE_SUFFIX = "mentionTokenCounts";
	private final boolean createNew;

	public KnowNERMentionTokenCountsChecker(String mainDir, String language,
                                          boolean createNew) {
		super(mainDir, language);
		this.createNew = createNew;
	}

	@Override
	public KnowNERResourceResult check() throws KnowNERLanguageConfiguratorException {
		Path path = Paths.get(languageDir, getAidaPrefix() + FILE_SUFFIX);
		if (Files.exists(path)) {
			if (createNew) {
				return new KnowNERResourceResult(path + " already exists!");
			} else {
				return new KnowNERResourceResult();
			}
		} else {
			logger.info("Creating '" + language + "' MentionTokenFrequencyCounts and storing in: " + path);

			MentionTokenFrequencyCounts mtfc = new MentionTokenFrequencyCounts(language);
			try {
				mtfc.init();
				mtfc.storeData(path.toFile());
			} catch (EntityLinkingDataAccessException e) {
				throw new KnowNERLanguageConfiguratorException(e);
			} catch (IOException e) {
				throw new KnowNERLanguageConfiguratorException(e);
			}
			if (Files.exists(path)) {
				return new KnowNERResourceResult();
			}
			throw new KnowNERLanguageConfiguratorException("File " + path + " was not created!");
		}
	}
}
