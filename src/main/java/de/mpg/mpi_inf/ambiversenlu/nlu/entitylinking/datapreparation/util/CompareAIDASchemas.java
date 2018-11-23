package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class CompareAIDASchemas {

  private static final Logger logger = LoggerFactory.getLogger(CompareAIDASchemas.class);

  private String schema1 = "mamir_aida";

  private String schema2 = "mamir_aida_old";

  private String hostname = "postgres2.d5.mpi-inf.mpg.de";

  private Integer port = 5432;

  private String username = "mamir";

  private String password = "D4Fl2gtn80pbYjL";

  private Integer maxCon = 50;

  private final String DB1 = "DB1";

  private final String DB2 = "DB2";

  private Connection con1;

  private Connection con2;

  private final int fetchSize = 10000;

  public void start(String sql, int colCount) throws SQLException {
    logger.info("Comparing ....");
    connectToDBs();

    Statement stmt1 = con1.createStatement();
    stmt1.setFetchSize(fetchSize);
    ResultSet rs1 = stmt1.executeQuery(sql);
    logger.info("Called ExecuteQuery for DB1");

    Statement stmt2 = con2.createStatement();
    stmt2.setFetchSize(fetchSize);
    ResultSet rs2 = stmt2.executeQuery(sql);
    logger.info("Called ExecuteQuery for DB2");

    boolean firstRecord = true;
    boolean perfectMatch = true;
    int count = 0;
    while (rs1.next()) {
      rs2.next();
      StringBuilder row1 = new StringBuilder();
      for (int i = 1; i <= colCount; i++) {
        row1.append(rs1.getString(i) + " - ");
      }
      StringBuilder row2 = new StringBuilder();
      for (int i = 1; i <= colCount; i++) {
        row2.append(rs2.getString(i) + " - ");
      }
      if (firstRecord) {
        logger.info("Comparing first record ...");
        firstRecord = false;
      }
      if (!row1.toString().equals(row2.toString())) {
        System.err.println("ERROR: rows don't match");
        System.err.println(schema1 + ":" + row1);
        System.err.println(schema2 + ":" + row2);
        perfectMatch = false;
        break;
      }
      if ((++count % 1000000) == 0) {
        logger.info("Compared " + (count / 1000000) + " million records so far ...");
      }
    }

    if (perfectMatch) logger.info("Congratulations, Perfect Match! :-)");
    disconnectDB();
  }

  private void connectToDBs() throws SQLException {
    Properties prop1 = EntityLinkingManager.getDatabaseProperties(hostname, port, username, password, maxCon, schema1);
    con1 = EntityLinkingManager.getConnectionForNameAndProperties(DB1, prop1);
    con1.setAutoCommit(false);

    Properties prop2 = EntityLinkingManager.getDatabaseProperties(hostname, port, username, password, maxCon, schema2);
    con2 = EntityLinkingManager.getConnectionForNameAndProperties(DB2, prop2);
    con2.setAutoCommit(false);
  }

  private void disconnectDB() throws SQLException {
    con1.setAutoCommit(true);
    con2.setAutoCommit(true);
    con1.close();
    con2.close();
  }

  public static void main(String[] args) throws SQLException {

    String dictionarySQL = "select dictionary.mention, entity_ids.entity from dictionary, entity_ids where dictionary.entity = entity_ids.id order by dictionary.mention, entity_ids.entity";
    String inlinksSQL = "SELECT id1.entity, id2.entity from ( select entity as e, unnest(inlinks) as in from entity_inlinks) AS inlinks, entity_ids as id1, entity_ids as id2 where inlinks.e = id1.id and inlinks.in = id2.id order by id1.entity, id2.entity";
    String keyphrasesSQL = "SELECT DISTINCT wids.word, ids.entity FROM entity_keyphrases kp, entity_ids ids, word_ids wids where kp.entity = ids.id and kp.keyphrase = wids.id order by ids.entity, wids.word";
    String entitiesCountsSQL = "SELECT entity_ids.entity, entity_counts.count FROM entity_counts, entity_ids where entity_counts.entity = entity_ids.id and entity_counts.count > 1 order by entity_ids.entity";

    System.out.println("Comparing dictionary");
    new CompareAIDASchemas().start(dictionarySQL, 2);

    System.out.println("Comparing entity_inlinks");
    new CompareAIDASchemas().start(inlinksSQL, 2);

    System.out.println("Comparing entity_keyphrases");
    new CompareAIDASchemas().start(keyphrasesSQL, 2);

    System.out.println("Comparing entity_counts");
    new CompareAIDASchemas().start(entitiesCountsSQL, 2);
  }

}
