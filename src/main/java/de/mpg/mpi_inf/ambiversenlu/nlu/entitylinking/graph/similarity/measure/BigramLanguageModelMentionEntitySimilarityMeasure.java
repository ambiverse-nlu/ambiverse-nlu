package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public abstract class BigramLanguageModelMentionEntitySimilarityMeasure extends LanguageModelMentionEntitySimilarityMeasure {

  public BigramLanguageModelMentionEntitySimilarityMeasure(Tracer tracer, boolean normalized) {
    super(tracer, UnitType.BIGRAM, normalized);
  }
}
