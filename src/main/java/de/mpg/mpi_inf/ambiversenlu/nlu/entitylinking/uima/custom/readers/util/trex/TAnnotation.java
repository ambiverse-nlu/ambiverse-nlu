package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex;

import com.jsoniter.annotation.JsonProperty;


public class TAnnotation {

  public TAnnotation() {}

  @JsonProperty
  public int[] boundaries;

  @JsonProperty
  public String surfaceform;

  @JsonProperty
  public String uri;

  @JsonProperty
  public String annotator;
}
