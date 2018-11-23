package de.mpg.mpi_inf.ambiversenlu.nlu.ner.dictionarygeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapDictionaryGeneratorIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MapDictionaryGeneratorIntegrationTest.class);

    private Path tempDirectory = null;

    @Before
    public void before() throws IOException {
        tempDirectory = Files.createTempDirectory(null);
        logger.info(tempDirectory.toString());
        ConfigUtils.setConfOverride("integration_test");
    }

    @Test
    public void testEnglish() throws IOException, EntityLinkingDataAccessException {
        MapDictionaryGenerator.main(
                new String[]{
                        "en",
                        "src/test/resources/ner/preparefornewlanguage/initialResources/dictionary_generator/interesting_yagoclasses_test",
                        tempDirectory.toString()});
        File tempDir = new File(tempDirectory.toString());
        System.out.println(tempDir.isDirectory());
        assertTrue(tempDir.isDirectory()
                && tempDir.list().length > 0/*
                && Files.size(Paths.get(tempDir.list()[0])) > 0*/);

    }

    @Test
    public void testGerman() throws IOException, EntityLinkingDataAccessException {
        MapDictionaryGenerator.main(
                new String[]{
                        "de",
                        "src/test/resources/ner/preparefornewlanguage/initialResources/dictionary_generator/interesting_yagoclasses_test",
                        tempDirectory.toString()});
        File tempDir = new File(tempDirectory.toString());
        assertTrue(tempDir.isDirectory()
                && tempDir.list().length > 0/*
                && Files.size(Paths.get(tempDir.list()[0])) > 0*/);

    }

    @Test
    public void testGermanWithDirectoryOfTypes() throws IOException, EntityLinkingDataAccessException {
        MapDictionaryGenerator.main(
                new String[]{
                        "de",
                        "src/test/resources/ner/dictionary_generator/yago_types",
                        tempDirectory.toString()});
        File tempDir = new File(tempDirectory.toString());
        assertTrue(tempDir.isDirectory()
                && tempDir.list().length > 0/*
                && Files.size(Paths.get(tempDir.list()[0])) > 0*/);

    }

    @Test
    public void testSampling() {

        Set<String> str = new HashSet<>();
        int size = 100;
        while (size-- > 0) {
            str.add(Integer.toString(size));

        }
//        at least compare size of the returned sample
        assertEquals(MapDictionaryGenerator.sample(str, 10).size(), 10);
    }

    @After
    public void after() throws IOException {
        Util.deleteNonEmptyDirectory(tempDirectory);
    }

}