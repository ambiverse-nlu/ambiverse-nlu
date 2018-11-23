package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtil {

  public static PreparedStatement getAutoExecutingPeparedStatement(Connection con, String sql, int batchSize) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(sql);
    AutoExecutingPreparedStatement autoStmt = new AutoExecutingPreparedStatement(prepStmt, batchSize);
    return autoStmt;
  }

  public synchronized  static void addBatch(PreparedStatement prepStmt) {
    try {
      prepStmt.addBatch();
    } catch (SQLException sqle) {
      sqle.getNextException().printStackTrace();
      throw new RuntimeException(sqle);
    }
  }

  public synchronized  static void executeBatch(PreparedStatement prepStmt) {
    try {
      prepStmt.executeBatch();
    } catch (SQLException sqle) {
      sqle.getNextException().printStackTrace();
      throw new RuntimeException(sqle);
    }
  }
}
