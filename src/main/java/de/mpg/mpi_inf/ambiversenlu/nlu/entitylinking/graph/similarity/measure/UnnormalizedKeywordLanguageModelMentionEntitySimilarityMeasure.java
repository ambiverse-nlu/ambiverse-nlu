package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class UnnormalizedKeywordLanguageModelMentionEntitySimilarityMeasure extends KeywordLanguageModelMentionEntitySimilarityMeasure {

  public UnnormalizedKeywordLanguageModelMentionEntitySimilarityMeasure(Tracer tracer) {
    super(tracer, false);
  }
}
