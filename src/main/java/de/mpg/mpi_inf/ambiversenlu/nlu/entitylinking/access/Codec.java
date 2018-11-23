package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Codec<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(Codec.class);

  private EncoderDecoder<K> keyCodec;

  private EncoderDecoder<V> valueCodec;

  private <K, V> Codec(EncoderDecoder keyDecoder, EncoderDecoder valueDecoder) {
    this.keyCodec = keyDecoder;
    this.valueCodec = valueDecoder;
  }

  public byte[] encodeKey(K key) throws IOException {
    logger.debug("Encoding key.");
    return keyCodec.encode(key);
  }

  public byte[] encodeValue(V value) throws IOException {
    logger.debug("Encoding value.");
    return valueCodec.encode(value);
  }

  public K decodeKey(byte[] key) throws IOException {
    logger.debug("Decode key.");
    return keyCodec.decode(key);
  }

  public V decodeValue(byte[] value) throws IOException {
    logger.debug("Decode value.");
    return valueCodec.decode(value);
  }

  public static class CodecFactory {

    public static <K1, V1> Codec getCodec(DatabaseKeyValueStore db) {
      EncoderDecoder.EncoderDecoderFactory factory = new EncoderDecoder.EncoderDecoderFactory();
      EncoderDecoder<K1> keyEnDe = factory.getEncoderDecoder(db, db.getKeyEncoding(), db.getKeyClass(), false, true);
      EncoderDecoder<V1> valueEnDe = factory.getEncoderDecoder(db, db.getValueEncoding(), db.getValueClass(), db.isValueIntPairs(), false);
      return new Codec(keyEnDe, valueEnDe);
    }
  }
}
