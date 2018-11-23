package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public abstract class EntityEntitySimilarityMeasure extends SimilarityMeasure {

  public EntityEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  public abstract double calcSimilarity(Entity a, Entity b, EntitiesContext context) throws EntityLinkingDataAccessException;
}
