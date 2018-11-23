package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Context;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class NullMentionEntittySimilarityMeasure extends MentionEntitySimilarityMeasure {

  public NullMentionEntittySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override public double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext) {
    return 0;
  }
}
