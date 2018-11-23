package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

/**
 * A non-existent entity, with Entity.OOKBE as identifier.
 */
public class NullEntity extends Entity {

  private static final long serialVersionUID = -1147100575481994318L;

  public NullEntity() {
    super(OOKBE, "AIDA", 0);
  }
}
