package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.PostgresUtil;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Imports the precomputed keyword weights from
 * entity_keywords to entity_keyphrases.
 * This allows AIDA to get all necessary data in one go. 
 *
 *
 */
public class EntityKeyphraseTokenWeightImporter {

  private static final Logger logger = LoggerFactory.getLogger(EntityKeyphraseTokenWeightImporter.class);

  public void run() throws SQLException, EntityLinkingDataAccessException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    Statement readStmt = con.createStatement();
    readStmt.setFetchSize(1_000_000);

    String sql = "UPDATE " + DataAccessSQL.ENTITY_KEYPHRASES + " SET keyphrase_token_weights=? WHERE " + "entity=? AND keyphrase=?";
    PreparedStatement setStmt = DBUtil.getAutoExecutingPeparedStatement(con, sql, 1_000_000);
    Entities entities = DataAccess.getAllEntities();
    int entityCount = 0;

    for (Entity entity : entities) {
      // Get all keyphrases and keywords+weights for a single entity.
      Entities singleEntity = new Entities();
      singleEntity.add(entity);
      TIntObjectHashMap<int[]> entityKeyphrases = new TIntObjectHashMap<int[]>();
      TIntObjectHashMap<int[]> keyphraseTokens = new TIntObjectHashMap<int[]>();
      DataAccess.getEntityKeyphraseTokens(singleEntity, entityKeyphrases, keyphraseTokens);

      TIntDoubleHashMap keywordWeights = new TIntDoubleHashMap();

      TIntHashSet allKeywords = new TIntHashSet();
      for (int[] keywords : keyphraseTokens.valueCollection()) {
        allKeywords.addAll(keywords);
      }
      if (allKeywords.isEmpty()) {
        // Skip entities without keyphrases.
        continue;
      }
      String keywordQuey = PostgresUtil.getIdQuery(allKeywords);

      sql =
          "SELECT keyword, weight FROM " + DataAccessSQL.ENTITY_KEYWORDS + " WHERE entity=" + entity.getId() + " AND" + " keyword IN (" + keywordQuey
              + ")";
      ResultSet rs = readStmt.executeQuery(sql);
      while (rs.next()) {
        int keyword = rs.getInt("keyword");
        double weight = rs.getDouble("weight");
        keywordWeights.put(keyword, weight);
      }
      rs.close();

      // Add the weights as array.
      for (TIntObjectIterator<int[]> itr = keyphraseTokens.iterator(); itr.hasNext(); ) {
        itr.advance();
        int keyphrase = itr.key();
        int[] tokens = itr.value();
        Double[] weights = new Double[tokens.length];
        for (int i = 0; i < tokens.length; ++i) {
          weights[i] = keywordWeights.get(tokens[i]);
        }
        Array weightArray = con.createArrayOf("float8", weights);
        setStmt.setArray(1, weightArray);
        setStmt.setInt(2, entity.getId());
        setStmt.setInt(3, keyphrase);
        setStmt.addBatch();
      }

      if (++entityCount % 10000 == 0) {
        logger.info("Added keyword token weights for " + entityCount + " entities");
      }
    }

    setStmt.executeBatch();
    con.commit();
    setStmt.close();
    readStmt.close();

    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);
  }

}
