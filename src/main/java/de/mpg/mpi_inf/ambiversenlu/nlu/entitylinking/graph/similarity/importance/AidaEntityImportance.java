package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.io.IOException;
import java.util.Collection;

public class AidaEntityImportance extends EntityImportance {

  private TIntDoubleHashMap entitiesImportances;

  public AidaEntityImportance(Entities entities) throws IOException, EntityLinkingDataAccessException {
    super(entities);
  }

  @Override protected void setupEntities(Entities e) throws IOException, EntityLinkingDataAccessException {
    Collection<Integer> entitiesIdsCollection = e.getUniqueIds();
    int[] entitiesIds = new int[entitiesIdsCollection.size()];
    int index = 0;
    for (int id : entitiesIdsCollection) {
      entitiesIds[index++] = id;
    }
    entitiesImportances = DataAccess.getEntitiesImportances(entitiesIds);
  }

  @Override public double getImportance(Entity entity) {
    return 1 - entitiesImportances.get(entity.getId());
  }

  @Override public String toString() {
    return "AidaEntityImportance";
  }
}
