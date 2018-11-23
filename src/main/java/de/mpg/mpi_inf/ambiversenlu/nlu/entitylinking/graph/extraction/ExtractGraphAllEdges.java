package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.extraction;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;

public class ExtractGraphAllEdges extends ExtractGraph {

  public ExtractGraphAllEdges(String graphName, Mentions m, Entities ue, EnsembleEntityEntitySimilarity eeSim, double alpha) {
    super(graphName, m, ue, eeSim, alpha);
  }

  protected boolean haveDistinceMentions(String e1, String e2) {
    return true;
  }
}
