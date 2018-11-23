package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;

import java.io.IOException;

/**
 * This class serves as way to get the importance of an entity
 * with regard to the complete collection, not to a specific mention (such as prior probability)
 *
 *
 */
public abstract class EntityImportance {

  private Entities entities;

  private double weight = 0.0;

  public EntityImportance(Entities entities) throws IOException, EntityLinkingDataAccessException {
    this.entities = entities;
    setupEntities(entities);
  }

  protected EntityImportance() {
    // Needed for subclasses that do not use internal entity ids and hence get only
    // KBIdentifiedEntity objects, not real Entity objects.
  }

  public Entities getEntities() {
    return entities;
  }

  protected abstract void setupEntities(Entities e) throws IOException, EntityLinkingDataAccessException;

  public abstract double getImportance(Entity entity) throws EntityLinkingDataAccessException;

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public String getIdentifier() {
    return this.getClass().getSimpleName();
  }
}
