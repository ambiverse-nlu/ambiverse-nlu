package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class DataAccessIntegrationTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test public void testEntityMetadataWikidataId() throws EntityLinkingDataAccessException {

    Integer entityId = null;

    //This first part only grabs any entity without a wikidata id
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String sql = "SELECT entity FROM entity_metadata where wikidataid is NULL limit 1";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        entityId = rs.getInt("entity");
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    EntityMetaData metaData = DataAccess.getEntityMetaData(entityId);
    assertEquals(null, metaData.getWikiData());

    //This first part only grabs any entity with a wikidata id
    try {
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String sql = "SELECT entity FROM entity_metadata where wikidataid is NOT NULL limit 1";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        entityId = rs.getInt("entity");
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    metaData = DataAccess.getEntityMetaData(entityId);
    Assert.assertTrue(metaData.getWikiData().startsWith("Q"));
  }

}
