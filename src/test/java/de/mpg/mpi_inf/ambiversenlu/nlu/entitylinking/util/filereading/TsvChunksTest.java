package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class TsvChunksTest {
  
  private File datafile;
  
  @Before
  public void initializeDataFile() throws URISyntaxException {
    datafile = Paths.get(ClassLoader.getSystemResource("fileentriestest/datafile.tsv").toURI()).toFile();
  }

  @Test public void testRegular() {
    int i = 0;
    for (Pair<String[], List<String[]>> entry : new TsvChunks(datafile, new int[] { 0, 0 }, false, 100)) {
      switch (i) {
        case 0:
          assertArrayEquals(new String[] { "A" }, entry.first);
          assertEquals(3, entry.second.size());
          assertArrayEquals(new String[] { "1", "2" }, entry.second.get(0));
          assertArrayEquals(new String[] { "1", "3" }, entry.second.get(1));
          assertArrayEquals(new String[] { "2", "1" }, entry.second.get(2));
          break;
        case 1:
          assertArrayEquals(new String[] { "B" }, entry.first);
          assertEquals(2, entry.second.size());
          assertArrayEquals(new String[] { "1", "1" }, entry.second.get(0));
          assertArrayEquals(new String[] { "2", "1" }, entry.second.get(1));
          break;
        case 2:
          assertArrayEquals(new String[] { "C" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "1", "1" }, entry.second.get(0));
          break;
        case 3:
          assertArrayEquals(new String[] { "D" }, entry.first);
          assertEquals(2, entry.second.size());
          assertArrayEquals(new String[] { "3", "1" }, entry.second.get(0));
          assertArrayEquals(new String[] { "4", "2" }, entry.second.get(1));
          break;
        case 4:
          assertArrayEquals(new String[] { "E" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "1", "1" }, entry.second.get(0));
          break;
        default:
          break;
      }
      ++i;
    }
    assertEquals(5, i);
  }

  @Test public void testBackwards() {
    int i = 0;
    for (Pair<String[], List<String[]>> entry : new TsvChunks(datafile, new int[] { -2, -2 }, false, 100)) {
      switch (i) {
        case 0:
          assertArrayEquals(new String[] { "1" }, entry.first);
          assertEquals(2, entry.second.size());
          assertArrayEquals(new String[] { "A", "2" }, entry.second.get(0));
          assertArrayEquals(new String[] { "A", "3" }, entry.second.get(1));
          break;
        case 1:
          assertArrayEquals(new String[] { "2" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "A", "1" }, entry.second.get(0));
          break;
        case 7:
          assertArrayEquals(new String[] { "1" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "E", "1" }, entry.second.get(0));
          break;
        default:
          break;
      }
      ++i;
    }
    assertEquals(8, i);
  }

  @Test public void testRange() {
    int i = 0;
    for (Pair<String[], List<String[]>> entry : new TsvChunks(datafile, new int[] { 0, 1 }, false, 100)) {
      switch (i) {
        case 0:
          assertArrayEquals(new String[] { "A", "1" }, entry.first);
          assertEquals(2, entry.second.size());
          assertArrayEquals(new String[] { "2" }, entry.second.get(0));
          assertArrayEquals(new String[] { "3" }, entry.second.get(1));
          break;
        case 1:
          assertArrayEquals(new String[] { "A", "2" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "1" }, entry.second.get(0));
          break;
        case 7:
          assertArrayEquals(new String[] { "E", "1" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "1" }, entry.second.get(0));
          break;
        default:
          break;
      }
      ++i;
    }
    assertEquals(8, i);
  }

  @Test public void testInner() throws URISyntaxException {
    int i = 0;
    for (Pair<String[], List<String[]>> entry : new TsvChunks(datafile, new int[] { 1, 1 }, false, 100)) {
      switch (i) {
        case 0:
          assertArrayEquals(new String[] { "1" }, entry.first);
          assertEquals(2, entry.second.size());
          assertArrayEquals(new String[] { "A", "2" }, entry.second.get(0));
          assertArrayEquals(new String[] { "A", "3" }, entry.second.get(1));
          break;
        case 1:
          assertArrayEquals(new String[] { "2" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "A", "1" }, entry.second.get(0));
          break;
        case 7:
          assertArrayEquals(new String[] { "1" }, entry.first);
          assertEquals(1, entry.second.size());
          assertArrayEquals(new String[] { "E", "1" }, entry.second.get(0));
          break;
        default:
          break;
      }
      ++i;
    }
    assertEquals(8, i);
  }
}