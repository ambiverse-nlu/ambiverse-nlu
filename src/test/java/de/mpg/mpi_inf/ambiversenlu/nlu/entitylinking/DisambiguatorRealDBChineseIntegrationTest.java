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
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DisambiguatorRealDBChineseIntegrationTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("integration_test");
  }

  @Test public void testNER() throws Exception {
    String text = "此前，奥巴马和卡斯特罗曾在南非已故总统曼德拉的葬礼上短暂握手。";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    JCas jCas = dp.processDev(doc);

    assertEquals("zh", jCas.getDocumentLanguage());

    Mentions mentions = Mentions.getNeMentionsFromJCas(jCas);

    assertEquals(4, mentions.size());
    Set<String> names = new HashSet<String>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        names.add(m.getMention());
      }
    }

    assertTrue(names.contains("奥巴马"));
    assertTrue(names.contains("卡斯特罗"));
    assertTrue(names.contains("南非"));
    assertTrue(names.contains("曼德拉"));
  }

  @Test public void testDisambiguation() throws Exception {

    String text = "去年12月，美国总统奥巴马发表讲话宣布，美国决定与古巴恢复正常关系，并放宽多项执行了50多年的制措施。";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);

    JCas jCas = dp.processDev(doc);

    assertEquals("zh", jCas.getDocumentLanguage());

    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);

    String mapped = mappings.get("美国");
    assertEquals("<United_States>", mapped);

    mapped = mappings.get("奥巴马");
    assertEquals("<Barack_Obama>", mapped);

    mapped = mappings.get("美国");
    assertEquals("<United_States>", mapped);

    mapped = mappings.get("古巴");
    assertEquals("<Cuba>", mapped);
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }

}
