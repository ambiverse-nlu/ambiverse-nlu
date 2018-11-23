package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.WikipediaLinkProbabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class KnowNERWikipediaProbabilitiesChecker extends KnowNERLocalResourceChecker {

	private Logger logger = LoggerFactory.getLogger(KnowNERWikipediaProbabilitiesChecker.class);

	public static final String FILE_SUFFIX = "wikiLinkLikelihoods";
	private final boolean createNew;
	List<WikiLinkLikelihoodProvider> wikiLinkLikelihoodProviders;

	public KnowNERWikipediaProbabilitiesChecker(String mainDir, String language,
																							List<WikiLinkLikelihoodProvider> wikiLinkProbabilitiesProviders,
                                              boolean createNew) {
		super(mainDir, language);
		this.createNew = createNew;
		this.wikiLinkLikelihoodProviders = wikiLinkProbabilitiesProviders;
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
			logger.info("Creating '" + language + "' WikipediaLinkProbabilities and storing in: " + path);
			WikipediaLinkProbabilities wlp =
							new WikipediaLinkProbabilities(wikiLinkLikelihoodProviders, language);
			try {
				wlp.storeData(path.toFile());
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
