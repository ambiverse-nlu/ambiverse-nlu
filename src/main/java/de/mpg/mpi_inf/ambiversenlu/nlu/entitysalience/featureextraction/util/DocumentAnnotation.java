package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;


import java.io.Serializable;

/**
 * A document-wide annotation. For example a salient entity.
 */
public class DocumentAnnotation implements Serializable, Comparable<DocumentAnnotation> {

  protected String id;

  protected double score;

  public DocumentAnnotation(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  @Override public int compareTo(DocumentAnnotation o) {
    return Double.compare(getScore(), o.getScore());
  }

}
