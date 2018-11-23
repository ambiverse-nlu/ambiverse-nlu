package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class InlinksBuilder {

  private static final Logger logger = LoggerFactory.getLogger(InlinksBuilder.class);

  private List<InlinksEntriesDataProvider> providers;

  public InlinksBuilder(List<InlinksEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException, EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = DataAccess.getAllEntityIds();

    // Use a hashset to store the links to deduplicate them. YAGO2
    // does not deduplicate links.
    TIntObjectHashMap<TIntHashSet> inlinks = new TIntObjectHashMap<TIntHashSet>();

    int linkcount = 0;
    for (InlinksEntriesDataProvider provider : providers) {
      String knowledgebase = provider.getKnowledgebaseName();

      for (Entry<String, String> inlinkEntry : provider) {
        String entityName = inlinkEntry.getValue();
        String inlinkName = inlinkEntry.getKey();
        int entity = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entityName, knowledgebase));
        int inlink = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(inlinkName, knowledgebase));

        // The dictionary building phase discards some entities. Ignore them here.
        if (entity <= 0 || inlink <= 0) {
          continue;
        }

        TIntHashSet links = inlinks.get(entity);
        if (links == null) {
          links = new TIntHashSet();
          inlinks.put(entity, links);
        }
        links.add(inlink);

        if (++linkcount % 1000000 == 0) {
          logger.info("Read " + linkcount + " links.");
        }
      }
    }
    logger.info("Reading Links DONE.");

    final Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();

    // Create table.
    String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_INLINKS + "(entity INTEGER, inlinks INTEGER[])";
    stmt.execute(sql);

    // Sort and write.
    logger.info("Materializing Links.");
    final PreparedStatement prepStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.ENTITY_INLINKS + "(entity, inlinks) " + "VALUES(?,?)", 1_000_000);
    con.setAutoCommit(false);

    inlinks.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

      @Override public boolean execute(int entity, TIntHashSet inlinks) {
        int[] sortedInlinks = inlinks.toArray();
        Arrays.sort(sortedInlinks);
        try {
          Array inlinkArray = con.createArrayOf("integer", ArrayUtils.toObject(sortedInlinks));
          prepStmt.setInt(1, entity);
          prepStmt.setArray(2, inlinkArray);
          prepStmt.addBatch();
        } catch (SQLException e) {
          e.printStackTrace();
          return false;
        }
        return true;
      }
    });
    prepStmt.executeBatch();
    con.commit();
    prepStmt.close();
    con.setAutoCommit(true);
    logger.info("Materializing Links DONE.");

    logger.info("Creating Index.");
    sql = "CREATE INDEX entity_inlinks_index " + "ON " + DataAccessSQL.ENTITY_INLINKS + " using btree (entity)";
    stmt.execute(sql);
    stmt.close();
    logger.info("Creating Index DONE.");

    EntityLinkingManager.releaseConnection(con);
  }

}
