package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.util.Arrays;

public class TsvUtils {

  /**
   * Get the array elements excluding those specified by range r, where 
   * r is an interval including the end: [start, end]. 
   *
   * @param entries
   * @param r
   * @return
   */
  public static String[] getElementsNotInRange(String[] entries, int[] r) {
    r = TsvUtils.transformRange(r, entries.length);
    int rangeLength = r[1] - r[0] + 1;
    String[] data = new String[entries.length - rangeLength];
    for (int i = 0; i < r[0]; ++i) {
      data[i] = entries[i];
    }
    for (int i = r[1] + 1; i < entries.length; ++i) {
      data[i - rangeLength] = entries[i];
    }
    return data;
  }

  /**
   * Get the array elements specified by range r, where 
   * r is an interval including the end: [start, end].  
   *
   * @param entries
   * @param r
   * @return
   */
  public static String[] getElementsInRange(String[] entries, int[] r) {
    r = TsvUtils.transformRange(r, entries.length);
    return Arrays.copyOfRange(entries, r[0], r[1] + 1);
  }

  /**
   * This will transform negative range offsets to positive ones.
   * @param r Range with potential negative offset.
   * @param origLength  Length of the original array. 
   */
  public static int[] transformRange(int[] r, int origLength) {
    int newStart = r[0];
    if (newStart < 0) {
      newStart = origLength + r[0];
    }
    int newEnd = r[1];
    if (newEnd < 0) {
      newEnd = origLength + newEnd;
    }

    if (newStart < 0 || newEnd < 0) {
      throw new IllegalArgumentException("Invalid negative offset exceeds original length");
    }
    if (newStart > newEnd) {
      throw new IllegalArgumentException("Invalid range, no content for [" + newStart + ", " + newEnd + "].");
    }

    return new int[] { newStart, newEnd };
  }

  public static int transformIndex(int i, int origLength) {
    if (i >= 0) {
      return i;
    }
    int newIndex = origLength + i;
    if (newIndex < 0 || newIndex > origLength) {
      throw new IllegalArgumentException("Index " + i + " is out of range for length " + origLength);
    }
    return newIndex;
  }

}
