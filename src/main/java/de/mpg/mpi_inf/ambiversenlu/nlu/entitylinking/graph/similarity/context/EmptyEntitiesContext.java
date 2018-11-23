package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;

public class EmptyEntitiesContext extends EntitiesContext {

  public EmptyEntitiesContext(Entities entities) throws Exception {
    super(entities, null);
  }

  @Override public int[] getContext(Entity entity) {
    return null;
  }

  @Override protected void setupEntities(Entities entities) throws Exception {
    // nothing
  }

  public String toString() {
    return "EmptyEntitiesContext";
  }
}
