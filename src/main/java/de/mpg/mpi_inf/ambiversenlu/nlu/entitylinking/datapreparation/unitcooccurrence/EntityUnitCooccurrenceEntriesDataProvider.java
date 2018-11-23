package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;

/**
 * This interface is responsible for providing the data for the
 * co-occurrence statistics between entities and units.
 */
public abstract class EntityUnitCooccurrenceEntriesDataProvider implements Iterable<EntityUnitCooccurrenceEntry> {

  UnitType unitType;

  public EntityUnitCooccurrenceEntriesDataProvider(UnitType unitType) {
    this.unitType = unitType;
  }

  public UnitType getUnitType() {
    return unitType;
  }
}
