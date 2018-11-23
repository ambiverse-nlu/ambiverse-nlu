package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import java.util.Map;

/**
 * A single training instance.
 */
public class TrainingInstance extends FeatureValueInstance {
  private String entityId;
  private String docId;

  private Double label;

  public TrainingInstance(Double label, Map<Integer, Double> featureValues) {
    super(featureValues);
    this.label = label;
  }

  public TrainingInstance(Double label, Map<Integer, Double> featureValues, String entityId, String docId) {
    super(featureValues);
    this.entityId = entityId;
    this.docId = docId;
    this.label = label;
  }

  public Double getLabel() {
    return label;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getDocId() {
    return docId;
  }

  public void setDocId(String docId) {
    this.docId = docId;
  }

  public String toLibSVMString() {
    StringBuilder sb = new StringBuilder();
    sb.append(label).append(" ").append(super.toLibSVMString());
    return sb.toString();
  }

  public String toString() {
    return toLibSVMString();
  }
}
