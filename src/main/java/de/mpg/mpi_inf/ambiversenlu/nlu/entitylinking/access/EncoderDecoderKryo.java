package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CompressionUtils;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public class EncoderDecoderKryo<T> implements EncoderDecoder<T> {

  private static final Logger logger = LoggerFactory.getLogger(EncoderDecoderKryo.class);

  private static ThreadLocal<Kryo> kryos;

  private static int MAX_OBJECT_SIZE = 250000; //The maximum size of the object allowed in the pool

  Class<T> objectType;

  public EncoderDecoderKryo(Class<T> objectType) {
    this.objectType = objectType;

    if (kryos == null) {
      kryos = new ThreadLocal<Kryo>(){
        //Max capacity is needed, to allow automatically cleaning, if the maximum is reached clean is called to remove unused references

        protected Kryo initialValue() {
          Kryo kryo = new Kryo();
          kryo.setRegistrationRequired(true);
          //  All primitives, primitive wrappers, and String are registered by default.

          kryo.register(KeyValueStoreRow.class, new Serializer<KeyValueStoreRow>() {
            //Problems sertializing Object with Kryo, this is a worl around. In principle it should work out of the box in kryo by adding @FieldSerializer.Bind(DefaultArraySerializers.ObjectArraySerializer.class) on top of the variable

            @Override public void write(Kryo kryo, Output output, KeyValueStoreRow keyValueStoreRow) { //work around
              byte[] data = SerializationUtils.serialize(keyValueStoreRow.values);
              int size = data.length;
              output.writeInt(size);
              output.writeBytes(data);
            }

            @Override public KeyValueStoreRow read(Kryo kryo, Input input, Class<? extends KeyValueStoreRow> aClass) {
              int size = input.readInt();
              Object[] values = (Object[]) SerializationUtils.deserialize(input.readBytes(size));
              return new KeyValueStoreRow(values);
            }
          }, 9);

          kryo.register(KeyValueStoreRow[].class, 10);
          kryo.register(String[].class, 11);
          kryo.register(double[].class, 12);

          kryo.register(TIntDoubleHashMap.class, new Serializer<TIntDoubleHashMap>() {
            public void write (Kryo kryo, Output output, TIntDoubleHashMap object) {
              int size = object.size();
              output.writeInt(size);

              int[] keys = new int[size];
              double[] values = new double[size];

              int in = 0;
              TIntDoubleIterator it = object.iterator();
              while(it.hasNext()) {
                it.advance();
                keys[in] = it.key();
                values[in] = it.value();
                in++;
              }

              final Iterator<AbstractMap.SimpleEntry<Integer, Double>> sorted = IntStream.range(0, keys.length)
                  .mapToObj(i -> new AbstractMap.SimpleEntry<>(keys[i], values[i]))
                  .sorted(Comparator.comparingInt(b -> b.getKey())).iterator();

              int index = 0;
              while(sorted.hasNext()) {
                Map.Entry<Integer, Double> entry = sorted.next();
                keys[index] = entry.getKey();
                values[index] = entry.getValue();
                index++;
              }

              try {
                CompressionUtils.deltaEncoding(keys);
              } catch (IOException e) {
                e.printStackTrace();
              }

              output.writeInts(keys, 0, keys.length);
              output.writeDoubles(values, 0, values.length);
              kryo.writeClassAndObject(output, object);
            }

            public TIntDoubleHashMap read (Kryo kryo, Input input, Class<? extends TIntDoubleHashMap> type) {
              int size = input.readInt();
              int[] keys = input.readInts(size);
              double[] values = input.readDoubles(size);
              CompressionUtils.deltaUncompress(keys);
              TIntDoubleHashMap object = new TIntDoubleHashMap(size, Constants.DEFAULT_LOAD_FACTOR);
              for(int i = 0; i < size; i++) {
                object.put(keys[i], values[i]);
              }
              return object;
            }
          }, 13);
          return kryo;
        }
      };
    }
  }

  @Override public byte[] encode(T element) {
    logger.debug("Encoding using kryo.");
    Kryo kryo = kryos.get();
    int size = 0;
    Output output = new Output(0, 500000000);
    try {
      kryo.writeObject(output, element);
      //logger.info("Object size: " + output.toBytes().length);
      logger.debug("Encoding finished.");
      byte [] result = output.toBytes();
      size = result.length;
      return result;
    } finally {
      output.close();
      if(size < MAX_OBJECT_SIZE) { //Small objects will be put back into the pull, larger ones will be thrown away
        kryos.remove();
      }
    }

  }

  @Override public T decode(byte[] bytes) {
    logger.debug("Decoding using kryo.");
    int size = bytes.length;
    Input input = new Input(bytes);
    Kryo kryo = kryos.get();
    try {
      T result = kryo.readObject(input, objectType);
      return result;
    } finally {
      input.close();
      if(size < MAX_OBJECT_SIZE) { //Small objects will be put back into the pull, larger ones will be thrown away
        kryos.remove();
      }
    }
  }

}