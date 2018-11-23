package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KeyValueStoreBuilderFactory {

  private DataAccess.type keyValueStoreType;

  private static final Logger logger = LoggerFactory.getLogger(KeyValueStoreBuilderFactory.class);

  public KeyValueStoreBuilderFactory(DataAccess.type keyValueStoreType) {
    this.keyValueStoreType = keyValueStoreType;
  }

  protected KeyValueStoreBuilder newKeyValueStoreBuilder(DatabaseKeyValueStore databaseKeyValueStore) throws IOException {
    KeyValueStoreBuilder keyValueStoreBuilder = null;
    switch (keyValueStoreType) {
      case cassandra:
        keyValueStoreBuilder = new CassandraKeyValueStoreBuilder(databaseKeyValueStore.getName());
        break;
    }
    return keyValueStoreBuilder;
  }
}
