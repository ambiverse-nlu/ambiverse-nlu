package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import com.google.protobuf.Descriptors;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class KeyValueStorePreparator {

  private static final Logger logger = LoggerFactory.getLogger(KeyValueStorePreparator.class);

  private List<DatabaseKeyValueStore> keyValueStoreConfigs;

  private Connection connection;

  KeyValueStoreBuilderFactory keyValueStoreBuilderFactory;

  private KeyValueStorePreparator(DataAccess.type keyValueStoreType, Connection connection, List<DatabaseKeyValueStore> keyValueStoreConfigs) {
    this.connection = connection;
    this.keyValueStoreConfigs = keyValueStoreConfigs;
    keyValueStoreBuilderFactory = new KeyValueStoreBuilderFactory(keyValueStoreType);
  }

  public static KeyValueStorePreparator getDefault(DataAccess.type keyValueStoreType) throws SQLException, IOException {
    Connection connection = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    return new KeyValueStorePreparator(keyValueStoreType, connection, Arrays.asList(DatabaseKeyValueStore.values()));
  }

  public void generateKeyValueStores() throws SQLException, Descriptors.DescriptorValidationException, IOException, InterruptedException {
    for (DatabaseKeyValueStore databaseKeyValueStore : keyValueStoreConfigs) {
      logger.info("Generate KeyValueStore '" + databaseKeyValueStore.getName() + "' from '" + databaseKeyValueStore.getSource() + "' with "
          + databaseKeyValueStore.getKeys() + " as key(s)");
      try {
        SqlToKeyValueConverter converter = SqlToKeyValueConverter.SqlToKeyValueConverterFactory
            .getSqlToKeyValueConverter(connection, databaseKeyValueStore, keyValueStoreBuilderFactory);
        converter.generateKeyValueStore();
      } catch (Exception e) {
        e.printStackTrace();
        throw new IOException("Couldn't create KeyValueStore '" + databaseKeyValueStore.getName() + "' (" + e.getMessage() + ")");
      }
    }
  }
}