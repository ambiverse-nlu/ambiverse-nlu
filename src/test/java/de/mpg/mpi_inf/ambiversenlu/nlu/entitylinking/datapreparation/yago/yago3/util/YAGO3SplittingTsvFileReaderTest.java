package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


public class YAGO3SplittingTsvFileReaderTest {

  @Test public void testSplitYago3File() throws Exception {
    File yagoFile = Paths.get(ClassLoader.getSystemResource("yago3/yago3.tsv").toURI()).toFile();

    // Delete old split files.
    File[] splitFiles = yagoFile.getParentFile().listFiles(((dir, name) -> name.contains("_split_")));
    for (File f : splitFiles) {
      f.delete();
    }

    YAGO3SplittingTsvFileReader reader = new YAGO3SplittingTsvFileReader(yagoFile);

    assertEquals(10, reader.getFacts("rdf:type").size());
    assertEquals(1, reader.getFacts("<hasGloss>").size());
    assertEquals(9, reader.getFacts("<hasInternalWikipediaLinkTo>").size());
    assertEquals(1, reader.getFacts("<hasFamilyName>").size());

    assertEquals("Blücher", reader.getFacts("<hasFamilyName>").get(0).getObjectAsJavaString());
  }
}