package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Optimizable {
  public abstract List<Parameter> getParameters();

  public abstract int getCollectionSize();

  public abstract int[] getFullBatch();

  public abstract double run(ParameterConfig currentConfig)
      throws ExecutionException;
}
