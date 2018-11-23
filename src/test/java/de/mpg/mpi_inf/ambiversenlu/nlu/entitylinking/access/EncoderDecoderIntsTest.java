package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EncoderDecoderIntsTest {

  @Test public void test() throws IOException {

    EncoderDecoder<Integer> intencoder = EncoderDecoder.EncoderDecoderFactory.getEncoderInts(Integer.class);
    int value = 894738047;
    byte[] bytes = intencoder.encode(value);
    int nValue = intencoder.decode(bytes);
    assertEquals(value, nValue);

    EncoderDecoder<int[]> intArrayEncoder = EncoderDecoder.EncoderDecoderFactory.getEncoderInts(int[].class);
    int[] intArray = new int[] { 6, 7, 8, 4, 7, 4, 0, 1 };
    bytes = intArrayEncoder.encode(intArray);
    int[] nIntArray = intArrayEncoder.decode(bytes);
    for (int i = 0; i < intArray.length; i++) {
      assertEquals(intArray[i], nIntArray[i]);
    }

    EncoderDecoder<int[]> intArrayEncoderPairs = EncoderDecoder.EncoderDecoderFactory.getEncoderDecoderIntPairs(int[].class);
    bytes = intArrayEncoderPairs.encode(intArray);
    nIntArray = intArrayEncoderPairs.decode(bytes);
    for (int i = 0; i < intArray.length; i++) {
      assertEquals(intArray[i], nIntArray[i]);
    }

  }

}
