package de.mpg.mpi_inf.ambiversenlu.nlu.openie;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;


public class OpenIEFactExtractionIntegrationTest {


  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
    Component.CLAUSIE.params = new Object[] { "removeRedundant", false, "reverbForm", false };
  }

  @After public void tearDown() {
    Component.CLAUSIE.params = new Object[] { "removeRedundant", false, "reverbForm", true };
  }
  
  @Test
  public void testEnglishFactExtraction() throws Exception {
    OpenIEFactExtraction fe = new OpenIEFactExtraction(Language.getLanguageForString("en"));
    
    Collection<Fact> facts = fe.extractFacts("Albert Einstein was born in Ulm in Germany.");
    
    assertEquals(2, facts.size());
    
    // Assertions about fact 1
    Iterator<Fact> it = facts.iterator();
    Fact fact = it.next();
    
    List<Entity> subjectEntities = fact.getSubject().getEntitis();
    List<Entity> objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(2, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q937", subjectEntities.get(0).getId());  // Albert Einstein
    assertEquals("was born", fact.getRelation().getText());
    assertEquals("http://www.wikidata.org/entity/Q3012", objectEntities.get(0).getId());   // Ulm
    assertEquals("http://www.wikidata.org/entity/Q183", objectEntities.get(1).getId());   // Germany
    
    // Assertions about fact 2
    fact = it.next();
    
    subjectEntities = fact.getSubject().getEntitis();
    objectEntities = fact.getObject().getEntitis();
    
    assertEquals(1, subjectEntities.size());
    assertEquals(1, objectEntities.size());
    
    assertEquals("http://www.wikidata.org/entity/Q937", subjectEntities.get(0).getId());  // Albert Einstein
    assertEquals("was born", fact.getRelation().getText());
    assertEquals("http://www.wikidata.org/entity/Q3012", objectEntities.get(0).getId());   // Ulm
  }
}
