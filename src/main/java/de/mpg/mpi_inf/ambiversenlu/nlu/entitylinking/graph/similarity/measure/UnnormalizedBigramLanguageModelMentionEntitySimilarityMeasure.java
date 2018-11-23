package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class UnnormalizedBigramLanguageModelMentionEntitySimilarityMeasure extends BigramLanguageModelMentionEntitySimilarityMeasure {

  public UnnormalizedBigramLanguageModelMentionEntitySimilarityMeasure(Tracer tracer) {
    super(tracer, false);
  }
}
