package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance;

import java.util.Map.Entry;

/**
 * This interface is responsible for providing the data for entities importances
 * entity importance is a double between 0 and 1
 *
 */
public abstract class EntityImportanceEntriesDataProvider implements Iterable<Entry<String, Double>> {

  public abstract String getKnowledgebaseName();
}
