package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessForTesting;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriorProbabilityTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void test() throws Exception {
    Set<Mention> mentions = new HashSet<>();
    Mention kashmirMention = new Mention();
    kashmirMention.setMention("Kashmir");

    Mention pageMention = new Mention();
    pageMention.setMention("Page");

    mentions.add(kashmirMention);
    mentions.add(pageMention);

    Entity kashmir = DataAccessForTesting.getTestEntity("Kashmir");
    Entity kashmirSong = DataAccessForTesting.getTestEntity("Kashmir_(song)");
    Entity jimmy = DataAccessForTesting.getTestEntity("Jimmy_Page");
    Entity larry = DataAccessForTesting.getTestEntity("Larry_Page");
    
    PriorProbability pp = new MaterializedPriorProbability(mentions, true);
    
    double ppKashmirKashmir = pp.getPriorProbability(kashmirMention, kashmir);
    double ppKashmirKashmirSong = pp.getPriorProbability(kashmirMention, kashmirSong);

    assertTrue(ppKashmirKashmir > ppKashmirKashmirSong);
    assertEquals(0.9, ppKashmirKashmir, 0.001);
    assertEquals(1.0, ppKashmirKashmir + ppKashmirKashmirSong, 0.001);

    double ppPageJimmy = pp.getPriorProbability(pageMention, jimmy);
    double ppPageLarry = pp.getPriorProbability(pageMention, larry);

    assertTrue(ppPageJimmy < ppPageLarry);
    assertEquals(0.3, ppPageJimmy, 0.001);
    assertEquals(1.0, ppPageJimmy + ppPageLarry, 0.001);
  }
}