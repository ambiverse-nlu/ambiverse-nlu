package de.mpg.mpi_inf.ambiversenlu.nlu.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.DocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.Serializable;

import static org.apache.uima.fit.util.JCasUtil.exists;

public class Document implements Serializable {

  public static final String DUMMY_ID= "dummy";

  private Language language;

  private DisambiguationSettings disambiguationSettings;

  private DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy;

  private Document.DOCUMENT_INPUT_FORMAT documentInputFormat;

  private String encoding;

  private String id;

  private String text;

  private DocumentAnnotations annotations;

  public static enum DOCUMENT_INPUT_FORMAT {
    TEXT
  }

  private Document() {
  }

  public Document(String text, String encoding, String id, Language language, DisambiguationSettings disambiguationSettings,
      DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy, Document.DOCUMENT_INPUT_FORMAT documentInputFormat, DocumentAnnotations annotations) {
    this.text = text;
    this.encoding = encoding;
    this.id = id;
    this.language = language;
    this.disambiguationSettings = disambiguationSettings;
    this.docChunkStrategy = docChunkStrategy;
    this.documentInputFormat = documentInputFormat;
    this.annotations = annotations;
  }

  protected Document(Builder builder) {
    this(builder.text, builder.encoding, builder.id, builder.language, builder.disambiguationSettings, builder.docChunkStrategy,
        builder.documentInputFormat, builder.annotations);
    if (language != null && disambiguationSettings != null) {
      disambiguationSettings.setLanguageSettings(LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(language));
    }
  }

  public Document clone() {
    Document clone = new Document();
    clone.text = text;
    clone.encoding = encoding;
    clone.id = id;
    clone.language = language;
    clone.disambiguationSettings = disambiguationSettings;
    clone.docChunkStrategy = docChunkStrategy;
    clone.documentInputFormat = documentInputFormat;
    clone.annotations = annotations;
    return clone;
  }

  public static class Builder {

    private String text;

    private Language language;

    private DisambiguationSettings disambiguationSettings;

    private DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy = DocumentChunker.DOCUMENT_CHUNK_STRATEGY.MULTIPLE_FIXEDLENGTH;

    private Document.DOCUMENT_INPUT_FORMAT documentInputFormat = DOCUMENT_INPUT_FORMAT.TEXT;

    private String encoding = "UTF-8";

    protected String id;

    private DocumentAnnotations annotations;

    public Builder withText(String text) {
      this.text = text;
      return this;
    }

    public Builder withLanguage(Language language) {
      this.language = language;
      return this;
    }

    public Builder withDisambiguationSettings(DisambiguationSettings disambiguationSettings) {
      this.disambiguationSettings = disambiguationSettings;
      return this;
    }

    public Builder withChunkinStrategy(DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy) {
      this.docChunkStrategy = docChunkStrategy;
      return this;
    }

    public Builder withDocumentInputFormat(Document.DOCUMENT_INPUT_FORMAT documentInputFormat) {
      this.documentInputFormat = documentInputFormat;
      return this;
    }

    public Builder withDocumentEncoding(String encoding) {
      this.encoding = encoding;
      return this;
    }

    public Builder withId(String id) {
      this.id = id;
      return this;
    }

    public Builder withAnnotations(DocumentAnnotations annotations) {
      this.annotations = annotations;
      return this;
    }

    private void generateDefaultId() {
      id = Integer.toString(Math.abs(text.substring(0, Math.min(text.length(), 100)).hashCode()));
    }

    public Document build() throws ClassNotFoundException, NoSuchMethodException, IOException {
      if (text == null) {
        throw new IllegalArgumentException("Text must be set.");
      }
      if (this.id == null) {
        generateDefaultId();
      }
      return new Document(this);
    }
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setDisambiguationSettings(DisambiguationSettings disambiguationSettings) {
    this.disambiguationSettings = disambiguationSettings;
  }

  public void setDocChunkStrategy(DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy) {
    this.docChunkStrategy = docChunkStrategy;
  }

  public void setDocumentInputFormat(Document.DOCUMENT_INPUT_FORMAT documentInputFormat) {
    this.documentInputFormat = documentInputFormat;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setDocumentId(String documentId) {
    this.id = documentId;
  }

  public String getText() {
    return text;
  }

  public Language getLanguage() {
    return language;
  }

  public DisambiguationSettings getDisambiguationSettings() {
    return disambiguationSettings;
  }

  public DocumentChunker.DOCUMENT_CHUNK_STRATEGY getDocChunkStrategy() {
    return docChunkStrategy;
  }

  public Document.DOCUMENT_INPUT_FORMAT getDocumentInputFormat() {
    return documentInputFormat;
  }

  public String getEncoding() {
    return encoding;
  }

  public String getDocumentId() {
    return id;
  }

  public void addSettingstoJcas(JCas jcas) throws IOException {
    AidaDocumentSettings ads = new AidaDocumentSettings(jcas);
    if (this.getLanguage() != null) {
      if (jcas.getDocumentLanguage() != null && !jcas.getDocumentLanguage().equals("x-unspecified") && !jcas.getDocumentLanguage()
          .equals(this.getLanguage().toString())) {
        throw new IllegalArgumentException("Language in JCas and language in settings are different");
      }
      ads.setLanguage(this.getLanguage().toString());
      jcas.setDocumentLanguage(ads.getLanguage());
    }
    if (this.getDocChunkStrategy() != null) {
      ads.setDocChunkStrategy(this.getDocChunkStrategy().toString());
    }
    ads.setDocumentId(this.getDocumentId());
    if (ads.getDocumentInputFormat() != null) {
      ads.setDocumentInputFormat(this.getDocumentInputFormat().toString());
    }
    ads.setEncoding(this.getEncoding());
    if (disambiguationSettings != null) {
      disambiguationSettings.addToJCas(ads, jcas);
    }
    ads.addToIndexes();
    if (annotations != null) {
      annotations.addMentionsToJCas(jcas);
    }
    if (!exists(jcas, DocumentMetaData.class)) {
      DocumentMetaData md = new DocumentMetaData(jcas);
      md.setDocumentId(ads.getDocumentId());
      md.addToIndexes();
    }
  }
}