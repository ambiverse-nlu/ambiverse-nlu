package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeightComputationTest {

  @Test public void testComputeNPMI() {
    double npmi;
    npmi = WeightComputation.computeNPMI(1, 1, 1, 10);
    assertEquals(1.0, npmi, 0.001);

    npmi = WeightComputation.computeNPMI(1, 1, 0, 10);
    assertEquals(-1.0, npmi, 0.001);

    assertTrue(WeightComputation.computeNPMI(3, 3, 2, 10) > WeightComputation.computeNPMI(3, 3, 1, 10));
  }
}
