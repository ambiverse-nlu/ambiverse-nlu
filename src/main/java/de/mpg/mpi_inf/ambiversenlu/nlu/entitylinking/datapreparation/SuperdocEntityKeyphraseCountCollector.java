package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * Expects as input two tables:
 *  - facts(arg1,relation,arg2): all hasInternalWikipediaLinkTo facts are used
 *  - entity_keyphrases(entity,keyphrase)
 *
 * For each entity, the superdoc is created by checking the hasInternalWikipediaLinkTo.
 * For all entities in the superdoc, the keyphrases are read to get the (occurrence) count of a 
 * keyphrase in the superdoc (i.e. in how many of the docs of the superdoc the keyphrase exists)
 *
 * The result is a file with the format (tab-separated):
 *  entity<tab>keyphrase<tab>occurrence_count
 *
 */
public class SuperdocEntityKeyphraseCountCollector {

  private static final Logger logger = LoggerFactory.getLogger(SuperdocEntityKeyphraseCountCollector.class);

  private int fetchSize = 1000000;

  private BufferedWriter writer;

  public SuperdocEntityKeyphraseCountCollector(File outFile) throws FileNotFoundException {
    writer = FileUtils.getBufferedUTF8Writer(outFile);
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    SuperdocEntityKeyphraseCountCollector kpStatCollector = new SuperdocEntityKeyphraseCountCollector(new File(args[0]));
    kpStatCollector.run();
  }

  public void run() throws Exception {
    logger.info("STARTING");
    logger.info("Reading Link Graph");
    Entities entities = DataAccess.getAllEntities();
    TIntObjectHashMap<int[]> linkFrom = DataAccess.getAllInlinks();
    TIntObjectHashMap<int[]> linkTo = transformInlinksToOutlinks(linkFrom);
    logger.info("Creating Count Dictionaries");
    TIntObjectHashMap<TIntIntHashMap> superdocKeyphraseCounts = createAllSuperdocKeyphraseDictionaries();
    logger.info("Counting Keyphrase Occurrences");
    fillSuperdocKeyphraseCounts(linkTo, superdocKeyphraseCounts);
    logger.info("Writing to file");
    writeToFile(superdocKeyphraseCounts);
    // Flush global writer.
    writer.flush();
    writer.close();
    logger.info("DONE");
  }

  private TIntObjectHashMap<int[]> transformInlinksToOutlinks(TIntObjectHashMap<int[]> linkFrom) {
    TIntObjectHashMap<TIntLinkedList> linkTo = new TIntObjectHashMap<TIntLinkedList>();

    // Reverse link direction.
    for (TIntObjectIterator<int[]> itr = linkFrom.iterator(); itr.hasNext(); ) {
      itr.advance();
      int target = itr.key();
      int[] sources = itr.value();
      for (int newTarget : sources) {
        TIntLinkedList newSources = linkTo.get(newTarget);
        if (newSources == null) {
          newSources = new TIntLinkedList();
          linkTo.put(newTarget, newSources);
        }
        newSources.add(target);
      }
    }

    // Transform to array.
    TIntObjectHashMap<int[]> linkToArray = new TIntObjectHashMap<int[]>(linkTo.size(), 1.0f);
    for (TIntObjectIterator<TIntLinkedList> itr = linkTo.iterator(); itr.hasNext(); ) {
      itr.advance();
      linkToArray.put(itr.key(), itr.value().toArray());
    }

    return linkToArray;
  }

