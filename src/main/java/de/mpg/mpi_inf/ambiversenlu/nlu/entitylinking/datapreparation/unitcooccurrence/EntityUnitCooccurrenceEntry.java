package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence;

/**
 * This class represents an entry of a co-occurrence table of an unit.
 * (A row of a co-occurrence table like entity_keywords)
 */
public class EntityUnitCooccurrenceEntry {

  private int entity;

  private int unit;

  private int count;

  public EntityUnitCooccurrenceEntry(int entity, int unit, int count) {
    this.entity = entity;
    this.unit = unit;
    this.count = count;
  }

  public int getEntity() {
    return entity;
  }

  public int getUnit() {
    return unit;
  }

  public int getCount() {
    return count;
  }
}
