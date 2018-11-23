package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class DataAccessCassandraIntegrationTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test_cass");
  }

  @Test public void testGetTypes() throws EntityLinkingDataAccessException {
  Entities entities = DataAccess.getEntitiesForMention("Merkel", 1.0, 0, true);
    TIntObjectHashMap<Set<Type>> types = DataAccess.getTypes(entities);
    Type politician = new Type("YAGO3", "<wordnet_politician_110450303>");

    Set<Type> allTypes = new HashSet<>();
    for (TIntObjectIterator<Set<Type>> itr = types.iterator(); itr.hasNext(); ) {
      itr.advance();
      allTypes.addAll(itr.value());
    }

    assertTrue(allTypes.contains(politician));
  }

}
