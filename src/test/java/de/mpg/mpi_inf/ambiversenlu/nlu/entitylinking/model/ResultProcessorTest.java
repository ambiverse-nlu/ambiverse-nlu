package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ResultProcessorTest {
  public static final double DEFAULT_ALPHA = 0.6;

  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;

  public static final int DEFAULT_SIZE = 5;

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void test() throws Exception {
    Document.Builder docbuilder = new Document.Builder();
    docbuilder.withId("testPageKashmir1");
    docbuilder.withText("Page played Kashmir at Knebworth.");

    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(0, 4);
    annotations.addMention(12, 7);
    annotations.addMention(23, 9);
    docbuilder.withAnnotations(annotations);

    DisambiguationSettings settings = new CocktailPartyLangaugeModelDefaultDisambiguationSettings();
    settings.getGraphSettings().setAlpha(DEFAULT_ALPHA);
    settings.getGraphSettings().setCohRobustnessThresholdNE(DEFAULT_COH_ROBUSTNESS);
    settings.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);
    settings.setIncludeNullAsEntityCandidate(false);
    settings.setDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);

    docbuilder.withDisambiguationSettings(settings);

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    AnalyzeOutput ao = OutputUtils.generateAnalyzeOutputfromProcessedDocument(dp.process(docbuilder.build()));

    // TODO: test ao directly (as in DisambiguationPipelinesHolderTest)

    String result = OutputUtils.generateJSONStringfromAnalyzeOutput(ao);

    JSONAssert.assertEquals(
        "{\"docId\":\"testPageKashmir1\",\"language\":\"en\",\"matches\":[{\"charLength\":4,\"charOffset\":0,\"text\":\"Page\",\"entity\":{\"id\":\"http://www.wikidata.org/entity/TESTING:Jimmy_Page\",\"confidence\":1.0}},{\"charLength\":7,\"charOffset\":12,\"text\":\"Kashmir\",\"entity\":{\"id\":\"http://www.wikidata.org/entity/TESTING:Kashmir_(song)\",\"confidence\":1.0}},{\"charLength\":9,\"charOffset\":23,\"text\":\"Knebworth\",\"entity\":{\"id\":\"http://www.wikidata.org/entity/TESTING:Knebworth_Festival\",\"confidence\":1.0}}],\"entities\":[{\"id\":\"http://www.wikidata.org/entity/TESTING:Knebworth_Festival\",\"name\":\"Knebworth_Festival\",\"url\":\"http://Knebworth_Festival\",\"salience\":0.0},{\"id\":\"http://www.wikidata.org/entity/TESTING:Jimmy_Page\",\"name\":\"Jimmy_Page\",\"url\":\"http://Jimmy_Page\",\"salience\":0.0},{\"id\":\"http://www.wikidata.org/entity/TESTING:Kashmir_(song)\",\"name\":\"Kashmir_(song)\",\"url\":\"http://Kashmir_(song)\",\"salience\":0.0}]}",
        result,
        false);
  }
}