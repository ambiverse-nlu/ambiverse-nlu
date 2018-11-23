package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
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
public class ConceptDisambiguatorRealDBIntegrationTest {

  public static final double DEFAULT_ALPHA = 0.8;

  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;

  public static final int DEFAULT_SIZE = 5;

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test
  public void testEnglish() throws Exception {
    String text = "Jack founded Alibaba with investments from two banks.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());;

    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.ENTITY_CONCEPT_SALIENCE_STANFORD);


    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.85);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);

    assertEquals("en", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultConceptMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    System.out.println(mappings.toString());

    String mapped = mappings.get("banks");
    assertEquals("<Bank>", mapped);

    mapped = mappings.get("investments");
    assertEquals("<Investment>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }

    return repack;
  }
}
