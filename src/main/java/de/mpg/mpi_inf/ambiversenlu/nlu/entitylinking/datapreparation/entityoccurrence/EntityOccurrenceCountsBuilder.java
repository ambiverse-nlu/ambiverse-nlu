package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;

public class EntityOccurrenceCountsBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntityOccurrenceCountsBuilder.class);

  private List<EntityOccurrenceCountsEntriesDataProvider> providers;

  public EntityOccurrenceCountsBuilder(List<EntityOccurrenceCountsEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement schemaStatement = con.createStatement();

    String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_COUNTS + "(entity INTEGER, count INTEGER)";
    schemaStatement.execute(sql);
    schemaStatement.close();

    PreparedStatement insertECountStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "insert into " + DataAccessSQL.ENTITY_COUNTS + " VALUES(?,?)", 1_000_000);
    con.setAutoCommit(false);

    for (EntityOccurrenceCountsEntriesDataProvider provider : providers) {
      for (Entry<Integer, Integer> entityCount : provider) {
        insertECountStmt.setInt(1, entityCount.getKey());
        insertECountStmt.setInt(2, entityCount.getValue());
        insertECountStmt.addBatch();
      }
    }

    insertECountStmt.executeBatch();
    con.commit();
    insertECountStmt.close();
    con.setAutoCommit(true);

    logger.info("Creating indexes ...");
    schemaStatement = con.createStatement();
    sql = "CREATE INDEX entity_count_index ON " + DataAccessSQL.ENTITY_COUNTS + " using btree (entity);";
    schemaStatement.execute(sql);
    schemaStatement.close();

    logger.info("Done!");
    EntityLinkingManager.releaseConnection(con);
  }
}
