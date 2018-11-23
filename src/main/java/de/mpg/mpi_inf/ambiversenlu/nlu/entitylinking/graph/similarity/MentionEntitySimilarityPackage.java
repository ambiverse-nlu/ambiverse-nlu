package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import java.util.List;

/**
 * Holds MentionEntitySimilarities with and without prior
 */
public class MentionEntitySimilarityPackage {

  List<MentionEntitySimilarity> mentionEntitySimilarityNoPrior;

  List<MentionEntitySimilarity> mentionEntitySimilarityWithPrior;

  public MentionEntitySimilarityPackage(List<MentionEntitySimilarity> mentionEntitySimilarityNoPrior,
      List<MentionEntitySimilarity> mentionEntitySimilarityWithPrior) {
    this.mentionEntitySimilarityNoPrior = mentionEntitySimilarityNoPrior;
    this.mentionEntitySimilarityWithPrior = mentionEntitySimilarityWithPrior;
  }

  public List<MentionEntitySimilarity> getMentionEntitySimilarityNoPrior() {
    return mentionEntitySimilarityNoPrior;
  }

  public List<MentionEntitySimilarity> getMentionEntitySimilarityWithPrior() {
    return mentionEntitySimilarityWithPrior;
  }
}
