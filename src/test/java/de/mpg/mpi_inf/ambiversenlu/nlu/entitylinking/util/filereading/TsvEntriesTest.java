package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TsvEntriesTest {

  @Test public void testRegular() throws FileNotFoundException, URISyntaxException {
    File file = Paths.get(ClassLoader.getSystemResource("fileentriestest/file.tsv").toURI()).toFile();
    int i = 0;
    
    for (String[] line : new TsvEntries(file)) {
      switch (i) {
        case 0:
          assertArrayEquals(new String[] { "A", "B", "C" }, line);
          break;
        case 1:
          assertArrayEquals(new String[] { "D" }, line);
          break;
        case 2:
          assertArrayEquals(new String[] { "E" }, line);
          break;
        case 3:
          assertArrayEquals(new String[] { "F", "G" }, line);
          break;
        case 4:
          assertArrayEquals(new String[] { "H", "IJK", "L" }, line);
          break;
        case 5:
          assertArrayEquals(new String[] { "MNO" }, line);
          break;
        default:
          break;
      }
      ++i;
    }
    
    assertEquals(6, i);
  }

  @Test public void testOneliner() throws FileNotFoundException, URISyntaxException {
    File file = Paths.get(ClassLoader.getSystemResource("fileentriestest/oneliner.tsv").toURI()).toFile();
    int i = 0;
    
    for (String line[] : new TsvEntries(file)) {
      assertArrayEquals(new String[] { "A", "B", "C" }, line);
      ++i;
    }
    
    assertEquals(1, i);
  }
}