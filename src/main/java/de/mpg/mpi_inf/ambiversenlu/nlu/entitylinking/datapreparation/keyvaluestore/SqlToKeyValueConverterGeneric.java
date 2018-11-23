package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.protobuf.Descriptors;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.Codec;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.KeyValueStoreRow;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.PostgresUtil;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SqlToKeyValueConverterGeneric extends SqlToKeyValueConverter {

  private static final Logger logger = LoggerFactory.getLogger(SqlToKeyValueConverterGeneric.class);

  @Override public void generateKeyValueStore()
      throws InterruptedException, SQLException, Descriptors.DescriptorValidationException, IOException {
    if (databaseKeyValueStore.isSourceTable()) {
      logger.info("Generating from source table");

      databaseKeyValueStore.isSourceSorted();
      databaseKeyValueStore.getSource();
      databaseKeyValueStore.getKeys();

      fromTable(connection, databaseKeyValueStore.getSource(), databaseKeyValueStore.getKeys(), databaseKeyValueStore.isSourceSorted(),
          keyValueStoreBuilderFactory.newKeyValueStoreBuilder(databaseKeyValueStore));
    } else {
      logger.info("Generating from query");
      fromSelectStatement(connection, databaseKeyValueStore.getSource(), databaseKeyValueStore.getKeys(), databaseKeyValueStore.isSourceSorted(),
          keyValueStoreBuilderFactory.newKeyValueStoreBuilder(databaseKeyValueStore));
    }
  }

  public SqlToKeyValueConverterGeneric(Connection connection, DatabaseKeyValueStore databaseKeyValueStore,
      KeyValueStoreBuilderFactory keyValueStoreBuilderFactory) {
    super(connection, databaseKeyValueStore, keyValueStoreBuilderFactory);
  }

  private boolean fromSelectStatement(Connection connection, String selectStatement, Set<String> keySet, boolean sorted,
      KeyValueStoreBuilder keyValueStoreBuilder) throws SQLException, Descriptors.DescriptorValidationException, IOException, InterruptedException {
    Statement statement = connection.createStatement();
    String viewName = "temp_" + StringUtils.join(keySet.toArray(), "_") + "_" + ++VIEW_NUMBER;
    statement.execute("CREATE TEMP VIEW " + viewName + " AS " + selectStatement + ";");
    boolean result = fromTable(connection, viewName, keySet, sorted, keyValueStoreBuilder);
    statement.close();
    return result;
  }

  private boolean fromTable(Connection connection, String tableName, Set<String> keySet, boolean sorted, KeyValueStoreBuilder keyValueStoreBuilder)
      throws SQLException, IOException, Descriptors.DescriptorValidationException, InterruptedException {

    if (!keyValueStoreBuilder.setup()) {
      logger.info("Skipping KeyValueStore creation...");
      return false;
    }

    List<String> keys = new ArrayList<>();
    List<String> values = new ArrayList<>();
    Statement statement = connection.createStatement();

    logger.info(
        "SQL statement: " + "SELECT column_name, ordinal_position, udt_name, is_nullable, column_default " + "FROM information_schema.columns "
            + "WHERE table_name='" + tableName.toLowerCase() + "' " + "AND column_name IN (" + PostgresUtil.getPostgresEscapedConcatenatedQuery(keySet)
            + ");");

    {
      ResultSet resultSetKeys = statement.executeQuery(
          "SELECT column_name, ordinal_position, udt_name, is_nullable, column_default " + "FROM information_schema.columns " + "WHERE table_name='"
              + tableName.toLowerCase() + "' " + "AND column_name IN (" + PostgresUtil.getPostgresEscapedConcatenatedQuery(keySet)
              + ") ORDER BY ordinal_position;");
      while (resultSetKeys.next()) {
        String colName = resultSetKeys.getString(1);
        keys.add(colName);
      }
      resultSetKeys.close();
    }

    {
      ResultSet resultSetValues = statement.executeQuery(
          "SELECT column_name, ordinal_position, udt_name, is_nullable, column_default " + "FROM information_schema.columns " + "WHERE table_name='"
              + tableName.toLowerCase() + "' " + "AND column_name NOT IN (" + PostgresUtil.getPostgresEscapedConcatenatedQuery(keySet)
              + ") ORDER BY ordinal_position;");
      while (resultSetValues.next()) {
        String colName = resultSetValues.getString(1);
        values.add(colName);
      }
      resultSetValues.close();
    }

    long rowCountEstimated = 0;
    {
      ResultSet rowCountResultSet = statement
          .executeQuery("SELECT reltuples::bigint AS estimate FROM pg_class where relname='" + tableName.toLowerCase() + "';");
      if (rowCountResultSet.next()) rowCountEstimated = rowCountResultSet.getLong(1);
      rowCountResultSet.close();
    }

    List<String> colList = new ArrayList<>();
    StringBuilder sql = new StringBuilder();
    StringBuilder keysStringBuilder = new StringBuilder();
    for (String key : keys) {
      if (keysStringBuilder.length() != 0) keysStringBuilder.append(", ");
      keysStringBuilder.append(key);
      colList.add(key);
    }
    String keyString = keysStringBuilder.toString();

    sql.append("SELECT ").append(keyString);
    for (String value : values) {
      sql.append(", ").append(value);
      colList.add(value);
    }
    sql.append(" FROM ").append(tableName.toLowerCase());
    if (!sorted) sql.append(" ORDER BY ").append(keyString);
    sql.append(";");

    connection.setAutoCommit(false);
    statement.setFetchSize(FETCH_SIZE);
    logger.info("Requesting database: " + sql.toString());
    ResultSet rs = statement.executeQuery(sql.toString());

    logger.info("Reading Database (estimated rows: " + rowCountEstimated + ")");
    Codec codec = Codec.CodecFactory.getCodec(databaseKeyValueStore);
    Object key = null;
    Object value = getValue(databaseKeyValueStore.getValueClass());
    int count = 0;
    logger.info("Starting conversion");
    while (rs.next()) {
      Object nkey = getKey(keys, rs, 0);
      if (nkey == null) {
        continue;
      }
      if (key == null) {
        key = nkey;
      } else if (!nkey.equals(key)) {
        addPair(key, value, keyValueStoreBuilder, codec);
        key = nkey;
        value = clearValue(value, databaseKeyValueStore.getValueClass());
      }
      value = addValue(value, rs, databaseKeyValueStore.getValueClass(), keys.size(), values);
      if (++count % 1_000_000 == 0) logger.info("Read " + count / 1_000_000 + " mio rows");
    }
    addPair(key, value, keyValueStoreBuilder, codec);

    statement.close();
    logger.info("Finished reading " + count + " rows.");
    keyValueStoreBuilder.build();
    return true;
  }

  private Object addValue(Object value, ResultSet rs, Class clazz, int col, Collection<String> values) throws SQLException {
    if (clazz.equals(TIntDoubleHashMap.class)) {
      TIntDoubleHashMap result = (TIntDoubleHashMap) value;
      boolean firstInt = false;
      if (rs.getObject(col + 1) instanceof Integer) {
        firstInt = true;
      }
      if (firstInt) {
        result.put(rs.getInt(col + 1), rs.getDouble(col + 2));
      } else {
        result.put(rs.getInt(col + 2), rs.getDouble(col + 1));
      }
      return result;
    } else if (clazz.equals(int[].class)) {
      List<Integer> result = (List<Integer>) value;
      if (databaseKeyValueStore.isValueIntPairs()) {
        result.add(rs.getInt(col + 1));
        result.add(rs.getInt(col + 2));
      } else {
        Integer[] vs = (Integer[]) rs.getArray(col + 1).getArray();
        for(int v: vs) {
          result.add(v);
        }
      }
      return result;
    } else if (clazz.equals(KeyValueStoreRow[].class)) {
      List<KeyValueStoreRow> result = (List<KeyValueStoreRow>) value;
      Object[] v = new Object[values.size()];
      for (int i = 0; i < values.size(); i++) {
        v[i] = rs.getObject(col + i + 1);
      }
      result.add(new KeyValueStoreRow(v));
      return result;
    } else if (clazz.equals(double[].class)) {
      List<Double> result = (List<Double>) value;
      result.add(rs.getDouble(col + 1));
      return result;
    } else if (clazz.equals(String[].class)) {
      List<String> result = (List<String>) value;
      result.add(rs.getString(col + 1));
      return result;
    } else if (clazz.equals(KeyValueStoreRow.class)) {
      Object[] v = new Object[values.size()];
      for (int i = 0; i < values.size(); i++) {
        v[i] = rs.getObject(col + i + 1);
      }
      return new KeyValueStoreRow(v);
    } else {
      return rs.getObject(col + 1);
    }
  }

  private Object getValuesV(Object value, Class clazz, int partition, int totalPatitions) {
    int start = 0;
    Integer end = null;
    if(value instanceof List && totalPatitions >= 1) {
      int size = ((List) value).size();
      start =  (size/totalPatitions) * (partition - 1);
      end = partition != totalPatitions ? (size/totalPatitions) * (partition): size;
    } else if(value instanceof  List) {
      end = ((List) value).size();
    }

    if (clazz.equals(int[].class)) {
      List<Integer> result = ((List<Integer>) value).subList(start, end);
      return Ints.toArray(result);
    } else if (clazz.equals(double[].class)) {
      List<Double> result = ((List<Double>) value).subList(start, end);
      return Doubles.toArray(result);
    } else if (clazz.equals(KeyValueStoreRow[].class)) {
      List<KeyValueStoreRow> result = ((List<KeyValueStoreRow>) value).subList(start, end);
      return result.toArray(new KeyValueStoreRow[result.size()]);
    } else if (clazz.equals(String[].class)) {
      List<String> result = ((List<String>) value).subList(start, end);
      return result.toArray(new String[result.size()]);
    } else {
      return value;
    }
  }

  private Object clearValue(Object value, Class clazz) {
    if (clazz.equals(TIntDoubleHashMap.class)) {
      TIntDoubleHashMap result = (TIntDoubleHashMap) value;
      result.clear();
      return result;
    } else if (clazz.equals(int[].class)) {
      List<Integer> result = (List<Integer>) value;
      result.clear();
      return result;
    } else if (clazz.equals(KeyValueStoreRow[].class)) {
      List<KeyValueStoreRow> result = (List<KeyValueStoreRow>) value;
      result.clear();
      return result;
    } else if (clazz.equals(double[].class)) {
      List<Double> result = (List<Double>) value;
      result.clear();
      return result;
    } else if (clazz.equals(String[].class)) {
      List<String> result = (List<String>) value;
      result.clear();
      return result;
    } else {
      return null;
    }
  }

  private Object getValue(Class clazz) {
    if (clazz.equals(TIntDoubleHashMap.class)) {
      return new TIntDoubleHashMap(20000);
    } else if (clazz.equals(int[].class)) {
      return new ArrayList<Integer>();
    } else if (clazz.equals(KeyValueStoreRow[].class)) {
      return new ArrayList<KeyValueStoreRow>();
    } else if (clazz.equals(double[].class)) {
      return new ArrayList<Double>();
    } else if (clazz.equals(String[].class)) {
      return new ArrayList<String>();
    } else {
      return null;
    }
  }

  private Object getKey(Collection<String> keys, ResultSet rs, int col) throws SQLException {
    if (keys.size() > 1) {
      Object[] kobject = new Object[keys.size()];
      for (int i = 0; i < keys.size(); i++) {
        kobject[i] = rs.getObject(col + i + 1);
      }
      return new KeyValueStoreRow(kobject);
    } else {
      return rs.getObject(col + 1);
    }
  }


  private void addPair(Object key, Object value, KeyValueStoreBuilder keyValueStoreBuilder, Codec codec) throws IOException {
    if(databaseKeyValueStore.getPartitions() <= 0) {
      throw  new IllegalArgumentException("The number of partitions of the key value store should be strictly positive");
    } else if (databaseKeyValueStore.getPartitions() > 1 && !databaseKeyValueStore.getKeyClass().equals(String.class)) {
      throw  new IllegalArgumentException("Multiple partitions only supports String as the key class");
    }
    if(databaseKeyValueStore.getPartitions() == 1) {
      keyValueStoreBuilder.addPair(codec.encodeKey(key), codec.encodeValue(getValuesV(value, databaseKeyValueStore.getValueClass(), 1, 1)));
    } else {
      for(int i = 1; i <= databaseKeyValueStore.getPartitions(); i++) {
        keyValueStoreBuilder.addPair(codec.encodeKey(key+"-"+Integer.toString(i)), codec.encodeValue(getValuesV(value, databaseKeyValueStore.getValueClass(), i, databaseKeyValueStore.getPartitions())));
      }
    }
  }


}