  private TIntObjectHashMap<TIntIntHashMap> createAllSuperdocKeyphraseDictionaries() throws SQLException, EntityLinkingDataAccessException {
    TIntObjectHashMap<TIntIntHashMap> superdocKeyphraseDictionaries = new TIntObjectHashMap<TIntIntHashMap>(DataAccess.getCollectionSize(), 1.0f);

    // get all keyphrases       
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    Statement stmt = con.createStatement();
    stmt.setFetchSize(fetchSize);

    String sql = "SELECT entity,keyphrase FROM " + DataAccessSQL.ENTITY_KEYPHRASES;
    ResultSet rs = stmt.executeQuery(sql);

    int rowCount = 0;

    while (rs.next()) {
      rowCount++;

      // write message every 1,000,000 rows
      if ((rowCount % 1000000) == 0) {
        logger.info("Read " + rowCount / 1000000 + " mio e/kp");
      }

      // create entity->keyphrase->count 2-stage dictionaries

      // create eid if necessary
      int eid = rs.getInt("entity");
      int kid = rs.getInt("keyphrase");

      TIntIntHashMap keyphraseCount = superdocKeyphraseDictionaries.get(eid);

      if (keyphraseCount == null) {
        keyphraseCount = new TIntIntHashMap();
        superdocKeyphraseDictionaries.put(eid, keyphraseCount);
      }

      // initially, all keyphrase counts are 0
      keyphraseCount.put(kid, 0);
    }

    rs.close();
    stmt.close();
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);

    logger.info("Created " + superdocKeyphraseDictionaries.size() + " entity-keyphrase dictionaries");

    return superdocKeyphraseDictionaries;
  }

  private void fillSuperdocKeyphraseCounts(TIntObjectHashMap<int[]> linkTo, TIntObjectHashMap<TIntIntHashMap> superdocKeyphraseCounts)
      throws SQLException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    Statement stmt = con.createStatement();
    stmt.setFetchSize(fetchSize);

    String sql = "SELECT entity,keyphrase FROM " + DataAccessSQL.ENTITY_KEYPHRASES;
    ResultSet rs = stmt.executeQuery(sql);

    int reportFreq = 1000000;
    int rowCount = 0;
    long totalCount = 0;
    long totalLinksSinceLast = 0;
    long startTime = System.currentTimeMillis();

    while (rs.next()) {
      // write message every 1,000,000 rows
      if ((++rowCount % reportFreq) == 0) {
        long duration = System.currentTimeMillis() - startTime;
        double avgLinks = (double) totalLinksSinceLast / (double) reportFreq;
        double linksPerMs = (double) totalLinksSinceLast / duration;
        logger.info(
            "Read " + rowCount / 1000000 + " mio e/kp ... " + totalCount + " kp-counts adjusted." + " Average number of links was: " + avgLinks + " ("
                + linksPerMs + " links/ms).");
        startTime = System.currentTimeMillis();
        totalLinksSinceLast = 0;
      }

      // get ids
      int eid = rs.getInt("entity");
      int kid = rs.getInt("keyphrase");

      // add keyphrase to entity itself
      TIntIntHashMap keyphraseCount = superdocKeyphraseCounts.get(eid);
      boolean adjusted = keyphraseCount.adjustValue(kid, 1);
      // Just bookkeeping.
      if (adjusted) ++totalCount;

      // add keyphrase to entities this entity links to
      int[] links = linkTo.get(eid);
      if (links != null) {
        totalLinksSinceLast += links.length;
        for (int linkedEid : links) {
          keyphraseCount = superdocKeyphraseCounts.get(linkedEid);

          if (keyphraseCount != null) {
            adjusted = keyphraseCount.adjustValue(kid, 1);
            // Just bookkeeping.
            if (adjusted) ++totalCount;
          } else {
            logger.warn("No dictionary for entity '" + eid + "'");
          }
        }
      }
    }

    rs.close();
    stmt.close();
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);

    logger.info(totalCount + " kp-counts adjusted");
  }

  private void writeToFile(TIntObjectHashMap<TIntIntHashMap> superdocKeyphraseCounts) throws IOException, SQLException {
    int eCount = 0;

    logger.info("Writing entity-keyphrase pairs for " + superdocKeyphraseCounts.size() + " entities");

    for (int eid : superdocKeyphraseCounts.keys()) {

      if ((++eCount % 100000) == 0) {
        double percent = (double) eCount / (double) superdocKeyphraseCounts.size();

        logger.info("Wrote " + eCount + " entities (" + new DecimalFormat("#.##").format(percent) + ")");
      }

      TIntIntHashMap keyphraseCounts = superdocKeyphraseCounts.get(eid);

      for (int kid : keyphraseCounts.keys()) {
        int count = keyphraseCounts.get(kid);

        writer.write(eid + "\t" + kid + "\t" + String.valueOf(count));
        writer.newLine();
      }
    }
  }
}
  