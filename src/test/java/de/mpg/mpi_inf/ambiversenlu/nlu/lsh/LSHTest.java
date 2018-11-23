package de.mpg.mpi_inf.ambiversenlu.nlu.lsh;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LSHTest {

  @Test public void testDictionary() throws InterruptedException, IOException, EntityLinkingDataAccessException {
    Set<String> names = new HashSet<>();
    names.add("The Who");
    names.add("Who");
    names.add("Pete Townshend");
    names.add("Pete Townshends");
    names.add("Jimmy Page");
    names.add("Jimmie Page");
    names.add("AIDA");
    names.add("AIDS");

    LSH<String> lsh = LSH.createLSH(names, new LSHStringNgramFeatureExtractor(3), 4, 6, 2);

    Set<String> items = lsh.getSimilarItems("Pete Townshend");
    assertEquals(2, items.size());
    Set<String> expected = new HashSet<>();
    expected.add("Pete Townshend");
    expected.add("Pete Townshends");
    assertEquals(expected, items);

    items = lsh.getSimilarItems("AIDA");
    assertEquals(1, items.size());

    items = lsh.getSimilarItems("Jimmy Page");
    assertEquals(2, items.size());
  }
}
