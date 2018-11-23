package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary;

import java.util.List;
import java.util.Map.Entry;

/**
 *
 * This interface is responsible for providing the data used to build the dictionary
 * In order to add more source of "means" to the dictionary you should write 
 * your own provider that implements this interface
 *
 */
public abstract class DictionaryEntriesDataProvider implements Iterable<Entry<String, List<DictionaryEntity>>> {

}
