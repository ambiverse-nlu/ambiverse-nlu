package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Match;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CocktailPartySizeConstrainedTest {

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test
  public void testCocktailParty() throws Exception {

    Document.Builder docbuilder = new Document.Builder();
    docbuilder.withId("testPageKashmir1");
    docbuilder.withText("When Page played Kashmir at Knebworth, his Les Paul was uniquely tuned.");

    DisambiguationSettings settings = new CocktailPartyLangaugeModelDefaultDisambiguationSettings();
    settings.setComputeConfidence(false);
    docbuilder.withDisambiguationSettings(settings);
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(5,4);
    docbuilder.withAnnotations(annotations);

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    AnalyzeOutput ao = OutputUtils.generateAnalyzeOutputfromProcessedDocument(dp.process(docbuilder.build()));

    Map<String, Entity> mappings = new HashMap<>();
    List<Match> matches = ao.getMatches();
    for(Match match: matches) {
      mappings.put(match.getText(), match.getEntity());
    }

    Entity mapped = mappings.get("Page");
    Double score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Jimmy_Page", mapped.getId());
    assertEquals(0.6867701831293966, score, 0.00001);

    mapped = mappings.get("Kashmir");
    score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Kashmir_(song)", mapped.getId());
    assertEquals(0.4397143998422002, score, 0.00001);

    mapped = mappings.get("Knebworth");
    score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Knebworth_Festival", mapped.getId());
    assertEquals(0.918187376294634, score, 0.00001);

    mapped = mappings.get("Les Paul");
    score = mapped.getConfidence();
    assertEquals(null, mapped.getId());
    assertEquals(null, score);

  }

}
