package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EncoderDecoderInts<T> implements EncoderDecoder<T> {

  private static final Logger logger = LoggerFactory.getLogger(EncoderDecoderInts.class);

  boolean array = false;

  Class<T> elementType;

  boolean pairs = false;

  public EncoderDecoderInts(Class<T> elementType) {
    if (!elementType.equals(Integer.class) && !(array = elementType.equals(int[].class))) {
      throw new IllegalArgumentException();
    }
    this.elementType = elementType;
    logger.debug("Ints Encoder created");
  }

  public EncoderDecoderInts(Class<T> elementType, boolean b) {
    this(elementType);
    pairs = b;
  }

  public byte[] encode(T element) throws IOException {
    logger.debug("Encoding using Ints encoder.");
    if (!array) {
      return CompressionUtils.compressIntegers((Integer) element); //try not to autobox
    } else {
      if (pairs) {
        return CompressionUtils.sortAndCompressPairs((int[]) element);
      } else {
        return CompressionUtils.sortAndCompressIntegers((int[]) element);
      }
    }
  }

  public T decode(byte[] bytes) throws IOException {
    logger.debug("Decoding using ints decoder.");
    int[] result;
    if (pairs) {
      result = CompressionUtils.uncompressPairs(bytes);
    } else {
      result = CompressionUtils.uncompressIntegers(bytes);
    }
    if (!array) {
      return elementType.cast(result[0]);
    } else {
      return elementType.cast(result);
    }
  }
}
