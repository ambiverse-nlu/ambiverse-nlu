package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures;

public abstract class MeasureTracer extends TracerPart {

  // holds the name of the measure
  protected String name;

  // the value of the similarity of this measure
  protected double score;

  protected double weight;

  public MeasureTracer(String name, double weight) {
    super();
    this.name = name;
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }
}
