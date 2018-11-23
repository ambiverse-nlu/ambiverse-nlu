package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent;

import java.util.Map.Entry;

/**
 * This interface is responsible for providing "raw" data for entities importance
 * and at run time, the exact importance score will be computed out of them
 *
 */
public abstract class EntityImportanceComponentEntriesDataProvider implements Iterable<Entry<String, Integer>> {

  public abstract String getDBTableNameToStoreIn();

  public abstract String getKnowledgebaseName();
}
