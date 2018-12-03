package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Match;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CocktailPartyTest {

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test
  public void testCocktailPartyConfidence() throws Exception {


    Document.Builder docbuilder = new Document.Builder();
    docbuilder.withId("testPageKashmir1");
    docbuilder.withText("When Page played Kashmir at Knebworth, his Les Paul was uniquely tuned.");
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(5,4);
    docbuilder.withAnnotations(annotations);

    DisambiguationSettings settings = new CocktailPartyLangaugeModelDefaultDisambiguationSettings();
    settings.setComputeConfidence(false);
    settings.getConfidenceSettings().setConfidenceBalance(1.0f);
    settings.setDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);

    docbuilder.withDisambiguationSettings(settings);

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    AnalyzeOutput ao = OutputUtils.generateAnalyzeOutputfromProcessedDocument(dp.process(docbuilder.build()));

    Map<String, Match> mappings = new HashMap<>();
    List<Match> matches = ao.getMatches();
    for(Match match: matches) {
      mappings.put(match.getText(), match);
    }

    //Not sure the purpose of this test, previously confidence was set to 1 to all entities. This changed when the language model was introduced why?
    de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity mapped = mappings.get("Page").getEntity();
    Double score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Jimmy_Page", mapped.getId());
    assertEquals(0.6081203302087401, score, 0.00001);

    mapped = mappings.get("Kashmir").getEntity();
    score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Kashmir_(song)", mapped.getId());
    assertEquals(0.380952380952381, score, 0.00001);

    mapped = mappings.get("Knebworth").getEntity();
    score = mapped.getConfidence();
    assertEquals("http://www.wikidata.org/entity/TESTING:Knebworth_Festival", mapped.getId());
    assertEquals(0.8061653840769745, score, 0.00001);

    mapped = mappings.get("Les Paul").getEntity();
    score = mapped.getConfidence();
    assertEquals(null, mapped.getId());
    assertEquals(null, score);
  }

}
