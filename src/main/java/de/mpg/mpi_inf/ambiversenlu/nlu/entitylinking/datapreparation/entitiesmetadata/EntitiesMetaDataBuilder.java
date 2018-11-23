package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData;
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

public class EntitiesMetaDataBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntitiesMetaDataBuilder.class);

  private List<EntitiesMetaDataEntriesDataProvider> providers;

  public EntitiesMetaDataBuilder(List<EntitiesMetaDataEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws EntityLinkingDataAccessException {

    Connection con = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);

      Statement stmt = con.createStatement();

      // Create table.
      String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_METADATA + "(entity INTEGER NOT NULL, " + "humanReadableRerpresentation TEXT NOT NULL, "
          + "URL TEXT NOT NULL, " + "KnowledgeBase TEXT NOT NULL, " + "DepictionUrl TEXT," + "License TEXT," + "Description TEXT,"
          + "WikiDataId TEXT)";
      stmt.execute(sql);

      con.setAutoCommit(false);

      logger.info("Materializing entities meta data ...");
      PreparedStatement prepStmt = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.ENTITY_METADATA
              + "(entity, humanReadableRerpresentation, URL, KnowledgeBase, DepictionUrl, License, Description, WikiDataId) " + "VALUES(?,?,?,?,?,?,?,?)",
          1_000_000);

      TObjectIntHashMap<KBIdentifiedEntity> entityIds = DataAccess.getAllEntityIds();

      for (EntitiesMetaDataEntriesDataProvider provider : providers) {
        String knowledgebase = provider.getKnowledgebaseName();
        for (EntityMetaData entityMetaData : provider) {
          String entity = entityMetaData.getEntity();
          int entityId = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase));
          // The dictionary building phase discards some entities. Ignore them here.
          if (entityId <= 0) {
            continue;
          }

          prepStmt.setInt(1, entityId);
          prepStmt.setString(2, entityMetaData.getHumanReadableRepresentation());
          prepStmt.setString(3, entityMetaData.getURL());
          prepStmt.setString(4, knowledgebase);
          prepStmt.setString(5, entityMetaData.getDepictionURL());
          prepStmt.setString(6, entityMetaData.getLicenseURL());
          prepStmt.setString(7, entityMetaData.getDescription());
          prepStmt.setString(8, entityMetaData.getWikiData());
          PrepareData.addBatch(prepStmt);
        }
      }
      PrepareData.executeBatch(prepStmt);
      con.commit();
      prepStmt.close();
      con.setAutoCommit(true);

      logger.info("Materializing entities meta data DONE.");

      logger.info("Creating Indexes.");
      stmt = con.createStatement();
      sql = "CREATE INDEX " + DataAccessSQL.ENTITY_METADATA + "_eindex " + "ON " + DataAccessSQL.ENTITY_METADATA + " using btree (entity)";
      stmt.execute(sql);
      stmt.close();
      stmt = con.createStatement();
      sql = "CREATE INDEX " + DataAccessSQL.ENTITY_METADATA + "_windex " + "ON " + DataAccessSQL.ENTITY_METADATA + " using btree (WikiDataId)";
      stmt.execute(sql);
      stmt.close();
      stmt = con.createStatement();
      sql = "CREATE INDEX " + DataAccessSQL.ENTITY_METADATA + "_uindex " + "ON " + DataAccessSQL.ENTITY_METADATA + " using btree (url)";
      stmt.execute(sql);
      stmt.close();
      stmt = con.createStatement();
      sql = "CREATE INDEX " + DataAccessSQL.ENTITY_METADATA + "_hindex " + "ON " + DataAccessSQL.ENTITY_METADATA
          + " USING btree (humanReadableRerpresentation)";
      stmt.execute(sql);
      stmt.close();
      try {
        stmt = con.createStatement();
        sql = "CREATE EXTENSION pg_trgm";
        stmt.execute(sql);
        stmt.close();
        stmt = con.createStatement();
        sql = "CREATE INDEX " + DataAccessSQL.ENTITY_METADATA + "_trgmidx " + "ON " + DataAccessSQL.ENTITY_METADATA
            + " USING gist (humanReadableRerpresentation gist_trgm_ops)";
        stmt.execute(sql);
        stmt.close();
      } catch (SQLException e) {
        logger.error(
            "Could not create trgm index, infix queries will be slow (this does not affect the regular AIDA performance). " + "SQL was: '" + sql
                + "', " + "error was: " + e.getMessage());
      }
      logger.info("Creating Indexes DONE.");
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
}
