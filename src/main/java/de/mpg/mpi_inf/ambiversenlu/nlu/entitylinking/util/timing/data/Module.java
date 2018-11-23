package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data;

public class Module {

  private String id;

  private int numberOfCalls;

  private double averageExectionTime;

  private double maximumExecutionTime;

  private int executionLevel;

  public Module(String moduleId, int callLevel, int totalCalls, double avgTime, double maxTime) {
    id = moduleId;
    numberOfCalls = totalCalls;
    averageExectionTime = avgTime;
    maximumExecutionTime = maxTime;
    executionLevel = callLevel;
  }

  public String getId() {
    return id;
  }

  public int getExecutionLevel() {
    return executionLevel;
  }

  public int getNumberOfCalls() {
    return numberOfCalls;
  }

  public double getAverageExecutionTime() {
    return averageExectionTime;
  }

  public double getMaximumExecutionTime() {
    return maximumExecutionTime;
  }
}
