package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class KeywordsStatCollector {

  private static final Logger logger = LoggerFactory.getLogger(KeywordsStatCollector.class);

  /**
   * @param args
   * @throws SQLException
   */
  public static void main(String[] args) throws SQLException, EntityLinkingDataAccessException {
    KeywordsStatCollector keywordsStatCollector = new KeywordsStatCollector();
    keywordsStatCollector.startCollectingStatsInMemory();
  }

  public void startCollectingStatsInMemory() throws SQLException, EntityLinkingDataAccessException {
    Connection con = null;
    Statement statement = null;

    TIntObjectHashMap<TIntHashSet> entityKeywords = new TIntObjectHashMap<TIntHashSet>();

    ResultSet r;

    con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    statement = con.createStatement();
    statement.setFetchSize(1000000);

    logger.info("Reading all keyphrase tokens ...");
    TIntObjectHashMap<int[]> keyphraseTokens = DataAccess.getAllKeyphraseTokens();

    String sql = "select entity, keyphrase from " + DataAccessSQL.ENTITY_KEYPHRASES;
    r = statement.executeQuery(sql);
    logger.info("Reading Data ...");
    int rowCount = 0;// just for logging
    while (r.next()) {
      int entity = r.getInt("entity");
      int keyphrase = r.getInt("keyphrase");
      int[] keyphrase_tokens = keyphraseTokens.get(keyphrase);

      TIntHashSet keywords = entityKeywords.get(entity);
      if (keywords == null) {
        keywords = new TIntHashSet();
        entityKeywords.put(entity, keywords);
      }

      if (keyphrase_tokens == null) {
        logger.info("ISSUE : Keyphrase : " + keyphrase + " has no tokens.");
        continue;
      }

      for (int token : keyphrase_tokens) {
        keywords.add(token);
      }

      if (++rowCount % 10000000 == 0) {
        logger.info(rowCount + " rows read");
      }
    }
    r.close();
    statement.close();
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);

    // Count the occurrences of keywords with respect to entities
    TIntIntHashMap keywordCounts = new TIntIntHashMap();
    for (int entity : entityKeywords.keys()) {
      TIntHashSet keywords = entityKeywords.get(entity);
      for (int keyword : keywords.toArray()) {
        keywordCounts.adjustOrPutValue(keyword, 1, 1);
      }
    }

    logger.info("Storing data ...");
    storeKeywordsIntoDB(keywordCounts);
  }

  private void storeKeywordsIntoDB(TIntIntHashMap keywordsCounts) throws SQLException {
    Connection con = null;

    con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement schemaStatement = con.createStatement();

    String sql = "CREATE TABLE " + DataAccessSQL.KEYWORD_COUNTS + "(keyword INTEGER, count INTEGER)";
    schemaStatement.execute(sql);
    schemaStatement.close();

    final PreparedStatement insertStatement;
    insertStatement = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.KEYWORD_COUNTS + " VALUES(?,?)", 1_000_000);
    con.setAutoCommit(false);

    keywordsCounts.forEachEntry(new TIntIntProcedure() {

      @Override public boolean execute(int keyword, int count) {
        try {
          insertStatement.setInt(1, keyword);
          insertStatement.setInt(2, count);
          insertStatement.addBatch();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return true;
      }
    });
    insertStatement.executeBatch();
    con.commit();
    insertStatement.close();
    con.setAutoCommit(true);

    schemaStatement = con.createStatement();
    logger.info("Creating indexes ...");
    sql = "CREATE INDEX keywords_index ON " + DataAccessSQL.KEYWORD_COUNTS + " using btree (keyword);";
    schemaStatement.execute(sql);
    schemaStatement.close();

    EntityLinkingManager.releaseConnection(con);
  }
}
