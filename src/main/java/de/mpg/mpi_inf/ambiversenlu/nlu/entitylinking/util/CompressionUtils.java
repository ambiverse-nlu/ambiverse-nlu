package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;

/**
 * VInt compression taken from WritableUtils in Apache Hadoop 1.0.3!
 *
 */
public class CompressionUtils {

  /**
   * Convenicence method call for compressPairs - makes sure that the first list is sorted
   *
   * @param is  Input integers (does NOT need to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] sortAndCompressPairs(int[] is) throws IOException {
    if (is == null || is.length == 0) {
      return new byte[0];
    }

    if (is.length % 2 != 0) {
      throw new IllegalArgumentException("Input array should be of even length");
    }

    int[] first = new int[is.length / 2];
    int[] second = new int[is.length / 2];

    for (int i = 0; i < is.length / 2; i++) {
      first[i] = is[i * 2];
      second[i] = is[i * 2 + 1];
    }

    final Iterator<AbstractMap.SimpleEntry<Integer, Integer>> sorted = IntStream.range(0, first.length)
        .mapToObj(i -> new AbstractMap.SimpleEntry<Integer, Integer>(first[i], second[i])).sorted(Comparator.comparingInt(b -> b.getKey()))
        .iterator();

    int index = 0;
    while (sorted.hasNext()) {
      Map.Entry<Integer, Integer> entry = sorted.next();
      first[index] = entry.getKey();
      second[index] = entry.getValue();
      index++;
    }

    return compressPairs(first, second);
  }

  public static void deltaEncoding(int[] array) throws IOException {
    int base = array[0];
    for (int i = 1; i < array.length; i++) {
      int next = array[i];
      int gap = next - base;
      array[i] = gap;
      base = next;
    }
  }

  public static void deltaUncompress(int[] array) {
    int base = array[0];
    for (int i = 1; i < array.length; i++) {
      int gap = array[i];
      array[i] = base + gap;
      base = array[i];
    }
  }

  private static byte[] compressPairs(int[] keys, int[] values) throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream das = new DataOutputStream(baos);

    int[] first = Arrays.copyOf(keys, keys.length);
    int[] second = Arrays.copyOf(values, values.length);

    CompressionUtils.writeVInt(das, first.length + second.length);

    deltaEncoding(first);
    for (int i : first) {
      CompressionUtils.writeVInt(das, i);
    }

    for (int i = 0; i < second.length; i++) {
      CompressionUtils.writeVInt(das, second[i]);
    }

    return baos.toByteArray();
  }

  /**
   * Convenicence method call for compressIntegers - makes sure that the list is sorted
   *
   * @param is  Input integers (does NOT need to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] sortAndCompressIntegers(int[] is) throws IOException {
    Arrays.sort(is);
    return compressIntegers(is);
  }

  /**
   * Takes an integer and converts it to compressed byte array
   * using variable length integers and gap encoding
   *
   * @param is  Input integers (needs to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] compressIntegers(int is) throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream das = new DataOutputStream(baos);

    // put length of docids
    CompressionUtils.writeVInt(das, 1);

    // use gap encoding. first int is base, rest is increment
    int base = is;
    CompressionUtils.writeVInt(das, base);
    return baos.toByteArray();
  }

  /**
   * Takes a sorted array of integers, converts to compressed byte array
   * using variable length integers and gap encoding
   *
   * @param is  Input integers (needs to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] compressIntegers(int[] is) throws IOException {
    if (is == null || is.length == 0) {
      return new byte[0];
    }

    int[] copy = Arrays.copyOf(is, is.length);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream das = new DataOutputStream(baos);

    // put length of docids
    CompressionUtils.writeVInt(das, copy.length);

    deltaEncoding(copy);
    for (int i : copy) {
      CompressionUtils.writeVInt(das, i);
    }

    return baos.toByteArray();
  }

  /**
   * Convenicence method call for compressIntegers - makes sure that the list is sorted
   *
   * @param is  Input integers (does NOT need to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] sortAndCompressIntegers(List<Integer> is) throws IOException {
    Collections.sort(is);
    return compressIntegers(is);
  }

  /**
   * Takes a sorted collection of integers, converts to compressed byte array
   * using variable length integers and gap encoding
   *
   * @param is  Input integers (needs to be sorted!)
   * @return compressed byte array
   * @throws IOException
   */
  public static byte[] compressIntegers(List<Integer> is) throws IOException {
    if (is == null || is.isEmpty()) {
      return new byte[0];
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream das = new DataOutputStream(baos);

    // put length of docids
    CompressionUtils.writeVInt(das, is.size());

    // use gap encoding. first int is base, rest is increment
    Iterator<Integer> itr = is.iterator();
    int base = itr.next();
    CompressionUtils.writeVInt(das, base);

    while (itr.hasNext()) {
      int next = itr.next();
      int gap = next - base;
      CompressionUtils.writeVInt(das, gap);
      base = next;
    }

    return baos.toByteArray();
  }

  /**
   * Uncompresses a byte array compressed by compressIntegers()
   *
   * @param data  compressed integers
   * @return integer array
   * @throws IOException
   */
  public static int[] uncompressIntegers(byte[] data) throws IOException {
    if (data == null || data.length == 0) {
      return new int[0];
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    int length = CompressionUtils.readVInt(dis);

    int[] integers = new int[length];

    for (int i = 0; i < length; i++) {
      integers[i] = CompressionUtils.readVInt(dis);
    }
    deltaUncompress(integers);

    return integers;
  }

  public static int[] uncompressPairs(byte[] data) throws IOException {
    if (data == null || data.length == 0) {
      return new int[0];
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    int length = CompressionUtils.readVInt(dis);

    if (length % 2 != 0) {
      throw new IllegalArgumentException("Input array should be of even length");
    }

    int[] integers = new int[length];

    // at least one integer is present, read as base
    int base = CompressionUtils.readVInt(dis);
    integers[0] = base;

    for (int i = 1; i < length; i++) {
      if (i < length / 2) {
        int gap = CompressionUtils.readVInt(dis);
        integers[i * 2] = base + gap;
        base = integers[i * 2];
      } else {
        int value = CompressionUtils.readVInt(dis);
        integers[(i - length / 2) * 2 + 1] = value;
      }
    }
    return integers;
  }

  /**
   * Serializes an integer to a binary stream with zero-compressed encoding.
   * For -120 <= i <= 127, only one byte is used with the actual value.
   * For other values of i, the first byte value indicates whether the
   * integer is positive or negative, and the number of bytes that follow.
   * If the first byte value v is between -121 and -124, the following integer
   * is positive, with number of bytes that follow are -(v+120).
   * If the first byte value v is between -125 and -128, the following integer
   * is negative, with number of bytes that follow are -(v+124). Bytes are
   * stored in the high-non-zero-byte-first order.
   *
   * @param stream Binary output stream
   * @param i Integer to be serialized
   * @throws java.io.IOException
   */
  public static void writeVInt(DataOutput stream, int i) throws IOException {
    writeVLong(stream, i);
  }

  /**
   * Serializes a long to a binary stream with zero-compressed encoding.
   * For -112 <= i <= 127, only one byte is used with the actual value.
   * For other values of i, the first byte value indicates whether the
   * long is positive or negative, and the number of bytes that follow.
   * If the first byte value v is between -113 and -120, the following long
   * is positive, with number of bytes that follow are -(v+112).
   * If the first byte value v is between -121 and -128, the following long
   * is negative, with number of bytes that follow are -(v+120). Bytes are
   * stored in the high-non-zero-byte-first order.
   *
   * @param stream Binary output stream
   * @param i Long to be serialized
   * @throws java.io.IOException
   */
  public static void writeVLong(DataOutput stream, long i) throws IOException {
    if (i >= -112 && i <= 127) {
      stream.writeByte((byte) i);
      return;
    }

    int len = -112;
    if (i < 0) {
      i ^= -1L; // take one's complement'
      len = -120;
    }

    long tmp = i;
    while (tmp != 0) {
      tmp = tmp >> 8;
      len--;
    }

    stream.writeByte((byte) len);

    len = (len < -120) ? -(len + 120) : -(len + 112);

    for (int idx = len; idx != 0; idx--) {
      int shiftbits = (idx - 1) * 8;
      long mask = 0xFFL << shiftbits;
      stream.writeByte((byte) ((i & mask) >> shiftbits));
    }
  }

  /**
   * Reads a zero-compressed encoded long from input stream and returns it.
   * @param stream Binary input stream
   * @throws java.io.IOException
   * @return deserialized long from stream.
   */
  public static long readVLong(DataInput stream) throws IOException {
    byte firstByte = stream.readByte();
    int len = decodeVIntSize(firstByte);
    if (len == 1) {
      return firstByte;
    }
    long i = 0;
    for (int idx = 0; idx < len - 1; idx++) {
      byte b = stream.readByte();
      i = i << 8;
      i = i | (b & 0xFF);
    }
    return (isNegativeVInt(firstByte) ? (i ^ -1L) : i);
  }

  /**
   * Reads a zero-compressed encoded integer from input stream and returns it.
   * @param stream Binary input stream
   * @throws java.io.IOException
   * @return deserialized integer from stream.
   */
  public static int readVInt(DataInput stream) throws IOException {
    return (int) readVLong(stream);
  }

  /**
   * Given the first byte of a vint/vlong, determine the sign
   * @param value the first byte
   * @return is the value negative
   */
  public static boolean isNegativeVInt(byte value) {
    return value < -120 || (value >= -112 && value < 0);
  }

  /**
   * Parse the first byte of a vint/vlong to determine the number of bytes
   * @param value the first byte of the vint/vlong
   * @return the total number of bytes (1 to 9)
   */
  public static int decodeVIntSize(byte value) {
    if (value >= -112) {
      return 1;
    } else if (value < -120) {
      return -119 - value;
    }
    return -111 - value;
  }

  /**
   * Converts an integer array to a byte array
   *
   * @param is  array of ints
   * @return array of bytes representing the ints
   */
  public static byte[] integerArrayToByteArray(int[] is) {
    ByteBuffer bb = ByteBuffer.allocate(is.length * 4);
    for (int i : is) {
      bb.putInt(i);
    }
    return bb.array();
  }

  /**
   * Converts a single integer into a byte array.
   *
   * @param i Integer to convert.
   * @return byte[] representation.
   */
  public static byte[] integerToByteArray(int i) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.putInt(i);
    return bb.array();
  }

  /**
   * Converts the byte[] produced by integerArrayToByteArray back to int[]
   *
   * @param bs  byte[] produced by integerArrayToByteArray
   * @return int[] version of the bytes
   */
  public static int[] byteArrayToIntegerArray(byte[] bs) {
    int[] is = new int[bs.length / 4];

    ByteBuffer bb = ByteBuffer.wrap(bs);
    for (int i = 0; i < is.length; i++) {
      is[i] = bb.getInt();
    }

    return is;
  }

  /**
   * Converts a byte array of length 4 to an integer. Assumes that 
   * this is of the correct length, make sure to pass only byte[] that
   * were converted by integerToByteArray()
   *
   * @param bs  byte[] of length 4 representing an integer
   * @return int value of the byte[]
   */
  public static int byteArrayToInteger(byte[] bs) {
    ByteBuffer bb = ByteBuffer.wrap(bs);
    return bb.getInt();
  }
}
