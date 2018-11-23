package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class KeyphraseCountCollector {

  private static final Logger logger = LoggerFactory.getLogger(KeyphraseCountCollector.class);

  public void run() throws SQLException {
    logger.info("Creating keyphrase_counts");
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();

    String sql = "CREATE TABLE " + DataAccessSQL.KEYPHRASE_COUNTS + "(keyphrase INTEGER, count INTEGER)";
    stmt.execute(sql);

    sql = "INSERT INTO " + DataAccessSQL.KEYPHRASE_COUNTS + "(keyphrase, count) " + "SELECT keyphrase, count(entity) FROM "
        + DataAccessSQL.ENTITY_KEYPHRASES + " GROUP BY keyphrase";
    stmt.execute(sql);

    sql = "CREATE INDEX keyphrase_counts_keyphrase_index ON " + DataAccessSQL.KEYPHRASE_COUNTS + " using btree (keyphrase)";
    stmt.execute(sql);
    stmt.close();
    logger.info("Creating keyphrase_counts DONE");
    EntityLinkingManager.releaseConnection(con);
  }
}



