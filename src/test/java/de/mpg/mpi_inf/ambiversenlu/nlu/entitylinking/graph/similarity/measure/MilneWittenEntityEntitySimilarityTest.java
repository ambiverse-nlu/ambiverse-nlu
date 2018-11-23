package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessForTesting;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullTracer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MilneWittenEntityEntitySimilarityTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void mwTest() throws Exception {
    Entity a = DataAccessForTesting.getTestEntity("Kashmir_(song)");
    Entity b = DataAccessForTesting.getTestEntity("Jimmy_Page");
    Entity c = DataAccessForTesting.getTestEntity("Larry_Page");
    Entity d = DataAccessForTesting.getTestEntity("Knebworth_Festival");

    Entities entities = new Entities();
    entities.add(a);
    entities.add(b);
    entities.add(c);
    entities.add(d);

    EntityEntitySimilarity mwSim = EntityEntitySimilarity.getMilneWittenSimilarity(entities, new NullTracer());

    double simAB = mwSim.calcSimilarity(a, b);
    double simAC = mwSim.calcSimilarity(a, c);
    double simBD = mwSim.calcSimilarity(b, d);
    double simCD = mwSim.calcSimilarity(c, d);
    double simAD = mwSim.calcSimilarity(a, d);

    assertTrue(simAB > simAC);
    assertTrue(simAD < simAB);
    assertTrue(simBD > simCD);
    assertEquals(0.9493, simAB, 0.0001);
    assertEquals(0.8987, simBD, 0.0001);
    assertEquals(0.9197, simAD, 0.0001);
    assertEquals(0.0, simCD, 0.001);
  }
}
