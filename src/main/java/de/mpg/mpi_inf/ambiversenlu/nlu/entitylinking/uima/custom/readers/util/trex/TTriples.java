package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex;

import com.jsoniter.annotation.JsonProperty;

public class TTriples {

  public TTriples(){}

  @JsonProperty
  public int sentence_id;

  @JsonProperty
  public TAnnotation predicate;

  @JsonProperty
  public TAnnotation object;

  @JsonProperty
  public TAnnotation subject;

  @JsonProperty
  public String dependency_path;

  @JsonProperty
  public Double confidence;

  @JsonProperty
  public String annotator;
}
