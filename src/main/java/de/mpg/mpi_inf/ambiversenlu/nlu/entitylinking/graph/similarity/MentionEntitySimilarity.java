package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EmptyEntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.AlwaysOneSimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Context;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that should be used to create different ways of
 * calculating the similarity between a mention (with a context) and a given entity.
 *
 * The similarity is a value between 0.0 (dissimilar) and +infinity
 *
 *
 */
public class MentionEntitySimilarity {

  private static final Logger logger = LoggerFactory.getLogger(MentionEntitySimilarity.class);

  protected MentionEntitySimilarityMeasure similarityMeasure;

  protected EntitiesContext entitiesContext;

  private double weight;

  /**
   * Construct a similarity measure that compares the context of mention and entity
   * with the given similarityMeasure.
   * Or use the get...Similarity() method to get preconfigured ones.
   *
   * @param similarityMeasure Similarity measure to use for calculating the similarity
   * @param entityContext     Gets the context for the given entity
   */
  public MentionEntitySimilarity(MentionEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) {
    this(similarityMeasure, entityContext, 1.0);
  }

  /**
   * Construct a similarity measure that compares the context of mention and entity
   * with the given similarityMeasure.
   * Or use the get...Similarity() method to get preconfigured ones.
   *
   * @param similarityMeasure Similarity measure to use for calculating the similarity
   * @param entityContext     Gets the context for the given entity
   * @param weight            The weight of the given mention entity similarity (can be used for ensemble weighting)
   */
  public MentionEntitySimilarity(MentionEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext, double weight) {
    this.similarityMeasure = similarityMeasure;
    this.entitiesContext = entityContext;
    this.weight = weight;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public static MentionEntitySimilarity getDummyMentionEntitySimilarity(Entities entities, Tracer tracer) throws Exception {
    return new MentionEntitySimilarity(new AlwaysOneSimilarityMeasure(tracer), new EmptyEntitiesContext(entities));
  }

  public MentionEntitySimilarityMeasure getSimilarityMeasure() {
    return similarityMeasure;
  }

  public EntitiesContext getEntitiesContext() {
    return entitiesContext;
  }

  /**
   * Calculates the similarity between a mention and its context
   * (given as bag-of-words) and a given entity
   *
   * @param mention         Mention
   * @param entity          Entity
   * @param docId           ID of the originating document
   * @param mentionContext  Context of mention as bag of words
   * @return Similarity between mention and entity (in this context)
   * @throws Exception
   */
  public double calcSimilarity(Mention mention, Context context, Entity entity) throws Exception {
    logger.debug("Calculating similarity.");
    double sim = similarityMeasure.calcSimilarity(mention, context, entity, entitiesContext);
    return sim;
  }

  public void addExtraContext(Mention mention, Object context) {
    similarityMeasure.addExtraContext(mention, context);
  }

  public void announceMentionAssignment(Mention mention, Entity entity) {
    similarityMeasure.announceMentionAssignment(mention, entity);
  }

  public int[] getEntityContext(Entity entity) {
    return entitiesContext.getContext(entity);
  }

  public String toString() {
    return "" + similarityMeasure.toString() + ":" + entitiesContext.toString() + "";
  }

  public String getIdentifier() {
    return similarityMeasure.getIdentifier() + ":" + entitiesContext.getIdentifier();
  }

  public String getFileIdentifier() {
    return getIdentifier().replace(":", "-");
  }

}