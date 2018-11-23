package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public abstract class KeywordLanguageModelMentionEntitySimilarityMeasure extends LanguageModelMentionEntitySimilarityMeasure {

  public KeywordLanguageModelMentionEntitySimilarityMeasure(Tracer tracer, boolean normalized) {
    super(tracer, UnitType.KEYWORD, normalized);
  }
}
