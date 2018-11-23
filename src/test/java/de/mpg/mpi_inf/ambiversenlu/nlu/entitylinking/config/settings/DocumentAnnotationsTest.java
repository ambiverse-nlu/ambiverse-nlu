package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DocumentAnnotationsTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void testManualAnnotations()
      throws UIMAException, IOException, ClassNotFoundException, EntityLinkingDataAccessException,
      MissingSettingException, NoSuchMethodException, UnprocessableDocumentException {
    DocumentProcessor processor = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);

    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(43, 25);

    Document.Builder builder = new Document.Builder();
    builder.withLanguage(Language.getLanguageForString("en"))
            .withText("Lionel Messi does not want to play for the Argentinian National Team anymore.")
            .withAnnotations(annotations);
    JCas jCas = processor.processDev(builder.build());
    List<ResultMention> result = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<String, String> mappings = repackageMappings(result);
    assertTrue(mappings.containsKey("Argentinian National Team"));
  }

  private Map<String, String> repackageMappings(List<ResultMention> result) {
    Map<String, String> repack = new HashMap<String, String>();
    for (ResultMention rm : result) {
      repack.put(rm.getMention(), rm.getBestEntity().getEntity());
    }
    return repack;
  }
}
