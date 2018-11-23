package de.mpg.mpi_inf.ambiversenlu.nlu.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.DocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

public class Collection extends Document {

  private String path;

  private Reader.COLLECTION_FORMAT collectionType;

  private Integer docStart;

  private Integer docEnd;

  public String getPath() {
    return path;
  }

  public Reader.COLLECTION_FORMAT getCollectionType() {
    return collectionType;
  }

  public Integer getDocStart() {
    return docStart;
  }

  public Integer getDocEnd() {
    return docEnd;
  }

  public String[] additionalParam;

  public Collection(String encoding, String id, Language language, DisambiguationSettings disambiguationSettings,
      DocumentChunker.DOCUMENT_CHUNK_STRATEGY docChunkStrategy, Document.DOCUMENT_INPUT_FORMAT documentInputFormat) {

    super(null, encoding, id, language, disambiguationSettings, docChunkStrategy, documentInputFormat, null);
  }

  private Collection(Builder builder) {
    super(builder);
    this.path = builder.path;
    this.collectionType = builder.collectionType;
    this.docStart = builder.docStart;
    this.docEnd = builder.docEnd;
    this.additionalParam = builder.additionalParam;
  }

  public String getText() {
    throw new java.lang.IllegalStateException("Illegal call. A collection does not contain text");
  }

  public static class Builder extends Document.Builder {

    private String path;

    private Reader.COLLECTION_FORMAT collectionType;

    private Integer docStart;

    private Integer docEnd;

    private String[] additionalParam;

    public Builder withCollectionType(Reader.COLLECTION_FORMAT type) {
      this.collectionType = type;
      return this;
    }

    public Builder withPath(String path) {
      this.path = path;
      return this;
    }

    public Builder withDocStart(int docStart) {
      this.docStart = docStart;
      return this;
    }

    public Builder withDocEnd(int docEnd) {
      this.docEnd = docEnd;
      return this;
    }

    public Builder withAdditionalParam(String... params) {
      this.additionalParam = params;
      return this;
    }

    private void generateDefaultId() {
      this.id = (Integer.toString(Math.abs(path.substring(0, Math.min(path.length(), 100)).hashCode())));
    }

    public Collection build() {
      if (path == null) {
        throw new IllegalArgumentException("A collection requires a path input, even though it is a dummy path");
      }

      if (collectionType == null) {
        throw new IllegalArgumentException("A collection requires a collection type");
      }

      if (this.id == null && path != null) {
        generateDefaultId();
      }

      return new Collection(this);
    }
  }
}
