package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.data.EntityTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.MeasureTracer;

public class NullTracer extends Tracer {

  EntityEntityTracing nullEETracing = new NullEntityEntityTracing();

  public NullTracer() {
    super(null, null);
  }

  @Override public void addEntityForMention(Mention mention, int entity, EntityTracer entityTracer) {
  }

  @Override public void addMeasureForMentionEntity(Mention mention, int entity, MeasureTracer measure) {
  }

  @Override public void setMentionEntityTotalSimilarityScore(Mention mention, int entity, double score) {
  }

  public EntityEntityTracing eeTracing() {
    return nullEETracing;
  }
}
