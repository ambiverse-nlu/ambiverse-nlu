package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy;

import java.util.Map.Entry;

public abstract class TypeTaxonomyEntriesDataProvider implements Iterable<Entry<String, String>> {

  public abstract String getKnowledgebaseName();
}
