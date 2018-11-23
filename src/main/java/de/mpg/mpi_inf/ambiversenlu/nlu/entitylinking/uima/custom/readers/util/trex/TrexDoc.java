package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex;

import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class TrexDoc {

  public TrexDoc(){};

  @JsonProperty
  public String docid;

  @JsonProperty
  public String title;

  @JsonProperty
  public String text;

  @JsonProperty
  public String uri;

  @JsonProperty
  public List<TAnnotation> entities;

  @JsonProperty
  public List<int[]> words_boundaries;

  @JsonProperty
  public List<int[]> sentences_boundaries;

  @JsonProperty
  public List<TTriples> triples;
}
