package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Configuration implements Comparable<Configuration> {

  private String name;
  private String path;

  private Boolean isGold = false;

  private EvaluationStats stats;

  private Double avgPrecision = Double.NaN;
  private Double avgRecall = Double.NaN;
  private Double meanAvgPrecision = Double.NaN;
  private Double meanAvgRecall = Double.NaN;
  private Double nerPrecision = Double.NaN;
  private Double nerRecall = Double.NaN;
  private Double nerF1 = Double.NaN;
  private Double nedPrecision = Double.NaN;
  private Double nedRecall = Double.NaN;
  private Double nedF1 = Double.NaN;
  private Double map = Double.NaN;

  public EvaluationStats getStats() {
    return stats;
  }

  public void setStats(EvaluationStats stats) {
    this.stats = stats;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getGold() {
    return isGold;
  }

  public void setGold(Boolean gold) {
    isGold = gold;
  }

  public Double getAvgPrecision() {
    return avgPrecision;
  }

  public void setAvgPrecision(Double avgPrecision) {
    this.avgPrecision = avgPrecision;
  }

  public Double getAvgRecall() {
    return avgRecall;
  }

  public void setAvgRecall(Double avgRecall) {
    this.avgRecall = avgRecall;
  }

  public Double getMeanAvgPrecision() {
    return meanAvgPrecision;
  }

  public void setMeanAvgPrecision(Double meanAvgPrecision) {
    this.meanAvgPrecision = meanAvgPrecision;
  }

  public Double getMeanAvgRecall() {
    return meanAvgRecall;
  }

  public void setMeanAvgRecall(Double meanAvgRecall) {
    this.meanAvgRecall = meanAvgRecall;
  }

  public Double getNerPrecision() {
    return nerPrecision;
  }

  public void setNerPrecision(Double nerPrecision) {
    this.nerPrecision = nerPrecision;
  }

  public Double getNerRecall() {
    return nerRecall;
  }

  public void setNerRecall(Double nerRecall) {
    this.nerRecall = nerRecall;
  }

  public Double getNerF1() {
    return nerF1;
  }

  public void setNerF1(Double nerF1) {
    this.nerF1 = nerF1;
  }

  public Double getNedPrecision() {
    return nedPrecision;
  }

  public void setNedPrecision(Double nedPrecision) {
    this.nedPrecision = nedPrecision;
  }

  public Double getNedRecall() {
    return nedRecall;
  }

  public void setNedRecall(Double nedRecall) {
    this.nedRecall = nedRecall;
  }

  public Double getNedF1() {
    return nedF1;
  }

  public void setNedF1(Double nedF1) {
    this.nedF1 = nedF1;
  }

  public Double getMap() {
    return map;
  }

  public void setMap(Double map) {
    this.map = map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Configuration conf = (Configuration) o;
    return Objects.equals(this.name, conf.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override public String toString() {
    return "Configuration{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", isGold=" + isGold + '}';
  }

  @Override public int compareTo(@NotNull Configuration o) {
    return o.isGold.compareTo(this.isGold);
  }
}
