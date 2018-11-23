package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.UIMAException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UimaTokenizerTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test_multi");
  }

  @Test public void testTokenizer()
      throws MissingSettingException, IOException, ClassNotFoundException, EntityLinkingDataAccessException,
      NoSuchMethodException, UIMAException, UnprocessableDocumentException {

    String test = "This is a test.";
    for (int i = 1; i < 100; i++) {
      Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("en"), test);
      assertEquals(tokens.size(), 5);
    }

    test = "Esto es un test.";
    for (int i = 1; i < 100; i++) {
      Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("es"), test);
      assertEquals(tokens.size(), 5);
    }

    test = "这是一个考验。";
    for (int i = 1; i < 100; i++) {
      Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("zh"), test);
      assertEquals(tokens.size(), 6);
    }
  }
}