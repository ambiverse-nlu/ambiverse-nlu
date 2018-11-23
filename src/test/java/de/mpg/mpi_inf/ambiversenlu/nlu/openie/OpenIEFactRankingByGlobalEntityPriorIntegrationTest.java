package de.mpg.mpi_inf.ambiversenlu.nlu.openie;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.junit.Before;
import org.junit.Test;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;


public class OpenIEFactRankingByGlobalEntityPriorIntegrationTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }
  
  @Test
  public void testFactRanking() throws Exception {
    OpenIEFactExtraction fe = new OpenIEFactExtraction(Language.getLanguageForString("en"));
    
    // First, extract facts and make sure they appear in some certain order.
    Collection<Fact> facts = fe.extractFacts("Albert Einstein was born in Ulm. "
        + "Barack Obama was born in Honolulu. "
        + "Angela Merkel was born in Hamburg.");
    
    Iterator<Fact> it = facts.iterator();
    
    // Assertions about fact 1
    Fact fact = it.next();
    
    List<Entity> subjectEntities = fact.getSubject().getEntitis();
    List<Entity> objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());    
    
    assertEquals("http://www.wikidata.org/entity/Q937", subjectEntities.get(0).getId());   // Albert Einstein
    assertEquals("http://www.wikidata.org/entity/Q3012", objectEntities.get(0).getId());   // Ulm
    
    // Assertions about fact 2
    fact = it.next();
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q76", subjectEntities.get(0).getId());    // Barack Obama
    assertEquals("http://www.wikidata.org/entity/Q18094", objectEntities.get(0).getId());  // Honolulu
   
    // Assertions about fact 3
    fact = it.next();
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q567", subjectEntities.get(0).getId());   // Angela Merkel
    assertEquals("http://www.wikidata.org/entity/Q1055", objectEntities.get(0).getId());   // Hamburg
    
     
    // Now rank the facts and check their new order.
    OpenIEFactRankingByGlobalEntityPrior fr = new OpenIEFactRankingByGlobalEntityPrior();
    
    List<Fact> rankedFacts = fr.rankFacts(facts);
    
    assertEquals(3, facts.size());
    
    // Assertions about reordered fact 0
    fact = rankedFacts.get(0);
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q937", subjectEntities.get(0).getId());   // Albert Einstein
    assertEquals("http://www.wikidata.org/entity/Q3012", objectEntities.get(0).getId());   // Ulm
    
    // Assertions about reordered fact 1
    fact = rankedFacts.get(1);
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q567", subjectEntities.get(0).getId());   // Angela Merkel
    assertEquals("http://www.wikidata.org/entity/Q1055", objectEntities.get(0).getId());   // Hamburg
    
    // Assertions about reordered fact 2
    fact = rankedFacts.get(2);
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q76", subjectEntities.get(0).getId());    // Barack Obama
    assertEquals("http://www.wikidata.org/entity/Q18094", objectEntities.get(0).getId());  // Honolulu
  }

  
}
