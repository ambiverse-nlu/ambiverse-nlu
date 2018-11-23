package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * This tests all the demo examples for correct outcome.
 * Useful as a safety net :)
 *
 */
public class EncoderDecoderKryoTest {

  @Test public void test() throws IOException {
    EncoderDecoderKryo<Integer> encoderInteger = new EncoderDecoderKryo(Integer.class);
    int a = 845739038;
    byte[] bytes = encoderInteger.encode(a);
    int resultInt = encoderInteger.decode(bytes);
    assertEquals(a, resultInt);

    EncoderDecoderKryo<Double> encoderDouble = new EncoderDecoderKryo(Double.class);
    double b = 845739038;
    bytes = encoderDouble.encode(b);
    double resultDouble = encoderDouble.decode(bytes);
    assertEquals(b, resultDouble, 0.00001);

    EncoderDecoderKryo<String> encoderString = new EncoderDecoderKryo(String.class);
    String str = "hola";
    bytes = encoderString.encode(str);
    String resultString = encoderString.decode(bytes);
    assertEquals(str, resultString);

    EncoderDecoderKryo<String[]> encoderStringArray = new EncoderDecoderKryo(String[].class);
    String[] strArray = new String[] { "hola", "y", "chau" };
    bytes = encoderStringArray.encode(strArray);
    String[] resultStringArray = encoderStringArray.decode(bytes);
    for (int i = 0; i < strArray.length; i++) {
      assertEquals(strArray[i], resultStringArray[i]);
    }

    EncoderDecoderKryo<KeyValueStoreRow[]> keyvalueStoreRowArray = new EncoderDecoderKryo(KeyValueStoreRow[].class);
    Object[] elements = new Object[] { a, b, str };
    KeyValueStoreRow kvs1 = new KeyValueStoreRow(elements);
    Object[] elements2 = new Object[] { str, b, a };
    KeyValueStoreRow kvs2 = new KeyValueStoreRow(elements2);
    KeyValueStoreRow[] kvsr = new KeyValueStoreRow[] { kvs1, kvs2 };
    bytes = keyvalueStoreRowArray.encode(kvsr);
    KeyValueStoreRow[] kvsrResultl = keyvalueStoreRowArray.decode(bytes);

    assertEquals(kvsr[0].getInt(0), kvsrResultl[0].getInt(0));
    assertEquals(kvsr[0].getDouble(1), kvsrResultl[0].getDouble(1), 0.00001);
    assertEquals(kvsr[0].getString(2), kvsr[0].getString(2));

    assertEquals(kvsr[1].getInt(2), kvsrResultl[1].getInt(2));
    assertEquals(kvsr[1].getDouble(1), kvsrResultl[1].getDouble(1), 0.00001);
    assertEquals(kvsr[1].getString(0), kvsr[1].getString(0));

    EncoderDecoderKryo<TIntDoubleHashMap> tintDoubleHashMap = new EncoderDecoderKryo(TIntDoubleHashMap.class);
    TIntDoubleHashMap object = new TIntDoubleHashMap(2, Constants.DEFAULT_LOAD_FACTOR);
    object.put(7, 8.19);
    object.put(15, 7.9);
    bytes = tintDoubleHashMap.encode(object);
    TIntDoubleHashMap result = tintDoubleHashMap.decode(bytes);
    TIntDoubleIterator it = result.iterator();
    int index = 0;
    while (it.hasNext()) {
      it.advance();
      if (index == 0) {
        assertEquals(7, it.key());
        assertEquals(it.value(), 8.19, 0.00001);
      } else {
        assertEquals(15, it.key());
        assertEquals(it.value(), 7.9, 0.00001);
      }
      index++;
    }

  }

}