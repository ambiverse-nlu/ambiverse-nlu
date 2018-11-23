package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import gnu.trove.map.hash.TIntDoubleHashMap;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CollectionUtilsTest {

  @Test public void test() throws IOException {
    List<String[]> data = new ArrayList<String[]>();
    data.add(new String[] { "2", "A" });
    data.add(new String[] { "1", "B" });
    data.add(new String[] { "1", "A" });
    Map<String, List<String[]>> grouped = CollectionUtils.groupData(data, new int[] { 1, 1 });
    assertEquals(2, grouped.size());
    List<String[]> aList = grouped.get("A");
    assertEquals(2, aList.size());
    assertArrayEquals(new String[] { "2" }, aList.get(0));
    assertArrayEquals(new String[] { "1" }, aList.get(1));
    List<String[]> bList = grouped.get("B");
    assertEquals(1, bList.size());
    assertArrayEquals(new String[] { "1" }, bList.get(0));

    data = new ArrayList<String[]>();
    data.add(new String[] { "4", "A" });
    grouped = CollectionUtils.groupData(data, new int[] { -1, -1 });
    assertEquals(1, grouped.size());
    aList = grouped.get("A");
    assertArrayEquals(new String[] { "4" }, aList.get(0));
  }

  @Test public void testBigger() {
    List<String[]> data = new ArrayList<String[]>();
    data.add(new String[] { "2", "A" });
    data.add(new String[] { "1", "B" });
    data.add(new String[] { "3", "A" });
    data.add(new String[] { "4", "A" });
    data.add(new String[] { "5", "B" });
    data.add(new String[] { "6", "B" });
    data.add(new String[] { "6", "C" });
    data.add(new String[] { "7", "D" });
    data.add(new String[] { "8", "Z" });
    data.add(new String[] { "45", "AFsdFG" });
    data.add(new String[] { "3", "AfADF" });
    data.add(new String[] { "435", "Agdf" });
    data.add(new String[] { "435", "Asdgff" });
    data.add(new String[] { "145", "A" });
    data.add(new String[] { "1423", "A" });
    data.add(new String[] { "1566", "A" });
    data.add(new String[] { "17457", "fsdf gsadgd" });
    Map<String, List<String[]>> grouped = CollectionUtils.groupData(data, new int[] { -1, -1 });
    assertEquals(10, grouped.size());
    List<String[]> bList = grouped.get("B");
    assertEquals(3, bList.size());
    grouped = CollectionUtils.groupData(data, new int[] { 0, 0 });
    assertEquals(14, grouped.size());
  }

  @Test public void testNormalizeScores() {
    Map<Integer, Double> scores = new HashMap<Integer, Double>();
    scores.put(0, 30.0);
    scores.put(1, 70.0);
    Map<Integer, Double> norm = CollectionUtils.normalizeValuesToSum(scores);
    assertEquals(0.3, norm.get(0), 0.001);
    assertEquals(0.7, norm.get(1), 0.001);

    scores = new HashMap<Integer, Double>();
    scores.put(0, 9.0);
    norm = CollectionUtils.normalizeValuesToSum(scores);
    assertEquals(1.0, norm.get(0), 0.001);
  }

  @Test public void testBisectSearch() {
    double[] bounds = new double[] { 0.1, 0.1000015, 0.101, 0.3, 0.5, 1.0 };
    assertEquals(0, CollectionUtils.bisectSearch(bounds, 0.0));
    assertEquals(0, CollectionUtils.bisectSearch(bounds, 0.05));
    assertEquals(1, CollectionUtils.bisectSearch(bounds, 0.1));
    assertEquals(1, CollectionUtils.bisectSearch(bounds, 0.100001));
    assertEquals(2, CollectionUtils.bisectSearch(bounds, 0.100002));
    assertEquals(3, CollectionUtils.bisectSearch(bounds, 0.2));
    assertEquals(4, CollectionUtils.bisectSearch(bounds, 0.4));
    assertEquals(5, CollectionUtils.bisectSearch(bounds, 0.9));
    assertEquals(5, CollectionUtils.bisectSearch(bounds, 1.0));
  }

  @Test public void testConditionalElement() {
    TIntDoubleHashMap elements = new TIntDoubleHashMap();
    elements.put(0, 0.1);
    elements.put(1, 0.3);
    elements.put(2, 0.05);
    elements.put(3, 0.15);
    elements.put(4, 0.4);

    Random rand = new Random(1337);
    TIntDoubleHashMap counts = new TIntDoubleHashMap();
    for (int i = 0; i < 1000000; ++i) {
      int chosen = CollectionUtils.getConditionalElement(elements, rand);
      counts.adjustOrPutValue(chosen, 1.0, 1.0);
    }
    TIntDoubleHashMap actualProbs = CollectionUtils.normalizeValuesToSum(counts);
    assertEquals(0.1, actualProbs.get(0), 0.001);
    assertEquals(0.3, actualProbs.get(1), 0.001);
    assertEquals(0.05, actualProbs.get(2), 0.001);
    assertEquals(0.15, actualProbs.get(3), 0.001);
    assertEquals(0.4, actualProbs.get(4), 0.001);
  }

  @Test public void testGetAllItemCombinations() {
    Set<List<Integer>> should = new HashSet<>();
    should.add(Arrays.asList(new Integer[] { 1 }));
    assertEquals(should, CollectionUtils.getAllItemCombinations(new Integer[] { 1 }, 1));

    should = new HashSet<>();
    should.add(Arrays.asList(new Integer[] { 1, 2 }));
    assertEquals(should, CollectionUtils.getAllItemCombinations(new Integer[] { 1, 2 }, 2));

    should = new HashSet<>();
    should.add(Arrays.asList(new Integer[] { 1, 2 }));
    should.add(Arrays.asList(new Integer[] { 2, 3 }));
    should.add(Arrays.asList(new Integer[] { 1, 3 }));
    assertEquals(should, CollectionUtils.getAllItemCombinations(new Integer[] { 1, 2, 3 }, 2));

    should = new HashSet<>();
    should.add(Arrays.asList(new Integer[] { 1 }));
    should.add(Arrays.asList(new Integer[] { 2 }));
    should.add(Arrays.asList(new Integer[] { 3 }));
    assertEquals(should, CollectionUtils.getAllItemCombinations(new Integer[] { 1, 2, 3 }, 1));

    should = new HashSet<>();
    should.add(Arrays.asList(new Integer[] { 1, 2, 3 }));
    assertEquals(should, CollectionUtils.getAllItemCombinations(new Integer[] { 1, 2, 3 }, 3));
  }
}