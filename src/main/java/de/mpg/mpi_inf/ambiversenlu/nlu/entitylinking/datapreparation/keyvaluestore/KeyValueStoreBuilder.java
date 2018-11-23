package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import java.io.IOException;

public interface KeyValueStoreBuilder {

  public boolean setup() throws IOException;

  public boolean addPair(byte[] key, byte[] value) throws IOException;

  public boolean update(byte[] key, byte[] value) throws IOException;

  public boolean build();
}
