package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;

/**
 * Measures the importance of an entity by the number of
 * incoming links in Wikipedia/YAGO
 *
 *
 */
public class InlinkCountImportance extends EntityImportance {

  private TIntDoubleHashMap inlinkImportance;

  public InlinkCountImportance(Entities entities) throws IOException, EntityLinkingDataAccessException {
    super(entities);
  }

  @Override protected void setupEntities(Entities e) throws IOException, EntityLinkingDataAccessException {
    inlinkImportance = new TIntDoubleHashMap();
    TIntObjectHashMap<int[]> neighbors = DataAccess.getInlinkNeighbors(e);
    double collectionSize = (double) DataAccess.getCollectionSize();
    for (int eId : e.getUniqueIds()) {
      double importance = (double) neighbors.get(eId).length / (double) collectionSize;
      inlinkImportance.put(eId, importance);
    }
  }

  @Override public double getImportance(Entity entity) {
    return inlinkImportance.get(entity.getId());
  }

  public String toString() {
    return "InlinkCountImportance";
  }
}
