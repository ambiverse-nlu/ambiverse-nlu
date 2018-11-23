package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DisambiguatorRealDBSpanishIntegrationTest {

  public static final int DEFAULT_SIZE = 5;

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test public void testNER() throws Exception {
    String text = "Maradona nació en la ciudad de Buenos Aires.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    JCas jCas = dp.processDev(doc);

    assertEquals("es", jCas.getDocumentLanguage());

    Mentions mentions = Mentions.getNeMentionsFromJCas(jCas);
    Set<String> names = new HashSet<String>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        names.add(m.getMention());
      }
    }
    assertTrue(names.contains("Maradona"));
    assertTrue(names.contains("Buenos Aires"));
  }

  @Test public void testDisambiguation() throws Exception {
    String text = "Messi nació en la ciudad de Rosario.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withLanguage(Language.getLanguageForString("es"))
            .withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);

    DisambiguationSettings working = doc.getDisambiguationSettings();
    working.getGraphSettings().setAlpha(0.85);
    working.getGraphSettings().setCohRobustnessThresholdNE(0.9);
    working.getGraphSettings().setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    JCas jCas = dp.processDev(doc);

    assertEquals("es", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    System.out.println(mappings.toString());

    String mapped = mappings.get("Messi");
    assertEquals("<Lionel_Messi>", mapped);

    mapped = mappings.get("Rosario");
    assertEquals("<Rosario,_Santa_Fe>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }

}
