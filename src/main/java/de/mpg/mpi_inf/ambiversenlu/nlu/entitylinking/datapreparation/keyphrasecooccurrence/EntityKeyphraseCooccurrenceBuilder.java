package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class EntityKeyphraseCooccurrenceBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntityKeyphraseCooccurrenceBuilder.class);

  private List<EntityKeyphraseCooccurrenceEntriesDataProvider> providers;

  public EntityKeyphraseCooccurrenceBuilder(List<EntityKeyphraseCooccurrenceEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() {
    Connection con = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      store(con);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void store(Connection con) throws IOException, SQLException {
    PreparedStatement prepStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "UPDATE " + DataAccessSQL.ENTITY_KEYPHRASES + " SET count=? WHERE entity=? AND keyphrase=?",
            1_000_000);
    con.setAutoCommit(false);
    //for logging
    int count = 0;
    for (EntityKeyphraseCooccurrenceEntriesDataProvider provider : providers) {
      for (EntityKeyphraseCooccurrenceEntry entry : provider) {

        int entity = entry.getEntity();
        int keyphrase = entry.getKeyphrase();
        int sdf = entry.getCount();

        prepStmt.setInt(1, sdf);
        prepStmt.setInt(2, entity);
        prepStmt.setInt(3, keyphrase);

        addBatch(prepStmt);
        if ((++count) % 10000000 == 0) {
          logger.info(count + " entity-keyphrase pairs have been stored");
        }
      }
    }

    executeBatch(prepStmt);
    con.commit();
    prepStmt.close();
    con.setAutoCommit(true);
  }

  private void addBatch(PreparedStatement stmt) {
    try {
      stmt.addBatch();
    } catch (Exception e) {
      System.err.println("Error when adding batch: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

  private void executeBatch(PreparedStatement stmt) {
    try {
      stmt.executeBatch();
    } catch (Exception e) {
      System.err.println("Error when executing batch: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

}
