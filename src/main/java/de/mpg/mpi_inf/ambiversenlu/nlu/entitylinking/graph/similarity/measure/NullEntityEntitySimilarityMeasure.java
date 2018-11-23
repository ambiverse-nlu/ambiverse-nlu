package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class NullEntityEntitySimilarityMeasure extends EntityEntitySimilarityMeasure {

  public NullEntityEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override public double calcSimilarity(Entity a, Entity b, EntitiesContext context) {
    return -1;
  }

}
