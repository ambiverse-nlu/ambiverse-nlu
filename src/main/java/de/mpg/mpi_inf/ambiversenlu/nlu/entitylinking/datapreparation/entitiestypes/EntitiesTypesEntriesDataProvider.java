package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes;

import java.util.Map.Entry;

public abstract class EntitiesTypesEntriesDataProvider implements Iterable<Entry<String, String>> {

  public abstract String getKnowledgebaseName();
}
