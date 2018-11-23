package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model;

public class Stats {

  private Double precision;
  private Double recall;
  private Double f1;

  public Stats(Double precision, Double recall, Double f1) {
    this.precision = precision;
    this.recall = recall;
    this.f1 = f1;
  }

  public Double getPrecision() {
    return precision;
  }

  public void setPrecision(Double precision) {
    this.precision = precision;
  }

  public Double getRecall() {
    return recall;
  }

  public void setRecall(Double recall) {
    this.recall = recall;
  }

  public Double getF1() {
    return f1;
  }

  public void setF1(Double f1) {
    this.f1 = f1;
  }
}
