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
 * Builds the entity-type association relations:
 * - DataAccessSQL.ENTITY_TYPES
 * - DataAccessSQL.TYPE_ENTITIES
 *
 * Requires DataAccessSQL.TYPE_IDS created by
 * @see de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyBuilder
 */
public class EntitiesTypesDicionatriesBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntitiesTypesDicionatriesBuilder.class);

  private List<EntitiesTypesEntriesDataProvider> providers;

  public EntitiesTypesDicionatriesBuilder(List<EntitiesTypesEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException, EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = DataAccess.getAllEntityIds();
    TObjectIntHashMap<Type> typeIds = DataAccess.getAllTypeIds();

    // Use a hashset to store the type to deduplicate them.
    TIntObjectHashMap<TIntHashSet> entityTypesMap = new TIntObjectHashMap<TIntHashSet>();
    TIntObjectHashMap<TIntHashSet> typeEntitiesMap = new TIntObjectHashMap<TIntHashSet>();

    int entriesCount = 0;
    for (EntitiesTypesEntriesDataProvider provider : providers) {
      String knowledgebase = provider.getKnowledgebaseName();
      for (Entry<String, String> entityTypeEntry : provider) {
        String entityName = entityTypeEntry.getKey();
        String typeName = entityTypeEntry.getValue();

        int entity = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entityName, knowledgebase));
        int type = typeIds.get(new Type(knowledgebase, typeName));

        // The dictionary building phase discards some entities. Ignore them here.
        if (entity <= 0) {
          continue;
        }
        assert type > 0 : "No id for type: " + typeName;

        TIntHashSet types = entityTypesMap.get(entity);
        if (types == null) {
          types = new TIntHashSet();
          entityTypesMap.put(entity, types);
        }
        types.add(type);

        TIntHashSet entities = typeEntitiesMap.get(type);
        if (entities == null) {
          entities = new TIntHashSet();
          typeEntitiesMap.put(type, entities);
        }
        entities.add(entity);

        if (++entriesCount % 1000000 == 0) {
          logger.info("Read " + entriesCount + " entity-type entries.");
        }
      }
    }
    logger.info("Reading Entity-Type pairs DONE.");

    storeKeyValuesHashMap(entityTypesMap, DataAccessSQL.ENTITY_TYPES, "entity", "types");
    storeKeyValuesHashMap(typeEntitiesMap, DataAccessSQL.TYPE_ENTITIES, "type", "entities");

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
}
