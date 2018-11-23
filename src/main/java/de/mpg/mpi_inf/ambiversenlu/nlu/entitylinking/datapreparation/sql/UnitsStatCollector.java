package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.UnitUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is there to create the global count tables for units like keyword_counts.
 */
public class UnitsStatCollector {

  private static final Logger logger = LoggerFactory.getLogger(UnitsStatCollector.class);

  /**
   * @param args
   * @throws java.sql.SQLException
   */
  public static void main(String[] args) throws SQLException, EntityLinkingDataAccessException {

    if (args.length == 0) {
      System.out.println("A unit type is needed e.g. 'KEYWORD' or 'BIGRAM'");
      return;
    }
    UnitType unitType = UnitType.valueOf(args[0]);
    if (unitType != null) {
      UnitsStatCollector unitsStatCollector = new UnitsStatCollector();
      unitsStatCollector.startCollectingStatsInMemory(unitType);
    } else System.out.println(args[0] + " is not a valid unit type");
  }

  public void startCollectingStatsInMemory(UnitType unitType) throws SQLException, EntityLinkingDataAccessException {
    TIntObjectHashMap<TIntHashSet> entityUnits = UnitUtil.loadEntityUnits(unitType.getUnitSize());

    // Count the occurrences of units with respect to entities
    TIntIntHashMap unitCounts = new TIntIntHashMap();
    for (int entity : entityUnits.keys()) {
      TIntHashSet units = entityUnits.get(entity);
      for (int unit : units.toArray()) {
        unitCounts.adjustOrPutValue(unit, 1, 1);
      }
    }

    logger.info("Storing data ...");
    storeUnitsIntoDB(unitCounts, unitType);
  }

  private void storeUnitsIntoDB(TIntIntHashMap unitCounts, UnitType unitType) throws SQLException {
    String unitName = unitType.getUnitName();
    String tableName = unitType.getUnitCountsTableName();

    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement schemaStatement = con.createStatement();

    String sql = "CREATE TABLE " + tableName + " (" + unitName + " INTEGER, count INTEGER)";
    schemaStatement.execute(sql);
    schemaStatement.close();

    final PreparedStatement insertStatement;
    insertStatement = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + tableName + " VALUES(?,?)", 1_000_000);
    con.setAutoCommit(false);

    unitCounts.forEachEntry(new TIntIntProcedure() {

      @Override public boolean execute(int unit, int count) {
        try {
          insertStatement.setInt(1, unit);
          insertStatement.setInt(2, count);
          insertStatement.addBatch();
        } catch (SQLException e) {
          throw new RuntimeException(e);
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
    sql = "CREATE INDEX " + unitName + "_index ON " + tableName + " using btree (" + unitName + ");";
    schemaStatement.execute(sql);
    schemaStatement.close();

    EntityLinkingManager.releaseConnection(con);
  }
}
