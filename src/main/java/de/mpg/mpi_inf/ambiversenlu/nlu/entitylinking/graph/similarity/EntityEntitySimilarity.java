package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EmptyEntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.EntityEntitySimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.InlinkOverlapEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MilneWittenEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.NullEntityEntitySimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

/**
 * Abstract class that should be used to create different ways of
 * calculating the similarity between two entities.
 *
 * The similarity is a value between 0.0 (dissimilar) and +infinity.
 *
 *
 */
public class EntityEntitySimilarity {

  EntityEntitySimilarityMeasure similarityMeasure;

  EntitiesContext entityContext;

  double weight;

  /**
   * Measures the similarity of two entities, described by their context.
   * The entityContext takes care of constructing it (pass an appropriate object
   * or use the get...Similarity() methods to get a predefined one.
   * The similarity calculation is taken care of by the given similarity object.
   *
   * @param similarityMeasure Similarity measure to used for calculating the similarity
   * between two entities
   * @param entityContext     Constructs the context of the entity
   * @throws Exception
   */
  public EntityEntitySimilarity(EntityEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) throws Exception {
    this.similarityMeasure = similarityMeasure;
    this.entityContext = entityContext;
  }

  /**
   * Calculates the similarity between entity a and b
   *
   * @param a Entity a
   * @param b Entity b
   * @return Similarity between a and b
   * @throws Exception
   */
  public double calcSimilarity(Entity a, Entity b) throws Exception {
    double sim = similarityMeasure.calcSimilarity(a, b, entityContext);
    return sim;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  /**
   * Calculates the entitiy similarity using an adjusted inlink overlap measure,
   * devised by Milne/Witten
   *
   * @param entities  Entities to caculate the similarity for
   * @return
   * @throws Exception
   */
  public static MilneWittenEntityEntitySimilarity getMilneWittenSimilarity(Entities entities, Tracer tracer) throws Exception {
    return new MilneWittenEntityEntitySimilarity(new NullEntityEntitySimilarityMeasure(tracer), new EmptyEntitiesContext(entities));
  }

  public static InlinkOverlapEntityEntitySimilarity getInlinkOverlapSimilarity(Entities entities, Tracer tracer) throws Exception {
    return new InlinkOverlapEntityEntitySimilarity(new NullEntityEntitySimilarityMeasure(tracer), new EmptyEntitiesContext(entities));
  }
  
  public EntityEntitySimilarityMeasure getSimilarityMeasure() {
    return similarityMeasure;
  }

  public EntitiesContext getContext() {
    return entityContext;
  }

  public String getIdentifier() {
    return similarityMeasure.getIdentifier() + ":" + entityContext.getIdentifier();
  }

  public String toString() {
    return getIdentifier();
  }
}