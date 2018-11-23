package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.KLDivergenceCalculator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class KLDivergenceCalculatorTest {

  @Test public void testCalcKLDivergence() {
    double smoothingParameter = 1d;

    // similarity 1
    KLDivergenceCalculator klDivergenceCalculator1 = new KLDivergenceCalculator(true);
    klDivergenceCalculator1.addSummand(10, 60, 4, 50, 200, 3_000_000, smoothingParameter);
    klDivergenceCalculator1.addSummand(20, 60, 2, 50, 300, 3_000_000, smoothingParameter);
    klDivergenceCalculator1.addSummand(30, 60, 2, 50, 1000, 3_000_000, smoothingParameter);

    // similarity 2 (more terms for the entity => lower similarity/higher KLD)
    KLDivergenceCalculator klDivergenceCalculator2 = new KLDivergenceCalculator(true);
    klDivergenceCalculator2.addSummand(10, 60, 4, 100, 200, 3_000_000, smoothingParameter);
    klDivergenceCalculator2.addSummand(20, 60, 2, 100, 300, 3_000_000, smoothingParameter);
    klDivergenceCalculator2.addSummand(30, 60, 2, 100, 1000, 3_000_000, smoothingParameter);

    // similarity 3 (more term occurrences in entity context => higher similarity/lower KLD)
    KLDivergenceCalculator klDivergenceCalculator3 = new KLDivergenceCalculator(true);
    klDivergenceCalculator3.addSummand(10, 60, 10, 50, 200, 3_000_000, smoothingParameter);
    klDivergenceCalculator3.addSummand(20, 60, 5, 50, 300, 3_000_000, smoothingParameter);
    klDivergenceCalculator3.addSummand(30, 60, 3, 50, 1000, 3_000_000, smoothingParameter);

    // similarity 4 (the entity with higher relevance now occurres more often in the doc => higher similarity/lower KLD)
    KLDivergenceCalculator klDivergenceCalculator4 = new KLDivergenceCalculator(true);
    klDivergenceCalculator4.addSummand(30, 60, 4, 50, 200, 3_000_000, smoothingParameter);
    klDivergenceCalculator4.addSummand(20, 60, 2, 50, 300, 3_000_000, smoothingParameter);
    klDivergenceCalculator4.addSummand(10, 60, 2, 50, 1000, 3_000_000, smoothingParameter);

    assertTrue("More terms per entity does not result in lower similarity/higher KLD",
        klDivergenceCalculator1.getKLDivergence() < klDivergenceCalculator2.getKLDivergence());
    assertTrue("More term occurrences in an entity does not result in higher similarity/lower KLD",
        klDivergenceCalculator1.getKLDivergence() > klDivergenceCalculator3.getKLDivergence());
    assertTrue("Greater occurrence of a more relevant entity does not result in higher similarity/lower KLD",
        klDivergenceCalculator1.getKLDivergence() > klDivergenceCalculator4.getKLDivergence());
  }
}
