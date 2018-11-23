package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.CorpusConfiguration;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KnowNERCorpusCheckerIntegrationTest {

	private Path tempMainDir;
	private final String language = "de";

	@Before
	public void setUp() throws Exception {
		ConfigUtils.setConfOverride("integration_test");
		tempMainDir = Files.createTempDirectory("tempMainDir");
	}

	@After
	public void tearDown() throws Exception {
		Util.deleteNonEmptyDirectory(tempMainDir);

	}

//	Fails with memory issues - not needed for now.
//	@Test
	public void shouldAutogenerateCorpusIfItDoesNotExist() throws IOException, KnowNERLanguageConfiguratorException {

		KnowNERCorpusChecker checker = new KnowNERCorpusChecker(tempMainDir.toString(), language, 6);
		assertTrue(checker.check().isSuccess());
		assertTrue(Files.exists(Paths.get(tempMainDir.toString(),language,
				CorpusConfiguration.CORPUS_DIRECTORY_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_NAME,
				CorpusConfiguration.DEFAULT_FILE_NAME)));
		assertTrue(Files.exists(Paths.get(tempMainDir.toString(),language,
				CorpusConfiguration.CORPUS_DIRECTORY_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_CONFIG_NAME)));
	}

	@Test
	public void shouldReturnFailureIfCorpusExistsButConfigurationDoesNotExist() throws IOException, KnowNERLanguageConfiguratorException {
		Path corpusDir = Files.createDirectories(Paths.get(tempMainDir.toString(),language,
				CorpusConfiguration.CORPUS_DIRECTORY_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_NAME));
		Files.createFile(Paths.get(corpusDir.toString(), CorpusConfiguration.DEFAULT_FILE_NAME));

		KnowNERCorpusChecker checker = new KnowNERCorpusChecker(tempMainDir.toString(), language, 6);
		assertFalse(checker.check().isSuccess());
	}

	@Test
	public void shouldReturnSuccessIfCorpusAndConfigurationExist() throws IOException, KnowNERLanguageConfiguratorException {
		Path corpusDir = Files.createDirectories(Paths.get(tempMainDir.toString(),language,
				CorpusConfiguration.CORPUS_DIRECTORY_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_NAME));
		Files.write(Paths.get(corpusDir.toString(), CorpusConfiguration.DEFAULT_FILE_NAME),
				Collections.nCopies(6, "-DOCSTART-"));
		Files.write(Paths.get(corpusDir.toString(), CorpusConfiguration.DEFAULT_CORPUS_CONFIG_NAME),
				("{corpusFormat: DEFAULT, " +
						"rangeMap: {" +
						"TRAIN: [0,1]," +
						"TESTA: [2,3]," +
						"TESTB: [4,5]," +
						"TRAINA: [0,3]}}").getBytes());

		KnowNERCorpusChecker checker = new KnowNERCorpusChecker(tempMainDir.toString(), language, 6);
		assertTrue(checker.check().isSuccess());
	}

	@Test
	public void shouldReturnFailureIfCorpusDoesNotExistButConfigurationExists() throws IOException, KnowNERLanguageConfiguratorException {
		Path corpusDir = Files.createDirectories(Paths.get(tempMainDir.toString(),language,
				CorpusConfiguration.CORPUS_DIRECTORY_NAME,
				CorpusConfiguration.DEFAULT_CORPUS_NAME));
		Files.createFile(Paths.get(corpusDir.toString(), CorpusConfiguration.DEFAULT_CORPUS_CONFIG_NAME));

		KnowNERCorpusChecker checker = new KnowNERCorpusChecker(tempMainDir.toString(), language, 6);
		assertFalse(checker.check().isSuccess());
	}

}