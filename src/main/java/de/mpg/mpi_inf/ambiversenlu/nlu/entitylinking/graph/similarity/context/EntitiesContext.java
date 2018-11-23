package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ExternalEntitiesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class EntitiesContext implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(EntitiesContext.class);

  public ExternalEntitiesContext getExternalContext() {
    return externalContext;
  }

  public void setExternalContext(ExternalEntitiesContext externalContext) {
    this.externalContext = externalContext;
  }

  public EntitiesContextSettings getSettings() {
    return settings;
  }

  public void setSettings(EntitiesContextSettings settings) {
    this.settings = settings;
  }

  protected Entities entities;

  protected ExternalEntitiesContext externalContext;

  protected EntitiesContextSettings settings;

  public EntitiesContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    this(entities, new ExternalEntitiesContext(), settings);
  }

  public EntitiesContext() {

  }

  public EntitiesContext(Entities entities, ExternalEntitiesContext externalContext, EntitiesContextSettings settings) throws Exception {
    this.entities = entities;
    this.externalContext = externalContext;
    this.settings = settings;

    long beginTime = System.currentTimeMillis();

    setupEntities(entities);

    long runTime = (System.currentTimeMillis() - beginTime) / 1000;
    logger.debug("Done setting up " + this + ": " + runTime + "s");
  }

  public void setEntities(Entities entities) throws Exception {
    this.entities = entities;
    setupEntities(entities);
  }

  public Entities getEntities() {
    return entities;
  }

  public abstract int[] getContext(Entity entity);

  protected abstract void setupEntities(Entities entities) throws Exception;

  public String toString() {
    return getIdentifier();
  }

  public String getIdentifier() {
    return this.getClass().getSimpleName();
  }
}
