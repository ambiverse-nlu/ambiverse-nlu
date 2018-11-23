package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.PriorOnlyDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
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
public class DisambiguatorRealDBCzechIntegrationTest {

  public static final int DEFAULT_SIZE = 5;

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test
  public void testNapoleon() throws Exception {
    String text = "Napoleon byl císařem První francouzské říše. On byl poražen u Waterloo, Wellington a Blücher. " +
            "Byl zakázán svaté Heleně, zemřel na rakovinu žaludku a byl pohřben v Paříži.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(0,8);
    annotations.addMention(163,6);
    dbuilder.withAnnotations(annotations);

    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION);

    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.85);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);

    assertEquals("cs", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    String mapped = mappings.get("Napoleon");
    assertEquals("<Napoleon>", mapped);

    mapped = mappings.get("Waterloo");
    assertEquals("<Battle_of_Waterloo>", mapped);

    mapped = mappings.get("Blücher");
    assertEquals("<Gebhard_Leberecht_von_Blücher>", mapped);

    mapped = mappings.get("svaté Heleně");
    assertEquals("<Saint_Helena>", mapped);

    mapped = mappings.get("Paříži");
    assertEquals("<Paris>", mapped);

    // check that prior goes wrong
    doc.setDisambiguationSettings(new PriorOnlyDisambiguationSettings());
    jCas = dp.processDev(doc);
    result = ResultMention.getResultMentionsFromJCas(jCas);

    mappings = repackageMappings(result);

    mapped = mappings.get("Wellington");
    assertEquals("<Wellington>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }
}
