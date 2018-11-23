package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import com.google.protobuf.Descriptors;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class SqlToKeyValueConverter {

  private static final Logger logger = LoggerFactory.getLogger(SqlToKeyValueConverter.class);

  static final int FETCH_SIZE = 100000;

  static int VIEW_NUMBER = 0;

  Connection connection;

  DatabaseKeyValueStore databaseKeyValueStore;

  KeyValueStoreBuilderFactory keyValueStoreBuilderFactory;

  public SqlToKeyValueConverter(Connection connection, DatabaseKeyValueStore databaseKeyValueStore,
      KeyValueStoreBuilderFactory keyValueStoreBuilderFactory) {
    this.connection = connection;
    this.databaseKeyValueStore = databaseKeyValueStore;
    this.keyValueStoreBuilderFactory = keyValueStoreBuilderFactory;
  }

  public abstract void generateKeyValueStore()
      throws InterruptedException, SQLException, Descriptors.DescriptorValidationException, IOException;

  public static class SqlToKeyValueConverterFactory {

    public static SqlToKeyValueConverter getSqlToKeyValueConverter(Connection connection, DatabaseKeyValueStore databaseKeyValueStore,
        KeyValueStoreBuilderFactory keyValueStoreBuilderFactory) {
      return new SqlToKeyValueConverterGeneric(connection, databaseKeyValueStore, keyValueStoreBuilderFactory);
    }
  }
}
