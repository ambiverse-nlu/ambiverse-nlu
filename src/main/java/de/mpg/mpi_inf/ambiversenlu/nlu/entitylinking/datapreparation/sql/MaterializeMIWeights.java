package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation.MI_TYPE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Compute all the (Normalized Pointwise) Mutual Information weights for
 * the given counts.
 *
 *
 */
public class MaterializeMIWeights {

  private static final Logger logger = LoggerFactory.getLogger(MaterializeMIWeights.class);

  public void run(String keytermTable, String keytermKey, String entityKeytermTable, String entityKeytermKey, MI_TYPE miType)
      throws SQLException, EntityLinkingDataAccessException {
    logger.info("Materializing MI weights for " + entityKeytermTable);
    int collectionSize = DataAccess.getCollectionSize();
    logger.info("Reading keyterm counts from " + keytermTable);
    TIntIntHashMap keytermCounts = getCounts(keytermTable, keytermKey);
    logger.info("Reading entity counts");
    TIntIntHashMap entityCounts = getCounts(DataAccessSQL.ENTITY_COUNTS, "entity");

    logger.info("Computing weights");
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    PreparedStatement updateStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "UPDATE " + entityKeytermTable + " SET weight=? WHERE entity=? AND " + entityKeytermKey + "=?",
            1_000_000);
    Statement readStmt = con.createStatement();
    readStmt.setFetchSize(100000);
    ResultSet rs = readStmt.executeQuery("SELECT entity," + entityKeytermKey + ",count " + "FROM " + entityKeytermTable);
    int computations = 0;
    while (rs.next()) {
      int entity = rs.getInt("entity");
      int keyterm = rs.getInt(entityKeytermKey);

      int entityKeytermCount = rs.getInt("count");
      int entityCount = entityCounts.get(entity);
      int keytermCount = keytermCounts.get(keyterm);

      assert entityCount > 0 : "No entity count for ID: " + entity;
      assert entityCount <= collectionSize : "Entity " + EntityLinkingManager.getEntity(entity) + " too large: " + entityCount;
      assert keytermCount > 0 : "No keyterm count for ID: " + keyterm;
      assert keytermCount <= collectionSize : "Keyterm " + DataAccess.getWordForId(keyterm) + " too large: " + keytermCount;
      assert
          entityKeytermCount <= keytermCount :
          "EntityKeytermCount ( " + EntityLinkingManager.getEntity(entity) + " / " + DataAccess.getWordForId(keyterm) + " ): " + entityKeytermCount
              + " larger than keytermCount: " + keytermCount;
      assert
          entityKeytermCount <= entityCount :
          "EntityKeytermCount ( " + EntityLinkingManager.getEntity(entity) + " / " + DataAccess.getWordForId(keyterm) + " ): " + entityKeytermCount
              + " larger than entityCount: " + entityCount;

      double weight = 0.0;

      switch (miType) {
        case MUTUAL_INFORMATION:
          weight = WeightComputation.computeMI(entityCount, keytermCount, entityKeytermCount, collectionSize, false);
          break;
        case NORMALIZED_MUTUAL_INFORMATION:
          weight = WeightComputation.computeMI(entityCount, keytermCount, entityKeytermCount, collectionSize, true);
          break;
        case NORMALIZED_POINTWISE_MUTUAL_INFORMATION:
          weight = WeightComputation.computeNPMI(entityCount, keytermCount, entityKeytermCount, collectionSize);
          assert
              weight >= -1.001 :
              "NPMI was < 1 for " + "entity " + EntityLinkingManager.getEntity(entity) + ": " + entityCount + "\n " + "keyterm " + DataAccess
                  .getWordForId(keyterm) + ": " + keytermCount + " \n" + "intersection: " + entityKeytermCount;
          assert
              weight <= 1.001 :
              "NPMI was > 1 for " + "entity " + EntityLinkingManager.getEntity(entity) + ": " + entityCount + "\n " + "keyterm " + DataAccess
                  .getWordForId(keyterm) + ": " + keytermCount + " \n" + "intersection: " + entityKeytermCount;
        default:
          break;
      }

      // AIDA assumes keyphrase weights in [0.0, \infty),
      // Some MI forms have negative indication below 0 - just cut.
      if (weight < 0) {
        weight = 0;
      }

      updateStmt.setDouble(1, weight);
      updateStmt.setInt(2, entity);
      updateStmt.setInt(3, keyterm);
      DBUtil.addBatch(updateStmt);

      if (++computations % 1000000 == 0) {
        logger.info("Computed weights for " + computations + " pairs");
      }
    }
    DBUtil.executeBatch(updateStmt);
    con.commit();
    updateStmt.close();
    rs.close();
    readStmt.close();
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);
  }

  private TIntIntHashMap getCounts(String table, String key) throws SQLException {
    TIntIntHashMap counts = new TIntIntHashMap();
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    con.setAutoCommit(false);
    stmt.setFetchSize(1000000);
    ResultSet rs = stmt.executeQuery("SELECT " + key + ",count FROM " + table);
    while (rs.next()) {
      int keyThing = rs.getInt(key);
      int count = rs.getInt("count");
      counts.put(keyThing, count);
    }
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);
    return counts;
  }
}
