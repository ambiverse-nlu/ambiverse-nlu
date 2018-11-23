package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.PreparedInputChunk;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

import java.io.IOException;

/**
 * Simplified version of the CocktailParty algorithm that 
 * - does no initial pruning (assumes graph is pruned beforehand in GraphGenerator).
 */
public class SimpleGreedy extends CocktailParty {

  public SimpleGreedy(PreparedInputChunk input,
      DisambiguationSettings settings, Tracer tracer) throws Exception {
    super(input, settings, tracer);
  }

  @Override protected int getDiameter() throws IOException {
    return -1;
  }

  @Override protected void removeInitialEntitiesByDistance(Graph graph) {
    // Don't prune, assume graph as is.
    return;
  }
}
