package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
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

/**
 * Stores the type hierarchy of the source knowledge base in the AIDA
 * repository. Creates relations:
 * - DataAccessSQL.TYPE_TAXONOMY
 * - DataAccessSQL.TYPE_IDS
 */
public class TypeTaxonomyBuilder {

  private static final Logger logger = LoggerFactory.getLogger(TypeTaxonomyBuilder.class);

  private List<TypeTaxonomyEntriesDataProvider> providers;

  private TObjectIntHashMap<Type> typeIds = new TObjectIntHashMap<>();

  public TypeTaxonomyBuilder(List<TypeTaxonomyEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException {
    // Get all child-parents relations
    TIntObjectHashMap<TIntHashSet> subclassOf = new TIntObjectHashMap<>();

    int entriesCount = 0;
    for (TypeTaxonomyEntriesDataProvider provider : providers) {
      String knowledgebase = provider.getKnowledgebaseName();
      for (Entry<String, String> subclassEntry : provider) {
        int childId = getTypeId(knowledgebase, subclassEntry.getKey());
        int parentId = getTypeId(knowledgebase, subclassEntry.getValue());

        TIntHashSet parents = subclassOf.get(childId);
        if (parents == null) {
          parents = new TIntHashSet();
          subclassOf.put(childId, parents);
        }
        parents.add(parentId);

        if (++entriesCount % 1000000 == 0) {
          logger.info("Read " + entriesCount + " subclassOf entries.");
        }
      }
    }
    logger.info("Reading Type-Type pairs DONE.");

    storeKeyValuesHashMap(subclassOf, DataAccessSQL.TYPE_TAXONOMY, "type", "parents");
    storeTypesIds();

    logger.info("DONE.");
  }

  private void storeKeyValuesHashMap(TIntObjectHashMap<TIntHashSet> map, String tableName, String keyColName, String valuesColName)
      throws SQLException {
    final Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();

    // Create table.
    String sql = "CREATE TABLE " + tableName + "(" + keyColName + " INTEGER, " + valuesColName + " INTEGER[])";
    stmt.execute(sql);

    // Sort and write.
    logger.info("Materializing " + tableName + ".");
    final PreparedStatement prepStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + tableName + "(" + keyColName + ", " + valuesColName + ") " + "VALUES(?,?)",
            1_000_000);
    con.setAutoCommit(false);

    map.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

      @Override public boolean execute(int key, TIntHashSet values) {
        int[] sortedValues = values.toArray();
        Arrays.sort(sortedValues);
        try {
          Array valueskArray = con.createArrayOf("integer", ArrayUtils.toObject(sortedValues));
          prepStmt.setInt(1, key);
          prepStmt.setArray(2, valueskArray);
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
    logger.info("Materializing " + tableName + " DONE.");

    logger.info("Creating Index.");
    sql = "CREATE INDEX " + tableName + "_index " + "ON " + tableName + " using btree (" + keyColName + ")";
    stmt.execute(sql);
    stmt.close();
    logger.info("Creating Index DONE.");

    EntityLinkingManager.releaseConnection(con);
  }

  public void storeTypesIds() throws SQLException {
    final Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    stmt = con.createStatement();
    logger.info("Started storing types-ids in the DB");
    String sql = "CREATE TABLE " + DataAccessSQL.TYPE_IDS + "(type TEXT, knowledgeBase TEXT, id INTEGER)";
    stmt.execute(sql);
    stmt.close();
    PreparedStatement pStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.TYPE_IDS + "(type, knowledgeBase, id)  VALUES(?, ?, ?)", 1_000_000);
    con.setAutoCommit(false);
    int inserts = 0;
    for (Type type : typeIds.keySet()) {
      pStmt.setString(1, type.getName());
      pStmt.setString(2, type.getKnowledgeBase());
      pStmt.setInt(3, typeIds.get(type));
      pStmt.addBatch();

      if (++inserts % 100000 == 0) {
        logger.info(inserts + " type-ids added");
      }
    }
    pStmt.executeBatch();
    con.commit();
    pStmt.close();
    con.setAutoCommit(true);

    logger.info("Done writing types-ids done");

    stmt = con.createStatement();
    logger.info("Creating Indexes");
    sql = "CREATE INDEX type_id_tindex ON " + DataAccessSQL.TYPE_IDS + " USING btree (type, knowledgeBase);";
    stmt.execute(sql);
    sql = "CREATE INDEX type_id_iindex ON " + DataAccessSQL.TYPE_IDS + " using btree (id);";
    stmt.execute(sql);
    stmt.close();

    EntityLinkingManager.releaseConnection(con);

  }

  private int getTypeId(String knowledgeBase, String typeName) {
    Type type = new Type(knowledgeBase, typeName);
    int typeId = typeIds.get(type);
    if (typeIds.getNoEntryValue() == typeId) {
      typeId = typeIds.size() + 1; // start ids from 1.
      typeIds.put(type, typeId);
    }
    return typeId;
  }
}
