package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance;

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
import java.util.List;
import java.util.Map.Entry;

public class EntityImportanceBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntityImportanceBuilder.class);

  private List<EntityImportanceEntriesDataProvider> providers;

  public EntityImportanceBuilder(List<EntityImportanceEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException, EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = DataAccess.getAllEntityIds();
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    logger.info("Storing in DB.");

    Statement stmt = con.createStatement();
    String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_RANK + "(entity INTEGER, rank DOUBLE PRECISION)";
    stmt.execute(sql);

    sql = "INSERT INTO " + DataAccessSQL.ENTITY_RANK + "(entity, rank) " + "VALUES(?, ?)";
    PreparedStatement insertStmt = DBUtil.getAutoExecutingPeparedStatement(con, sql, 1_000_000);
    con.setAutoCommit(false);

    for (EntityImportanceEntriesDataProvider provider : providers) {
      String knowledgebase = provider.getKnowledgebaseName();

      for (Entry<String, Double> entityImportance : provider) {
        String entity = entityImportance.getKey();
        // Do not use 0 index, it has a special meaning in SQL.
        int entityId = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase));
        // The dictionary building phase discards some entities. Ignore them here.
        if (entityId <= 0) {
          continue;
        }

        insertStmt.setInt(1, entityId);
        insertStmt.setDouble(2, entityImportance.getValue());
        insertStmt.addBatch();
      }
    }
    insertStmt.executeBatch();
    con.commit();
    insertStmt.close();
    con.setAutoCommit(true);

    logger.info("Creating Index.");
    sql = "CREATE INDEX " + DataAccessSQL.ENTITY_RANK + "_idx " + "ON " + DataAccessSQL.ENTITY_RANK + " using btree (entity)";
    stmt.execute(sql);
    stmt.close();
    logger.info("Creating Index DONE.");
    logger.info("Done!");

    EntityLinkingManager.releaseConnection(con);
  }
}
