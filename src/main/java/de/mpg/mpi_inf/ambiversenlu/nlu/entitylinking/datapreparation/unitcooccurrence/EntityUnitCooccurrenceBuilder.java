package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * This class is there to create the co-occurrence tables for units e.g.:
 * entity_keywords or entity_bigrams.
 */
public class EntityUnitCooccurrenceBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntityUnitCooccurrenceBuilder.class);

  private List<EntityUnitCooccurrenceEntriesDataProvider> providers;

  public EntityUnitCooccurrenceBuilder(List<EntityUnitCooccurrenceEntriesDataProvider> providers) {
    this.providers = providers;
  }

  /**
   * Builds the co-occurrence table for the unit types given by the providers providers
   */
  public void run() throws SQLException {
    Connection con = null;

    for (EntityUnitCooccurrenceEntriesDataProvider provider : providers) {
      String tableName = provider.getUnitType().getEntityUnitCooccurrenceTableName();
      String unitName = provider.getUnitType().getUnitName();
      try {
        logger.info("Creating DB Tables ...");
        con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
        con.setAutoCommit(false);

        Statement schemaStatement = con.createStatement();

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(entity INTEGER, " + unitName + " INTEGER, " + "count INTEGER)";

        schemaStatement.execute(sql);
        schemaStatement.close();

        PreparedStatement insertEKWCountStmt = DBUtil
            .getAutoExecutingPeparedStatement(con, "insert into " + tableName + "(entity, " + unitName + ", count) VALUES(?,?,?)", 100000);

        for (EntityUnitCooccurrenceEntry entry : provider) {
          insertEKWCountStmt.setInt(1, entry.getEntity());
          insertEKWCountStmt.setInt(2, entry.getUnit());
          insertEKWCountStmt.setInt(3, entry.getCount());
          DBUtil.addBatch(insertEKWCountStmt);
        }

        DBUtil.executeBatch(insertEKWCountStmt);
        con.commit();
        insertEKWCountStmt.close();
        con.setAutoCommit(true);

        schemaStatement = con.createStatement();
        logger.info("Creating indexes ...");
        sql = "CREATE INDEX entity_" + unitName + "_index ON " + tableName + "  using btree (entity, " + unitName + " , count);";
        schemaStatement.execute(sql);
        schemaStatement.close();

        logger.info("Done :-)");

      } catch (SQLException e) {
        throw e;
      } finally {
        EntityLinkingManager.releaseConnection(con);
      }
    }
  }
}
