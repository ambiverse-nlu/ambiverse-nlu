package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public abstract class SimilarityMeasure {

  protected Tracer tracer = null;

  public SimilarityMeasure(Tracer tracer) {
    this.tracer = tracer;
  }

  public String toString() {
    return getIdentifier();
  }

  public String getIdentifier() {
    String id = this.getClass().getSimpleName();
    return id;
  }

  public Tracer getTracer() {
    return tracer;
  }
}
