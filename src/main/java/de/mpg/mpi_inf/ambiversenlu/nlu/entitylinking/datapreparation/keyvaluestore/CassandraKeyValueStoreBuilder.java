package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CassandraKeyValueStoreBuilder implements KeyValueStoreBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CassandraKeyValueStoreBuilder.class);

  private Session session;

  private String table;

  private PreparedStatement pstatement;

  private PreparedStatement updatestatement;

  List<ResultSetFuture> futures;

  public static final int BATCH_SIZE = 30_000;

  public CassandraKeyValueStoreBuilder(String table) throws IOException {
    session = EntityLinkingManager.getCassandraConnectionHolder(false).getSession();
    //Name tables in Cassandra must not contain dashes
    this.table = table.replaceAll("-", "_");
    logger.info("Creating '" + this.table);
    futures = new ArrayList<>(BATCH_SIZE);
  }

  @Override public boolean setup() throws IOException {

    //Creates a table with a ByteBuffer key and value
    String query =
        "CREATE TABLE IF NOT EXISTS " + CassandraConfig.get(CassandraConfig.KEYSPACE) + "." + table + " ( " + CassandraConfig.GENERIC_PRIMARY_KEY
            + " blob PRIMARY KEY, " + CassandraConfig.GENERIC_VALUE + " blob " + ") "
            + "WITH  caching = { 'keys' : 'ALL', 'rows_per_partition' : 'ALL' };";
    session.execute(query);

    //Prepare the statement for insertion, bind marker
    RegularStatement rstatement = QueryBuilder.insertInto(table).value(CassandraConfig.GENERIC_PRIMARY_KEY, QueryBuilder.bindMarker()).
        value(CassandraConfig.GENERIC_VALUE, QueryBuilder.bindMarker());
    pstatement = session.prepare(rstatement);

    RegularStatement rupdatestatement = QueryBuilder.update(table).with(QueryBuilder.set(CassandraConfig.GENERIC_VALUE, QueryBuilder.bindMarker())).
        where(QueryBuilder.eq(CassandraConfig.GENERIC_PRIMARY_KEY, QueryBuilder.bindMarker()));
    updatestatement = session.prepare(rupdatestatement);

    return true;
  }

  @Override public boolean addPair(byte[] key, byte[] value) throws IOException {
    return executePreparedStatement(pstatement, key, value);
  }

  @Override public boolean update(byte[] key, byte[] value) throws IOException {
    return executePreparedStatement(updatestatement, value, key);
  }

  public boolean executePreparedStatement(PreparedStatement ps, byte[] first, byte[] second) {
    if (futures.size() >= BATCH_SIZE) {
      flush();
    }
    BoundStatement boundStatementInsert = new BoundStatement(ps);
    futures.add(session.executeAsync(boundStatementInsert.bind(ByteBuffer.wrap(first), ByteBuffer.wrap(second))));
    return true;
  }

  private void flush() {
    for (ResultSetFuture rsf : futures) {
      rsf.getUninterruptibly();
    }
    futures.clear();
  }

  @Override public boolean build() {
    flush();
    return false;
  }

}