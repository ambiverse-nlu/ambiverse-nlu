package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.Codec;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface KeyValueStore<T, U> {

  public void setup() throws IOException;

  public U get(T key) throws IOException;

  public Map<T, U> getAll(Collection<T> keys) throws IOException;

  public EntryIterator entryIterator();

  public Codec getCodec();

  public long countRows();

  public static interface EntryIterator {

    boolean hasNext() throws IOException;

    Entry next() throws IOException;
  }

  public static class Entry<T, U> {

    private T key;

    private U values;

    protected Entry(T key, U value) {
      this.key = key;
      this.values = value;
    }

    public T getKey() {
      return this.key;
    }

    public U getValue() {
      return this.values;
    }
  }
}
