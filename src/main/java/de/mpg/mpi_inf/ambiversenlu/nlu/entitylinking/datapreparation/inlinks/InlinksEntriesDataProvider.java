package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks;

import java.util.Map.Entry;

/**
 * This interface is responsible for providing the data for the inlink relation between entities
 * This is used for computing the MilneWitten Coherence
 *
 */
public abstract class InlinksEntriesDataProvider implements Iterable<Entry<String, String>> {

  public abstract String getKnowledgebaseName();
}
