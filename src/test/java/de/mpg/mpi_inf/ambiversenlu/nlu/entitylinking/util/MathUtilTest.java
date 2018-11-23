package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MathUtilTest {

  private static final double EPSILON = 0.001;

  @Test public void testComputeJaccardSimilarity() {
    Set<Integer> a = new HashSet<>();
    a.add(1);
    Set<Integer> b = new HashSet<>();
    b.add(1);
    assertEquals(1.0, MathUtil.computeJaccardSimilarity(a, b), EPSILON);

    a.clear();
    b.clear();
    a.add(1);
    b.add(2);
    assertEquals(0.0, MathUtil.computeJaccardSimilarity(a, b), EPSILON);

    a.clear();
    b.clear();
    a.add(1);
    a.add(2);
    b.add(2);
    b.add(3);
    assertEquals(0.333, MathUtil.computeJaccardSimilarity(a, b), EPSILON);

    a.clear();
    b.clear();
    a.add(1);
    a.add(2);
    a.add(3);
    b.add(2);
    b.add(3);
    assertEquals(0.666, MathUtil.computeJaccardSimilarity(a, b), EPSILON);

    a.clear();
    b.clear();
    a.add(1);
    a.add(2);
    a.add(3);
    b.add(2);
    b.add(3);
    b.add(4);
    b.add(5);
    assertEquals(0.4, MathUtil.computeJaccardSimilarity(a, b), EPSILON);
  }

  @Test public void testTfidf() {
    Map<String, Integer> tfs = new HashMap<>();
    tfs.put("this", 1);
    tfs.put("is", 1);
    tfs.put("another", 2);
    tfs.put("example", 3);

    assertEquals(2.748, MathUtil.tfidf(tfs.get("example"), 1, 5), EPSILON);
  }
}
