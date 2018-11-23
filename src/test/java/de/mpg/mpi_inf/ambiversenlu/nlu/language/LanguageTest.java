package de.mpg.mpi_inf.ambiversenlu.nlu.language;

import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LanguageTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void testLanguage() throws Exception {
    Document doc = new Document.Builder().withText("This is an english sentence.").build();
    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.LANGUAGE_DETECTION);
    ProcessedDocument pd = dp.process(doc);
    assertEquals("en", pd.getLanguage().toString());
  }

  @Test(expected = UnprocessableDocumentException.class) public void testLanguageException() throws Exception {
    Document doc = new Document.Builder().withText("En tant que général en chef et chef d'État, Napoléon tente de briser "
        + "les coalitions montées et financées par le royaume de Grande-Bretagne et qui rassemblent, depuis 1792, les monarchies européennes contre la France et son régime né de la Révolution.").build();
    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.LANGUAGE_DETECTION);
    dp.process(doc);
  }

}
