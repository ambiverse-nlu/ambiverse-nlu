package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public class NormalizedKeywordLanguageModelMentionEntitySimilarityMeasure extends KeywordLanguageModelMentionEntitySimilarityMeasure {

  public NormalizedKeywordLanguageModelMentionEntitySimilarityMeasure(Tracer tracer) {
    super(tracer, true);
  }
}
