package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.Codec;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.UnexpectedException;
import java.util.*;

public class CassandraStore<T, U> implements KeyValueStore<T, U>, KeyValuesStorePartialReads<T, U> {

  private static final Logger logger = LoggerFactory.getLogger(CassandraStore.class);

  Long size = null;

  private Session session;

  private DatabaseKeyValueStore table;

  private String TABLE_NAME;

  //Prepared statement for a single key-value store (1 key, 1 value)
  private Statement statement1;

  private PreparedStatement pstatement2;

  private Statement statementCount;

  //Types for the application
  Class t;

  Class u;

  //Types in the database;
  Class k;

  //This should be created only once associated with the table name. This is OK if there is only one store per table for the whole application
  boolean keyIsByteArray;

  boolean valueIsByteArray;

  //To build queries programmatically for the case when columns are specified
  //QueryBuilder qb = new QueryBuilder(EntityLinkingManager.getCassandraConnectionHolder().getCluster()); //Not available in Cassandra java driver 3.0

  private Integer userId = null;

  private String primaryKey;

  private Codec codec;

  public Codec getCodec() {
    return codec;
  }

  @Override public long countRows() {
    if (size == null) {
      ResultSet result = session.execute(statementCount);
      size = result.all().get(0).getLong(0);
    }
    return size;
  }

  public static <T, U> CassandraStore createStore(Class<T> t, Class<U> u, String table, String primaryKey, Codec codec) throws IOException {
    return new CassandraStore<T, U>(t, u, table, primaryKey, codec);
  }

  private CassandraStore(Class<T> t, Class<U> u, String table, String primaryKey, Codec codec) throws IOException {
    this.t = t;
    this.u = u;
    this.TABLE_NAME = table.replaceAll("-", "_");
    logger.debug("Loading KeyValueStore: " + this.TABLE_NAME + " on keyspace " + CassandraConfig.get(CassandraConfig.KEYSPACE));
    this.primaryKey = primaryKey;
    session = EntityLinkingManager.getCassandraConnectionHolder(true).getSession();
    logger.debug("Loaded");

    Select.Where statement2 = QueryBuilder.select().from(TABLE_NAME).where(QueryBuilder.eq(primaryKey, QueryBuilder.bindMarker()));
    pstatement2 = session.prepare(statement2);

    statement1 = QueryBuilder.select().all().from(TABLE_NAME);

    statementCount = QueryBuilder.select().countAll().from(TABLE_NAME);

    if (t.equals(byte[].class)) {
      keyIsByteArray = true;
      k = ByteBuffer.class;
    } else {
      k = t;
    }
    if (u.equals(byte[].class)) {
      valueIsByteArray = true;
    }

    this.codec = codec;
  }

  //This function is necessary, byte[] is not a Cassandra DataType
  public <K> K getCassandraKey(T key, Class<K> type) {
    if (keyIsByteArray) {
      return type.cast(ByteBuffer.wrap((byte[]) key));
    } else {
      return type.cast(key);
    }
  }

  public <K> Collection<K> getCassandraKey(Collection<T> keys, Class<K> type) {
    if (keyIsByteArray) {
      Collection<K> result = new ArrayList<>();
      for (T key : keys) {
        result.add(type.cast(ByteBuffer.wrap((byte[]) key)));
      }
      return result;
    } else {
      return (List<K>) keys;
    }
  }

  @Override public void setup() throws IOException {
    logger.debug("Creating session from cassandra connection holder.");
    session = EntityLinkingManager.getCassandraConnectionHolder(true).getSession();
  }

  @Override public U get(T key) throws IOException {
    ByteBuffer test = (ByteBuffer) getCassandraKey(key, k);
    BoundStatement boundStatement = new BoundStatement(pstatement2).bind(test);
    ResultSet results = runQuery(boundStatement);
    return getResult(results);
  }

  public ResultSet runQuery(Statement statement) {
    return session.execute(statement);
  }

