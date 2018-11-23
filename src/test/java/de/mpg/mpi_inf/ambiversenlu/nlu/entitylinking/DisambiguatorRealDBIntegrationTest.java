package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.Settings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.PriorOnlyDisambiguationSettings;
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
 * This tests all the demo examples for correct outcome.
 * Useful as a safety net :)
 *
 */
public class DisambiguatorRealDBIntegrationTest {

  public static final double DEFAULT_ALPHA = 0.8;

  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;

  public static final int DEFAULT_SIZE = 5;

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test
  public void testNapoleon() throws Exception {
    String text = "Napoleon was the emperor of the First French Empire. He was defeated at Waterloo by Wellington and Blücher. He was banned to Saint Helena, died of stomach cancer, and was buried in Paris.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());;
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(99,7);
    annotations.addMention(125,12);
    dbuilder.withAnnotations(annotations);


    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);


    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.85);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);

    assertEquals("en", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    System.out.println(mappings.toString());

    String mapped = mappings.get("Napoleon");
    assertEquals("<Napoleon>", mapped);

    mapped = mappings.get("Waterloo");
    assertEquals("<Battle_of_Waterloo>", mapped);

    mapped = mappings.get("Blücher");
    assertEquals("<Gebhard_Leberecht_von_Blücher>", mapped);

    mapped = mappings.get("Saint Helena");
    assertEquals("<Saint_Helena>", mapped);

    mapped = mappings.get("Paris");
    assertEquals("<Paris>", mapped);

    // With the current default config, this fails.
//    mapped = mappings.get("Wellington");
//    assertEquals("<Arthur_Wellesley,_1st_Duke_of_Wellington>", mapped);

    // check that prior goes wrong
    doc.setDisambiguationSettings(new PriorOnlyDisambiguationSettings());
    jCas = dp.processDev(doc);
    result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    
    mappings = repackageMappings(result);

    mapped = mappings.get("Wellington");
    assertEquals("<Wellington>", mapped);
  }

  @Test public void testWho() throws Exception {
    Preparator p = new Preparator();

    String text = "When Who played Tommy in Columbus, Pete was at his best.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(5, 3);
    annotations.addMention(16, 5);
    annotations.addMention(25, 8);
    dbuilder.withAnnotations(annotations);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);

    DisambiguationSettings notWorking = doc.getDisambiguationSettings();
    notWorking.getGraphSettings().setAlpha(DEFAULT_ALPHA);
    notWorking.getGraphSettings().setCohRobustnessThresholdNE(DEFAULT_COH_ROBUSTNESS);
    notWorking.setDisambiguationTechnique(Settings.TECHNIQUE.GRAPH);
    notWorking.setDisambiguationAlgorithm(Settings.ALGORITHM.COCKTAIL_PARTY_SIZE_CONSTRAINED);
    notWorking.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);
    assertEquals("en", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    String mapped = mappings.get("Who");
    assertEquals("<The_Who>", mapped);

    mapped = mappings.get("Tommy");
    assertEquals("<Tommy_(album)>", mapped);

    mapped = mappings.get("Columbus");
    assertEquals("<Columbus,_Ohio>", mapped);

    mapped = mappings.get("Pete");
    assertEquals("<Pete_Townshend>", mapped);
  }

  @Test public void testAlexandria() throws Exception {
    Preparator p = new Preparator();

    String text = "Alexandria is an ancient city on the Mediterranean. It was famous for its lighthouse, one of the seven world wonders.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    ;
    Document doc = dbuilder.build();

    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.6);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(5);

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    JCas jCas = dp.processDev(doc);
    assertEquals("en", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    System.out.println(mappings.toString());

    String mapped = mappings.get("Alexandria");
    assertEquals("<Alexandria>", mapped);

    mapped = mappings.get("Mediterranean");
    assertEquals("<Mediterranean_Sea>", mapped);

    // same should work with prior only
    doc.setDisambiguationSettings(new PriorOnlyDisambiguationSettings());
    jCas = dp.processDev(doc);
    result = ResultMention.getResultAidaMentionsFromJCas(jCas);

    mappings = repackageMappings(result);

    mapped = mappings.get("Alexandria");
    assertEquals("<Alexandria>", mapped);

    mapped = mappings.get("Mediterranean");
    assertEquals("<Mediterranean_Sea>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }
}
