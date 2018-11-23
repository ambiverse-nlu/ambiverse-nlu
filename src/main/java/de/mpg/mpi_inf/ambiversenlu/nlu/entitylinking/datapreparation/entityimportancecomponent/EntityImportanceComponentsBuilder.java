package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EntityImportanceComponentsBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntityImportanceComponentsBuilder.class);

  private List<EntityImportanceComponentEntriesDataProvider> providers;

  private Map<String, Integer> importanceComponentsMaxValues;

  private Map<String, Integer> importanceComponentsMinValues;

  public EntityImportanceComponentsBuilder(List<EntityImportanceComponentEntriesDataProvider> providers) {
    this.providers = providers;
    importanceComponentsMaxValues = new HashMap<String, Integer>();
    importanceComponentsMinValues = new HashMap<String, Integer>();
  }

  public void run() throws EntityLinkingDataAccessException {

    TObjectIntHashMap<KBIdentifiedEntity> entityIds = DataAccess.getAllEntityIds();
    Connection con = null;
    try {
      logger.info("Storing in DB.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      Statement stmt = con.createStatement();

      for (EntityImportanceComponentEntriesDataProvider provider : providers) {
        String knowledgebase = provider.getKnowledgebaseName();
        String dbTableName = provider.getDBTableNameToStoreIn();

        String sql = "CREATE TABLE " + dbTableName + "(entity INTEGER, value INTEGER)";
        stmt.execute(sql);

        sql = "INSERT INTO " + dbTableName + "(entity, value) " + "VALUES(?, ?)";
        PreparedStatement insertStmt = DBUtil.getAutoExecutingPeparedStatement(con, sql, 1_000_000);

        for (Entry<String, Integer> entityImportance : provider) {
          String entity = entityImportance.getKey();
          // Do not use 0 index, it has a special meaning in SQL.
          int entityId = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase));
          // The dictionary building phase discards some entities. Ignore them here.
          if (entityId <= 0) {
            continue;
          }

          int value = entityImportance.getValue();
          adjustMaxMin(dbTableName, value);
          insertStmt.setInt(1, entityId);
          insertStmt.setInt(2, value);
          insertStmt.addBatch();
        }

        insertStmt.executeBatch();
        con.commit();
        insertStmt.close();
        con.setAutoCommit(true);
      }

      storeMaxMinValue(con);

      for (EntityImportanceComponentEntriesDataProvider provider : providers) {
        String dbTableName = provider.getDBTableNameToStoreIn();
        logger.info("Creating Indexes.");
        String sql = "CREATE INDEX " + dbTableName + "_idx " + "ON " + dbTableName + " USING btree (entity, value)";
        stmt.execute(sql);
      }
      stmt.close();
      logger.info("Creating Indexes DONE.");
      logger.info("Done!");

    } catch (SQLException e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

  }

  private void adjustMaxMin(String dbTableName, int newValue) {
    if (importanceComponentsMaxValues.containsKey(dbTableName)) {
      int oldMax = importanceComponentsMaxValues.get(dbTableName);
      if (newValue > oldMax) {
        importanceComponentsMaxValues.put(dbTableName, newValue);
      }
    } else {
      importanceComponentsMaxValues.put(dbTableName, newValue);
    }

    if (importanceComponentsMinValues.containsKey(dbTableName)) {
      int oldMin = importanceComponentsMinValues.get(dbTableName);
      if (newValue < oldMin) {
        importanceComponentsMinValues.put(dbTableName, newValue);
      }
    } else {
      importanceComponentsMinValues.put(dbTableName, newValue);
    }
  }

  private void storeMaxMinValue(Connection con) throws SQLException {
    Statement stmt = con.createStatement();
    String sql = "CREATE TABLE " + DataAccessSQL.IMPORTANCE_COMPONENTS_INFO + "(component TEXT, max INTEGER, min INTEGER)";
    stmt.execute(sql);
    con.setAutoCommit(false);

    sql = "INSERT INTO " + DataAccessSQL.IMPORTANCE_COMPONENTS_INFO + "(component, max, min) " + "VALUES(?, ?, ?)";
    PreparedStatement insertStmt = DBUtil.getAutoExecutingPeparedStatement(con, sql, 1_000_000);

    for (EntityImportanceComponentEntriesDataProvider provider : providers) {
      String dbTableName = provider.getDBTableNameToStoreIn();
      int max = importanceComponentsMaxValues.get(dbTableName);
      int min = importanceComponentsMinValues.get(dbTableName);

      insertStmt.setString(1, dbTableName);
      insertStmt.setInt(2, max);
      insertStmt.setInt(3, min);
      insertStmt.addBatch();
    }

    insertStmt.executeBatch();
    insertStmt.close();

    con.setAutoCommit(true);

    sql = "CREATE INDEX " + DataAccessSQL.IMPORTANCE_COMPONENTS_INFO + "_idx " + "ON " + DataAccessSQL.IMPORTANCE_COMPONENTS_INFO
        + " USING btree (component, max, min)";
    stmt.execute(sql);
    stmt.close();

  }

}
