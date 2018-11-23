package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence.EntityOccurrenceCountsEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This providers iterates over all entities and get their occurrence counts in YAGO
 * by following the inlinks
 * IMPORTANT: You have to iterate over all records till the end, otherwise, there will be dangling DB connections!
 *
 */
public class YagoEntityOccurrenceCountsDataProvider extends EntityOccurrenceCountsEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(YagoEntityOccurrenceCountsDataProvider.class);

  @Override public Iterator<Entry<Integer, Integer>> iterator() {
    Entities entities = null;
    try {
      entities = DataAccess.getAllEntities();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }

    logger.info("Loading superdocs.");
    //    TIntObjectHashMap<int[]> allNeighbors = DataAccess.getInlinkNeighbors(entities);
    TIntObjectHashMap<int[]> allNeighbors = null;
    try {
      allNeighbors = DataAccess.getAllInlinks();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }
    TIntObjectHashMap<int[]> allSuperDocs = new TIntObjectHashMap<int[]>(allNeighbors.size());

    // add entity itself to superdocs
    for (int e : entities.getUniqueIds()) {
      // Add entity itself to superdoc.
      int[] superdoc = new int[] { e };

      // Add neighbors if they exist.
      if (allNeighbors.contains(e)) {
        int[] neighbors = allNeighbors.get(e);
        superdoc = Arrays.copyOf(neighbors, neighbors.length + 1);
        superdoc[superdoc.length - 1] = e;
      }
      allSuperDocs.put(e, superdoc);
    }
    logger.info("Got " + allSuperDocs.size() + " superdocs");

    Map<Integer, Integer> entitiesCounts = new HashMap<Integer, Integer>();

    for (int entity : entities.getUniqueIds()) {
      // Get superdoc and size.
      int[] superDocEntities = allSuperDocs.get(entity);
      entitiesCounts.put(entity, superDocEntities.length);
    }

    return entitiesCounts.entrySet().iterator();
  }
}
