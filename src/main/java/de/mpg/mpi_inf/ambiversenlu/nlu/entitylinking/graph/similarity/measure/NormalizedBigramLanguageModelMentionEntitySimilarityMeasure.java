package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class NormalizedBigramLanguageModelMentionEntitySimilarityMeasure extends BigramLanguageModelMentionEntitySimilarityMeasure {

  public NormalizedBigramLanguageModelMentionEntitySimilarityMeasure(Tracer tracer) {
    super(tracer, true);
  }
}
