package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures;

import java.util.HashMap;
import java.util.Map;

public class TermTracer implements Comparable<TermTracer> {

  double termWeight;

  Map<Integer, Double> innerMatches = new HashMap<Integer, Double>();

  public double getTermWeight() {
    return termWeight;
  }

  public void setTermWeight(double termWeight) {
    this.termWeight = termWeight;
  }

  public Map<Integer, Double> getInnerMatches() {
    return innerMatches;
  }

  public void addInnerMatch(Integer inner, Double weight) {
    innerMatches.put(inner, weight);
  }

  @Override public int compareTo(TermTracer o) {
    return Double.compare(termWeight, o.getTermWeight());
  }
}
