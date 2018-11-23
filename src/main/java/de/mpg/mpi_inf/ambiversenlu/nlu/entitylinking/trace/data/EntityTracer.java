package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.data;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.MeasureTracer;

import java.util.LinkedList;
import java.util.List;

public class EntityTracer implements Comparable<EntityTracer> {

  private int entity;

  private double score;

  private List<MeasureTracer> measureTracers = new LinkedList<MeasureTracer>();

  public EntityTracer(int entity) {
    this.entity = entity;
  }

  public void addMeasureTracer(MeasureTracer mt) {
    measureTracers.add(mt);
  }

  public int compareTo(EntityTracer e) {
    return Double.compare(e.getTotalScore(), this.getTotalScore());
  }

  public int getEntityId() {
    return entity;
  }

  public List<MeasureTracer> getMeasureTracers() {
    return measureTracers;
  }

  public double getTotalScore() {
    return score;
  }

  public void setTotalScore(double score) {
    this.score = score;
  }
}
