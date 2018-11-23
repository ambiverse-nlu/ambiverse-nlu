package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;


public class FileEntriesTest {

  @Test public void testRegular() throws URISyntaxException {
    File file = Paths.get(ClassLoader.getSystemResource("fileentriestest/file.tsv").toURI()).toFile();    
    int i = 0;
    
    for (String line : new FileEntries(file)) {
      switch (i) {
        case 0:
          assertEquals("A\tB\tC", line);
          break;
        case 1:
          assertEquals("D", line);
          break;
        case 2:
          assertEquals("E", line);
          break;
        case 3:
          assertEquals("F\tG", line);
          break;
        case 4:
          assertEquals("H\tIJK\tL", line);
          break;
        case 5:
          assertEquals("MNO", line);
          break;
        default:
          break;
      }
      ++i;
    }
    assertEquals(6, i);
  }

  @Test public void testOneliner() throws URISyntaxException {
    File file = Paths.get(ClassLoader.getSystemResource("fileentriestest/oneliner.tsv").toURI()).toFile();
    int i = 0;
    
    for (String line : new FileEntries(file)) {
      assertEquals("A\tB\tC", line);
      ++i;
    }
    
    assertEquals(1, i);
  }
}