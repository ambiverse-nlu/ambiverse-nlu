package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import java.io.IOException;

public interface EncoderDecoder<T> {

  public abstract byte[] encode(T element) throws IOException;

  public abstract T decode(byte[] bytes) throws IOException;

  public static class EncoderDecoderFactory {

    public static <K> EncoderDecoder getEncoderDecoder(DatabaseKeyValueStore db, DatabaseKeyValueStore.Encoding encoding, Class clazz, boolean pairs,
        boolean key) {
      if (encoding.equals(DatabaseKeyValueStore.Encoding.KRYO)) {
        return getEncoderKryo(clazz);
      } else if (encoding.equals(DatabaseKeyValueStore.Encoding.INTS)) {
        if (!clazz.equals(Integer.class) && !clazz.equals(int[].class)) {
          throw new IllegalArgumentException("INTS encoding can only be used with int or int array");
        }
        if (pairs) {
          return getEncoderDecoderIntPairs(clazz);
        } else {
          return getEncoderInts(clazz);
        }
      } else {
        throw new IllegalArgumentException("Encoding " + encoding + " not supported");
      }
    }

    public static <K> EncoderDecoder getEncoderKryo(Class<K> type) {
      return new EncoderDecoderKryo<K>(type);
    }

    public static <K> EncoderDecoder getEncoderInts(Class<K> type) {
      return new EncoderDecoderInts<K>(type);
    }

    public static <K> EncoderDecoderInts<K> getEncoderDecoderIntPairs(Class<K> type) {
      return new EncoderDecoderInts<K>(type, true);
    }
  }
}
