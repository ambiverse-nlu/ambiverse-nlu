package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga)

 Some utility methods for arrays
 */
public class ArrayUtils {

  /**
   * Returns the size of the intersection of two SORTED arrays.
   * The arrays NEED TO BE PASSED SORTED.
   *
   * @param a First array
   * @param b Second array
   * @return Size of intersection of a and b
   */
  public static int intersectArrays(int[] a, int[] b) {
    int intersectCount = 0;

    int aIndex = 0;
    int bIndex = 0;

    while (aIndex < a.length && bIndex < b.length) {
      if (a[aIndex] == b[bIndex]) {
        intersectCount++;

        aIndex++;
        bIndex++;
      } else if (a[aIndex] < b[bIndex]) {
        aIndex++;
      } else {
        bIndex++;
      }
    }

    return intersectCount;
  }
}
