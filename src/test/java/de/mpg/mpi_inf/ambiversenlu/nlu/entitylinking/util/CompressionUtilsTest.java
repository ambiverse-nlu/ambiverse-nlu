package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class CompressionUtilsTest {

  @Test public void test() throws IOException {
    Random r = new Random();

    List<Integer> is = new LinkedList<Integer>();

    for (int i = 0; i < 13; i++) {
      is.add(r.nextInt());
    }

    Collections.sort(is);

    int[] test = CompressionUtils.uncompressIntegers(CompressionUtils.compressIntegers(is));

    for (int i = 0; i < test.length; i++) {
      assertEquals((int) is.get(i), test[i]);
    }

    is = new LinkedList<Integer>();
    test = CompressionUtils.uncompressIntegers(CompressionUtils.compressIntegers(is));
    for (int i = 0; i < test.length; i++) {
      assertEquals((int) is.get(i), test[i]);
    }

    is = new LinkedList<Integer>();
    is.add(0);
    test = CompressionUtils.uncompressIntegers(CompressionUtils.compressIntegers(is));
    for (int i = 0; i < test.length; i++) {
      assertEquals((int) is.get(i), test[i]);
    }

    is = new LinkedList<Integer>();
    is.add(1);
    test = CompressionUtils.uncompressIntegers(CompressionUtils.compressIntegers(is));
    for (int i = 0; i < test.length; i++) {
      assertEquals((int) is.get(i), test[i]);
    }

    int[] isArray = new int[] { -1, 59, 324235, 3245235, -1, -2, 0, 1, -1 };
    Arrays.sort(isArray);
    test = CompressionUtils.uncompressIntegers(CompressionUtils.sortAndCompressIntegers(isArray));
    for (int i = 0; i < test.length; i++) {
      assertEquals(isArray[i], test[i]);
    }

    int[] keys = new int[] { 7, 77, 6, 66, 5, 55, 4, 44, 3, 33, 2, 22, 1, 11 };
    test = CompressionUtils.uncompressPairs(CompressionUtils.sortAndCompressPairs(keys));
    int[] result = new int[] { 1, 11, 2, 22, 3, 33, 4, 44, 5, 55, 6, 66, 7, 77 };
    for (int i = 0; i < test.length; i++) {
      assertEquals(result[i], test[i]);
    }

  }
}
