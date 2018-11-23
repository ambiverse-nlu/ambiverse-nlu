package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.apache.uima.UIMAException;

import java.io.IOException;

public class UimaTokenizer {

  public static Tokens tokenize(Language language, String text)
      throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException,
      ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    return tokenize(language, text, PipelineType.TOKENIZATION);
  }

  public static Tokens tokenize(Language language, String text, PipelineType type)
      throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException,
      ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    DocumentProcessor dp = DocumentProcessor.getInstance(type);
    Document doc = new Document.Builder().withText(text).withLanguage(language).build();
    ProcessedDocument output = dp.process(doc);
    return output.getTokens();
  }
}
