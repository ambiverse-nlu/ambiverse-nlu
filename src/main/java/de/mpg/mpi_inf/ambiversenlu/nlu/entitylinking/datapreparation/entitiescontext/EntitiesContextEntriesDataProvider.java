package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext;

import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * This interface is responsible for providing the data used to construct
 * the context of the entities. In order to add more data to the entities context, you need to write 
 * your own provider that implements this interface
 *
 */

public abstract class EntitiesContextEntriesDataProvider implements Iterable<Entry<String, Set<EntityContextEntry>>> {

  public abstract String getKnowledgebaseName();
}
