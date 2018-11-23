package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

import java.util.List;

public class EnsembleEntityEntitySimilarity {

  private List<EntityEntitySimilarity> eeSims;

  public EnsembleEntityEntitySimilarity(Entities uniqueEntities, SimilaritySettings settings, Tracer tracer) throws Exception {
    eeSims = settings.getEntityEntitySimilarities(uniqueEntities, tracer);
  }

  public double calcSimilarity(Entity a, Entity b) throws Exception {
    double weightedSimilarity = 0.0;

    for (EntityEntitySimilarity eeSim : eeSims) {
      double sim = eeSim.calcSimilarity(a, b) * eeSim.getWeight();
      weightedSimilarity += sim;
    }

    return weightedSimilarity;
  }

  public List<EntityEntitySimilarity> getEeSims() {
    return eeSims;
  }
}
