package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConf;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfName;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.NERManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds KnowNERLanguageConfigurator object either customized or with default resource checkers
  */
public class KnowNERLanguageConfiguratorBuilder {

	private final List<KnowNERResourceChecker> checkerList = new ArrayList<>();
	private String language;
	private String mainDir;
	private boolean createNew = false;

	public KnowNERLanguageConfiguratorBuilder addResourceChecker(KnowNERResourceChecker checker) {
		checkerList.add(checker);
		return this;
	}

	public KnowNERLanguageConfiguratorBuilder withDefaultResourceCheckers() throws KnowNERLanguageConfiguratorException, IOException {
		String confName = AIDASchemaPreparationConfig.getConfigurationName();
		DataPrepConf conf = DataPrepConfFactory.getConf(DataPrepConfName.valueOf(confName));

		checkerList.add(new KnowNERCorpusChecker(mainDir, language));
		checkerList.add(new KnowNERGazeteersChecker(mainDir, language, true));
		try {
			Optional<DictionaryEntriesDataProvider> depOptional = conf.getDictionaryEntriesProvider()
					.stream()
					.filter(p -> p.getClass() == Yago3DictionaryEntriesDataProvider.class)
					.findAny();
			if (!depOptional.isPresent()) {
				throw new KnowNERLanguageConfiguratorException("DictionaryEntriesDataProvider is not available");
			}
			checkerList.add(new KnowNERPOSDictionaryChecker(mainDir, language, depOptional.get()));
			checkerList.add(new KnowNERYagoDictionaryChecker(mainDir, language, depOptional.get(), createNew));
			checkerList.add(new KnowNERTokenClassProbBmeowChecker(mainDir, language, depOptional.get(), createNew));
		} catch (IOException | EntityLinkingDataAccessException e) {
			throw new KnowNERLanguageConfiguratorException(e);
		}

		checkerList.add(new KnowNERWikipediaProbabilitiesChecker(
						mainDir, language, conf.getWikiLinkProbabilitiesProviders(), createNew));
		checkerList.add(new KnowNERMentionTokenCountsChecker(
						mainDir, language, createNew));

		return this;
	}

	public KnowNERLanguageConfiguratorBuilder setCreateNew(boolean createNew) {
		this.createNew = createNew;
		return this;
	}

	public KnowNERLanguageConfiguratorBuilder setLanguage(String language) {
		this.language = language;
		return this;
	}

	public KnowNERLanguageConfiguratorBuilder setMainDir(String mainDir) {
		this.mainDir = mainDir;
		if (!Files.exists(Paths.get(mainDir))) {
			throw new IllegalStateException("Main directory does not exist! " + mainDir);
		}
		return this;
	}

	public KnowNERLanguageConfigurator create() {
		return new KnowNERLanguageConfigurator(language, checkerList);
	}

	public static void main(String[] args) throws Throwable {
		ConfigUtils.setConfOverride("default");
		for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {
			KnowNERLanguageConfigurator configurator = new KnowNERLanguageConfiguratorBuilder()
					.setCreateNew(false)
					.setLanguage(language.name())
					.setMainDir(KnowNERSettings.getLangResourcesLocation())
					.addResourceChecker(new KnowNERMentionTokenCountsChecker(KnowNERSettings.getKeyspace(language.name()), language.name(), false))
					.create();
			List<String> results = configurator.run();
			System.out.println("Missing resources: " + language.name() + " " + results);
		}
		NERManager.shutDown();
		EntityLinkingManager.shutDown();
	}
}
