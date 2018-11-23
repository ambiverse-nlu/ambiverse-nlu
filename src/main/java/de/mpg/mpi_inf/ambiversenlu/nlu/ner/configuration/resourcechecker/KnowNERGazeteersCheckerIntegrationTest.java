package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERGazeteersChecker.GAZETEERS_DIRECTORY_NAME;
import static org.junit.Assert.assertTrue;

public class KnowNERGazeteersCheckerIntegrationTest {

	private Path tempMainDir;
	private final String language = "en";

	@Before
	public void setUp() throws Exception {
		tempMainDir = Files.createTempDirectory("tempMainDir");
		ConfigUtils.setConfOverride("integration_test");
		
	}

	@After
	public void tearDown() throws Exception {

		Util.deleteNonEmptyDirectory(tempMainDir);

	}

	@Test
	public void shouldReturnSuccessWhenAtLeastOneGazeteerThere() throws IOException, KnowNERLanguageConfiguratorException {
		Path gazeteersDir = Files.createDirectories(Paths.get(tempMainDir.toString(),language,
				GAZETEERS_DIRECTORY_NAME));
		Files.write(Paths.get(gazeteersDir.toString(), "cardinalNumber.txt"),
				("one\n" +
						"two\n" +
						"three\n" +
						"four\n" +
						"five").getBytes());

		KnowNERResourceResult result = new KnowNERGazeteersChecker(
				tempMainDir.toString(),
				language,
				true).check();

		assertTrue(result.isSuccess());
	}

	@Test
	public void shouldReturnSuccessWhenNoGazeteersThere() throws IOException, KnowNERLanguageConfiguratorException {
		Files.createDirectories(Paths.get(tempMainDir.toString(),language,
				GAZETEERS_DIRECTORY_NAME));

		KnowNERResourceResult result = new KnowNERGazeteersChecker(
				tempMainDir.toString(),
				language,
				true).check();

		assertTrue(result.isSuccess());
	}

}