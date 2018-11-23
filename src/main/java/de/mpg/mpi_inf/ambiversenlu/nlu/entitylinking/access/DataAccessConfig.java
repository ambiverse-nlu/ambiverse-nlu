package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

public class DataAccessConfig {

  private DataAccessInterface[] databases;

  private Double[] weights;

  public DataAccessConfig(DataAccessInterface[] databases) {
    this.databases = databases;
  }

  public DataAccessInterface[] getDataAccesses() {
    return databases;
  }

  public Double[] getWeights() {
    return weights;
  }
}
