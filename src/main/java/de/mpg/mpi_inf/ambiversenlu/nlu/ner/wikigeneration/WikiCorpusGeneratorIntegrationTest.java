package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class WikiCorpusGeneratorIntegrationTest {

	private Path parentPath;
	private Path destinationPath;

	@Before
	public void setUp() throws Exception {

		ConfigUtils.setConfOverride("integration_test");
		parentPath = Files.createTempDirectory("wikicorpusgeneratortest");
		destinationPath = Paths.get(parentPath.toString() + "/wikicorpusgeneratortest.tsv");

	}

	@After
	public void tearDown() throws Exception {
		Util.deleteNonEmptyDirectory(parentPath);

	}

//	Fails with memory issues - not needed for now.
//	@Test
	public void shouldCreateWikiCorpus() throws Throwable {
		new WikiCorpusGenerator(new String[]{
				"-p", destinationPath.toString(),
				"-l", "en",
				"-s", "ENTITY_RANK",
				"-m", "5"
		}).run();

		Assert.assertTrue(Files.exists(destinationPath));
		long docsNumber = Files.readAllLines(destinationPath)
				.stream()
				.filter(s -> s.startsWith("-DOCSTART-"))
				.count();
		Assert.assertEquals(5, docsNumber);

	}

}