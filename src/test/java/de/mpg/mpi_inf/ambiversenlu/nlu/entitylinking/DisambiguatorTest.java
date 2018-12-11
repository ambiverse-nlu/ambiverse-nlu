package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.Preparator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Testing against the predefined DataAccessForTesting.
 * 
 */
public class DisambiguatorTest {
  public static final double DEFAULT_ALPHA = 0.6;
  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;
  public static final int DEFAULT_SIZE = 5;

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test
  public void testPageKashmir() throws Exception {

    String text = "When Page played Kashmir at Knebworth, his Les Paul was uniquely tuned.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(5,4);
    dbuilder.withAnnotations(annotations);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);

    DisambiguationSettings settings = doc.getDisambiguationSettings();

    settings.getGraphSettings().setAlpha(DEFAULT_ALPHA);
    settings.getGraphSettings().setCohRobustnessThresholdNE(DEFAULT_COH_ROBUSTNESS);
    settings.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);
    settings.setIncludeNullAsEntityCandidate(false);

    JCas jCas = dp.processDev(doc);
    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);


    String mapped = mappings.get("Page");
    assertEquals("Jimmy_Page", mapped);

    mapped = mappings.get("Kashmir");
    assertEquals("Kashmir_(song)", mapped);

    mapped = mappings.get("Knebworth");
    assertEquals("Knebworth_Festival", mapped);

    mapped = mappings.get("Les Paul");
    assertEquals(Entity.OOKBE, mapped);
  }

  @Test
  public void testNoMaxEntityRank() throws Exception {
    Preparator p = new Preparator();

    String text = "When Page played Kashmir at Knebworth, his Les Paul was uniquely tuned.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(5,4);
    dbuilder.withAnnotations(annotations);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);


    DisambiguationSettings settings = doc.getDisambiguationSettings();
    settings.setIncludeNullAsEntityCandidate(false);
    settings.getGraphSettings().setAlpha(DEFAULT_ALPHA);
    settings.getGraphSettings().setCohRobustnessThresholdNE(DEFAULT_COH_ROBUSTNESS);
    settings.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);
    settings.setMaxEntityRank(-0.1);

    JCas jCas = dp.processDev(doc);
    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);


    String mapped = mappings.get("Page");
    assertEquals(Entity.OOKBE, mapped);

    mapped = mappings.get("Kashmir");
    assertEquals(Entity.OOKBE, mapped);

    mapped = mappings.get("Knebworth");
    assertEquals(Entity.OOKBE, mapped);

    mapped = mappings.get("Les Paul");
    assertEquals(Entity.OOKBE, mapped);
  }


  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }

}
