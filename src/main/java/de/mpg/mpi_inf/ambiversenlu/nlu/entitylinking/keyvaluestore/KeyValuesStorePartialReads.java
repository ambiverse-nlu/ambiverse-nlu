package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface KeyValuesStorePartialReads<T, U> {

  public void setup() throws IOException;

  public U get(T key, String... columns) throws IOException;

  public Map<T, U> getAll(Collection<T> keys, String... columns) throws IOException;
}