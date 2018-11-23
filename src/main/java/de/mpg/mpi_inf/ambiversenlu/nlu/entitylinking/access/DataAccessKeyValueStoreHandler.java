package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore.CassandraStore;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DataAccessKeyValueStoreHandler {

  private static final Logger logger = LoggerFactory.getLogger(DataAccessKeyValueStoreHandler.class);

  private static DataAccessKeyValueStoreHandler ownInstance = new DataAccessKeyValueStoreHandler();

  public static DataAccessKeyValueStoreHandler singleton() {
    logger.debug("Creating singleton.");
    return ownInstance;
  }

  // holds the loaded maps
  private KeyValueStore[] loadedKeyValueStores;

  private DataAccessKeyValueStoreHandler() {
    loadedKeyValueStores = new KeyValueStore[DatabaseKeyValueStore.values().length];
    for (DatabaseKeyValueStore databaseKeyValueStore : DatabaseKeyValueStore.values()) {
      try {
        loadKeyValueStores(databaseKeyValueStore);
      } catch (Exception e) {
        logger.error("Could not load KeyValueStore: " + databaseKeyValueStore.getName() + " (" + e.getMessage() + ")");
      }
    }
  }

  public KeyValueStore getKeyValueStore(DatabaseKeyValueStore databaseKeyValueStore) throws IOException {
    logger.debug("Getting key-value store.");
    KeyValueStore keyValueStore = loadedKeyValueStores[databaseKeyValueStore.ordinal()];
    if (keyValueStore == null) {
      throw new IOException("KVStore not loaded.");
    }
    logger.debug("Key-value store {}.", keyValueStore);
    return keyValueStore;
  }

  private void loadKeyValueStores(DatabaseKeyValueStore databaseKeyValueStore) throws IOException {
    logger.debug("Loading KeyValueStore: " + databaseKeyValueStore.getName());
    KeyValueStore<byte[], byte[]> keyValueStore = null;
    Codec.CodecFactory cfactory = new Codec.CodecFactory();
    Codec codec = cfactory.getCodec(databaseKeyValueStore);
    switch (DataAccess.getAccessType()) {
      case cassandra:
        logger.debug("Loading Cassandra Store");
        keyValueStore = CassandraStore
            .createStore(byte[].class, byte[].class, databaseKeyValueStore.getName(), CassandraConfig.GENERIC_PRIMARY_KEY, codec);
        break;
    }
    if (keyValueStore == null) logger.warn("Could not load KeyValueStore '" + databaseKeyValueStore.getName() + "'.");
    else {
      keyValueStore.setup();
      logger.debug("KeyValueStore '{}' loaded.", databaseKeyValueStore.getName());
    }
    loadedKeyValueStores[databaseKeyValueStore.ordinal()] = keyValueStore;
  }

  public Codec getCodec(DatabaseKeyValueStore databaseKeyValueStore) {
    logger.debug("Getting codec.");
    return loadedKeyValueStores[databaseKeyValueStore.ordinal()].getCodec();
  }
}
