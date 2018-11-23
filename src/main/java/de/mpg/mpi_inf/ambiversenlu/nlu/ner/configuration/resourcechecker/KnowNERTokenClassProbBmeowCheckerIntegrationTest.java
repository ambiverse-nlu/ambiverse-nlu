package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConf;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfName;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KnowNERTokenClassProbBmeowCheckerIntegrationTest {
	private DictionaryEntriesDataProvider dataProvider;
	private Path tempMainDir;
	private final String language = "de";

	@Before
	public void setUp() throws Exception {
		ConfigUtils.setConfOverride("default_ner_prepare_for_new_language_test_de");
		String confName = AIDASchemaPreparationConfig.getConfigurationName();
		DataPrepConf conf = DataPrepConfFactory.getConf(DataPrepConfName.valueOf(confName));
		Optional<DictionaryEntriesDataProvider> depOptional = conf.getDictionaryEntriesProvider()
				.stream()
				.filter(p -> p.getClass() == Yago3DictionaryEntriesDataProvider.class)
				.findAny();
		if (!depOptional.isPresent()) {
			throw new RuntimeException("DictionaryEntriesDataProvider is not available");
		}
		this.dataProvider = depOptional.get();
		tempMainDir = Files.createTempDirectory("tempMainDir");
		Files.createDirectories(Paths.get(tempMainDir.toString(),language));
	}

	@After
	public void tearDown() throws Exception {
		Util.deleteNonEmptyDirectory(tempMainDir);

	}

	@Test
	public void shouldReturnSuccessWhenNoFileExistsAndCreateNew() throws Exception {
		KnowNERResourceResult result = new KnowNERTokenClassProbBmeowChecker(tempMainDir.toString(), language, dataProvider, true).check();
		assertTrue(result.isSuccess());

	}

	@Test
	public void shouldReturnSuccessWhenFileExistsAndCreateNew() throws Exception {
		Files.write(Paths.get(tempMainDir.toString(), language,
				KnowNERLocalResourceChecker.getAidaPrefix() + KnowNERTokenClassProbBmeowChecker.FILE_SUFFIX),
				("Richard Anconina\n" +
						"GehÃ¶rgangsexostose\n" +
						"Bugatti EB110\n" +
						"Vesna Zmijanac\n" +
						"Quid\n" +
						"Quid\n").getBytes());

		KnowNERResourceResult result = new KnowNERTokenClassProbBmeowChecker(tempMainDir.toString(), language, dataProvider, true).check();
		assertFalse(result.isSuccess());

	}

	@Test
	public void shouldReturnSuccessWhenFileExistsAndNotCreateNew() throws Exception {
		Files.write(Paths.get(tempMainDir.toString(), language,
				KnowNERLocalResourceChecker.getAidaPrefix() + KnowNERTokenClassProbBmeowChecker.FILE_SUFFIX),
				("Richard Anconina\n" +
						"GehÃ¶rgangsexostose\n" +
						"Bugatti EB110\n" +
						"Vesna Zmijanac\n" +
						"Quid\n" +
						"Quid\n").getBytes());

		KnowNERResourceResult result = new KnowNERTokenClassProbBmeowChecker(tempMainDir.toString(), language, dataProvider, false).check();
		assertTrue(result.isSuccess());

	}
}