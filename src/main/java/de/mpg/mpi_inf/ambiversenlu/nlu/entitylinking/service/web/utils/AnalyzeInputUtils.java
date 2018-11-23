package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.utils;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnnotatedMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.LanguageNotSupportedException;

import java.io.IOException;

public class AnalyzeInputUtils {

  public static Document getDocumentfromAnalyzeInput(AnalyzeInput input)
      throws ClassNotFoundException, NoSuchMethodException, MissingSettingException, IOException {

    Document.Builder docBuilder = new Document.Builder();
    docBuilder.withText(input.getText());
    if (input.getDocId() != null) {
      docBuilder.withId(input.getDocId());
    }
    String language = input.getLanguage();
    if (language != null) {
      try {
        docBuilder.withLanguage(Language.getLanguageForString(input.getLanguage()));
      } catch (IllegalArgumentException e) {
        throw new LanguageNotSupportedException("Language not supported.");
      }
    }

    DisambiguationSettings.Builder disBuilder = new DisambiguationSettings.Builder();
    Boolean isCoherent = input.getCoherentDocument();
    if (isCoherent != null) {
      if (isCoherent) {
        disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);
      } else {
        disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_LOCAL);
      }
    } else {
      disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);
    }

    Double confidenceThreshold = input.getConfidenceThreshold();
    if (confidenceThreshold != null) {
      disBuilder.withNullMappingThreshold(confidenceThreshold);
    }

    docBuilder.withDisambiguationSettings(disBuilder.build());
    docBuilder.withAnnotations(getDocumentAnnotationsfromAnalyzeInput(input));
    return docBuilder.build();
  }

  public static DocumentAnnotations getDocumentAnnotationsfromAnalyzeInput(AnalyzeInput input) {
    DocumentAnnotations result = new DocumentAnnotations();
    if (input.getAnnotatedMentions() != null) {
      for (AnnotatedMention annotatedMention : input.getAnnotatedMentions()) {
        result.addMention(annotatedMention.getCharOffset(), annotatedMention.getCharLength());
      }
    }
    return result;
  }
}