  //TODO make asynchronous
  @Override public Map<T, U> getAll(Collection<T> keys) throws IOException {
    logger.debug("Getting for all keys.");
    if (keys.isEmpty()) {
      return Collections.EMPTY_MAP;
    }
    List<ResultSetFuture> results = new ArrayList<>();
    for (T key : keys) {
      results.add(session.executeAsync(new BoundStatement(pstatement2).bind(getCassandraKey(key, k))));
    }
    //BoundStatement boundStatement = new BoundStatement(pstatement).bind(getCassandraKey(keys, k));
    //ResultSet results = runQuery(boundStatement);
    return getMultipleKeyResults(results);
  }

  @Override public EntryIterator entryIterator() {
    return new CasandraEntryIterator();
  }

  public class CasandraEntryIterator<T, U> implements EntryIterator {

    private ResultSet results = null;

    private void init() {
      results = session.execute(statement1);
    }

    public CasandraEntryIterator() {
      init();
    }

    @Override public boolean hasNext() throws IOException {
      return !results.isExhausted();
    }

    @Override public Entry next() throws IOException {
      Row row = results.one();
      Entry result = new Entry(getKey(row), getAllValues(row));
      return result;
    }
  }

  @Override public U get(T key, String... columns) throws IOException {
    Select.Where statement = QueryBuilder.select(columns).from(TABLE_NAME).where(QueryBuilder.eq(primaryKey, getCassandraKey(key, k)));
    ResultSet results = runQuery(statement);
    return getResult(results);
  }

  public List<U> getAll(T key, String... columns) throws IOException {
    Select.Where statement = QueryBuilder.select(columns).from(TABLE_NAME).where(QueryBuilder.eq(primaryKey, getCassandraKey(key, k)));
    ResultSet results = runQuery(statement);
    return getResults(results);
  }

  @Override
  //For the moment Cassandra does not support OR so composite keys cannot be handled by
  //this function which aims to a single database call
  public Map<T, U> getAll(Collection<T> keys, String... columns) throws IOException {
    if (keys.isEmpty()) {
      return Collections.EMPTY_MAP;
    }
    Statement statement = QueryBuilder.select(columns).from(TABLE_NAME).where(QueryBuilder.in(primaryKey, getCassandraKey(keys, k)));
    ResultSet results = runQuery(statement);
    return getMultipleKeyResults(results);
  }

  private U getResult(ResultSet results) throws UnexpectedException {
    List<Row> all = results.all();
    if (all.size() > 1) {
      throw new UnexpectedException("Multiple rows per key");
    } else if (all.size() == 0) {
      return null;
    } else {
      return getAllValues(all.get(0));
    }
  }

  private List<U> getResults(ResultSet results) throws UnexpectedException {
    List<Row> all = results.all();
    if (all.size() == 0) {
      return null;
    }
    List<U> result = new ArrayList<>();
    for (Row row : all) {
      result.add(getAllValues(row));
    }
    return result;
  }

  private Map<T, U> getMultipleKeyResults(ResultSet results) throws UnexpectedException {
    Map<T, U> result = new HashMap<>();
    Row row;
    while (!results.isExhausted()) {
      row = results.one();
      result.put(getKey(row), getAllValues(row));
    }
    return result;
  }

  private Map<T, U> getMultipleKeyResults(List<ResultSetFuture> results) throws UnexpectedException {
    Map<T, U> result = new HashMap<>();
    Row row;
    for (ResultSetFuture rsf : results) {
      ResultSet rs = rsf.getUninterruptibly();
      if (rs.isExhausted()) continue;
      row = rs.one();
      result.put(getKey(row), getAllValues(row));
    }
    return result;
  }

  private <K> K getValue(Row row, int pos) {
    if (valueIsByteArray) {
      return (K) row.getBytes(pos).array();
    } else return (K) row.getObject(pos);
  }

  private T getKey(Row row) {
    return getValue(row, 0);
  }

  private U getAllValues(Row row) {
    if (valueIsByteArray) {
      return getValue(row, 1);
    } else {
      Object[] values = new Object[row.getColumnDefinitions().size()];
      for (int i = 1; i <= row.getColumnDefinitions().size(); i++) {
        values[i - 1] = getValue(row, i);
      }
      return (U) values;
    }
  }
}
