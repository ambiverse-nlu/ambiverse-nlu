package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Context;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

public abstract class MentionEntitySimilarityMeasure extends SimilarityMeasure {

  public MentionEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  protected boolean useDistanceDiscount = false;

  public boolean isUseDistanceDiscount() {
    return useDistanceDiscount;
  }

  public void setUseDistanceDiscount(boolean useDistanceDiscount) {
    this.useDistanceDiscount = useDistanceDiscount;
  }

  public abstract double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext)
      throws EntityLinkingDataAccessException;

  /**
   * This method is a place holder to enable the framework to add extra context to a specific mention 
   * during the processing of the code
   * subclasses should override this method accordingly
   * @param context the context to add
   */

  /**
   * This method is a place holder to enable the framework to add extra context to a specific mention 
   * during the processing of the code
   * subclasses should override this method accordingly
   *
   * @param mention the mention to which this context belongs
   * @param context the context to add
   */
  public void addExtraContext(Mention mention, Object context) {
    return;
  }

  /**
   *  This method is a place holder to enable the framework to announce when a mention gets assigned to an entity
   *  different measures may perform different upon such event.
   *  default implementation is doing nothing
   *
   * @param mention the mention that was assigned
   * @param entity the entity to which the mention got assigned
   */
  public void announceMentionAssignment(Mention mention, Entity entity) {
    return;
  }

}
