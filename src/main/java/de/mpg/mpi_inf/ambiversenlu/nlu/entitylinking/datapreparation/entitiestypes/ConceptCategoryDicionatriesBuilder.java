package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
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
 * Builds the Category-type association relations:
 * - DataAccessSQL.CONCEPT_CATEGORIES
 * - DataAccessSQL.TYPE_ENTITIES
 * - DataAccessSQL.CATEGORY_IDS
 *
 */
public class ConceptCategoryDicionatriesBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ConceptCategoryDicionatriesBuilder.class);

  private List<EntitiesTypesEntriesDataProvider> providers;
  
  private TObjectIntHashMap<Type> categoryIds = new TObjectIntHashMap<>();

  public ConceptCategoryDicionatriesBuilder(List<EntitiesTypesEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException, EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> conceptIds = DataAccess.getAllEntityIds(false);

    // Use a hashset to store the type to deduplicate them.
    TIntObjectHashMap<TIntHashSet> entityTypesMap = new TIntObjectHashMap<TIntHashSet>();
    TIntObjectHashMap<TIntHashSet> typeEntitiesMap = new TIntObjectHashMap<TIntHashSet>();

    int entriesCount = 0;
    for (EntitiesTypesEntriesDataProvider provider : providers) {
      String knowledgebase = provider.getKnowledgebaseName();
      for (Entry<String, String> entityTypeEntry : provider) {
        String entityName = entityTypeEntry.getKey();
        String typeName = entityTypeEntry.getValue();

        int concept = conceptIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entityName, knowledgebase));
        int type = getCategoryId(knowledgebase, typeName); 
        
        // The dictionary building phase discards some entities. Ignore them here.
        // If the entityName is not a concept. Ignore them here.
        if (concept <= 0) {
          continue;
        }

        assert type > 0 : "No id for category: " + typeName;

        TIntHashSet types = entityTypesMap.get(concept);
        if (types == null) {
          types = new TIntHashSet();
          entityTypesMap.put(concept, types);
        }
        types.add(type);

        TIntHashSet entities = typeEntitiesMap.get(type);
        if (entities == null) {
          entities = new TIntHashSet();
          typeEntitiesMap.put(type, entities);
        }
        entities.add(concept);

        if (++entriesCount % 1000000 == 0) {
          logger.info("Read " + entriesCount + " Category-type entries.");
        }
      }
    }
    logger.info("Reading Category-Type pairs DONE.");

    storeKeyValuesHashMap(entityTypesMap, DataAccessSQL.CONCEPT_CATEGORIES, "entity", "types");
    storeKeyValuesHashMap(typeEntitiesMap, DataAccessSQL.CATEGORY_CONCEPTS, "type", "entities");

    storeCategoryIds();
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
    sql = "CREATE INDEX " + tableName + "_index " + "ON " + tableName + "  using btree (" + keyColName + ")";
    stmt.execute(sql);
    stmt.close();
    logger.info("Creating Index DONE.");

    EntityLinkingManager.releaseConnection(con);
  }
  
  public void storeCategoryIds() throws SQLException {
    final Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    stmt = con.createStatement();
    logger.info("Started storing category-ids in the DB");
    String sql = "CREATE TABLE " + DataAccessSQL.CATEGORY_IDS + "(type TEXT, knowledgeBase TEXT, id INTEGER)";
    stmt.execute(sql);
    stmt.close();
    PreparedStatement pStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.CATEGORY_IDS + "(type, knowledgeBase, id)  VALUES(?, ?, ?)", 1_000_000);
    con.setAutoCommit(false);
    int inserts = 0;
    for (Type type : categoryIds.keySet()) {
      pStmt.setString(1, type.getName());
      pStmt.setString(2, type.getKnowledgeBase());
      pStmt.setInt(3, categoryIds.get(type));
      pStmt.addBatch();

      if (++inserts % 100000 == 0) {
        logger.info(inserts + " category-ids added");
      }
    }
    pStmt.executeBatch();
    con.commit();
    pStmt.close();
    con.setAutoCommit(true);

    logger.info("Done writing category-ids done");

    stmt = con.createStatement();
    logger.info("Creating Indexes");
    sql = "CREATE INDEX category_id_tindex ON " + DataAccessSQL.CATEGORY_IDS + " USING btree (type, knowledgeBase);";
    stmt.execute(sql);
    sql = "CREATE INDEX category_id_iindex ON " + DataAccessSQL.CATEGORY_IDS + " using btree (id);";
    stmt.execute(sql);
    stmt.close();

    EntityLinkingManager.releaseConnection(con);

  }
  
  
  private int getCategoryId(String knowledgeBase, String typeName) {
    Type type = new Type(knowledgeBase, typeName);
    int typeId = categoryIds.get(type);
    if (categoryIds.getNoEntryValue() == typeId) {
      typeId = categoryIds.size() + 1; // start ids from 1.
      categoryIds.put(type, typeId);
    }
    return typeId;
  }
}
