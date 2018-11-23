package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing against the predefined DataAccessForTesting.
 * 
 */
public class DisambiguatorRealDBGermanIntegrationTest {
  public static final double DEFAULT_ALPHA = 0.6;
  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;
  public static final int DEFAULT_SIZE = 5;

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
    EntityLinkingManager.init();
  }

  @Test
  public void testGermanNER() throws Exception {
    String text = "Johannes wurde in Frankfurt geboren. Dort hat auch die DNB ihren Hauptsitz.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(55,3);
    dbuilder.withAnnotations(annotations);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    JCas jCas = dp.processDev(doc);

    assertEquals("de", jCas.getDocumentLanguage());

    Mentions mentions = Mentions.getNeMentionsFromJCas(jCas);
    System.out.println(mentions.size());
    assertTrue(mentions.size() == 3);
    Set<String> names = new HashSet<String>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        names.add(m.getMention());
      }
    }
    assertTrue(names.contains("Johannes"));
    assertTrue(names.contains("Frankfurt"));
    assertTrue(names.contains("DNB"));
  }

  @Test
  public void testDisambiguation() throws Exception {
    String text = "Albert Einstein wurde in Ulm geboren.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new CocktailPartyLangaugeModelDefaultDisambiguationSettings());
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);


    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.85);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);

    assertEquals("de", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    System.out.println(mappings.toString());

    String mapped = mappings.get("Albert Einstein");
    assertEquals("<Albert_Einstein>", mapped);

    mapped = mappings.get("Ulm");
    assertEquals("<Ulm>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }
}