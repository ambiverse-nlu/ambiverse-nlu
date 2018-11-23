package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;

/**
 * The UnitType enum represents the types of Units as they are used
 * for the similarity measure. E.g: 'KEYWORD' or 'BIGRAM'
 */
public enum UnitType {
  // The Types must be determined sorted by the UnitLength in ascending order ( 1, 2, 3, ...) 
  KEYWORD("keyword", 1, DataAccessSQL.ENTITY_KEYWORDS, DataAccessSQL.KEYWORD_COUNTS, DatabaseKeyValueStore.ENTITY_KEYWORDS_ENTITY,
      DatabaseKeyValueStore.KEYWORD_COUNTS_KEYWORD), BIGRAM("bigram", 2, DataAccessSQL.ENTITY_BIGRAMS, DataAccessSQL.BIGRAM_COUNTS,
      DatabaseKeyValueStore.ENTITY_BIGRAMS_ENTITY, DatabaseKeyValueStore.BIGRAM_COUNTS_BIGRAM);

  private String unitName;

  private int unitSize;

  private String entityUnitCooccurrenceTableName;

  private String unitCountsTableName;

  private DatabaseKeyValueStore entityUnitCooccurrenceKeyValueStore;

  private DatabaseKeyValueStore unitCountsKeyValueStore;

  /**
   * @param unitName                        the name of the unit usually in lower case. E.g.: 'keyword' or 'bigram'
   * @param unitSize                        the size of the unit (it has to be &gt; 0). E.g.: 'bigram' => 2 ...
   * @param entityUnitCooccurrenceTableName the table name for the entity_unit co-occurrence.
   * @param unitCountsTableName             the name of the table for the global counts of the unit.
   */
  UnitType(String unitName, int unitSize, String entityUnitCooccurrenceTableName, String unitCountsTableName,
      DatabaseKeyValueStore entityUnitCooccurrenceKeyValueStore, DatabaseKeyValueStore unitCountsKeyValueStore) {
    if (unitSize < 1) throw new IllegalArgumentException("The unit size cant be less than 1.");
    this.unitName = unitName;
    this.unitSize = unitSize;
    this.entityUnitCooccurrenceTableName = entityUnitCooccurrenceTableName;
    this.unitCountsTableName = unitCountsTableName;
    this.entityUnitCooccurrenceKeyValueStore = entityUnitCooccurrenceKeyValueStore;
    this.unitCountsKeyValueStore = unitCountsKeyValueStore;
  }

  /**
   * Returns the name of the unit
   */
  public String getUnitName() {
    return unitName;
  }

  /**
   * Returns the unit size what is basically the number of tokens per unit
   */
  public int getUnitSize() {
    return unitSize;
  }

  /**
   * Gives the name of the table for the entity-unit co-occurrence data
   */
  public String getEntityUnitCooccurrenceTableName() {
    return entityUnitCooccurrenceTableName;
  }

  /**
   * Gives the name of the table for the global counts of a unit
   */
  public String getUnitCountsTableName() {
    return unitCountsTableName;
  }

  public DatabaseKeyValueStore getEntityUnitCooccurrenceKeyValueStore() {
    return entityUnitCooccurrenceKeyValueStore;
  }

  public DatabaseKeyValueStore getUnitCountsKeyValueStore() {
    return unitCountsKeyValueStore;
  }
}
