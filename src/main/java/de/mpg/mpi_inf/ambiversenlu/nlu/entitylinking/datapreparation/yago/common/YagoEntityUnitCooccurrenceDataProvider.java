package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntry;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.UnitUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Returns the entity-*unit* co-occurrence counts in the super docs in YAGO
 * IMPORTANT: You have to iterate over all records till the end, otherwise, there will be dangling DB connections!
 */
public class YagoEntityUnitCooccurrenceDataProvider extends EntityUnitCooccurrenceEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(YagoEntityUnitCooccurrenceDataProvider.class);

  public YagoEntityUnitCooccurrenceDataProvider(UnitType unitType) {
    super(unitType);
  }

  @Override public Iterator<EntityUnitCooccurrenceEntry> iterator() {
    TIntObjectHashMap<TIntHashSet> entitiesUnits = null;
    try {
      entitiesUnits = UnitUtil.loadEntityUnits(getUnitType().getUnitSize());
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }

    logger.info("Reading all entities ");
    Entities allEntities = null;
    try {
      allEntities = DataAccess.getAllEntities();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }
    TIntSet entitiesIdsSet = entitiesUnits.keySet();
    Entities entities = new Entities();

    for (Entity entity : allEntities) {
      if (entitiesIdsSet.contains(entity.getId())) {
        entities.add(entity);
      }
    }
    //Entities entities = YagoLabelsToYagoDictionary.getEntitiesForIds(entitiesUnits.keys());
    logger.info("Reading all entities DONE");

    logger.info("Reading inlink neighbors");
    TIntObjectHashMap<int[]> allNeighbors = null;
    try {
      allNeighbors = DataAccess.getAllInlinks();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }
    logger.info("Reading inlink neighbors DONE");

    TIntObjectHashMap<int[]> allSuperDocs = new TIntObjectHashMap<int[]>(allNeighbors.size());

    // add entity itself to superdocs
    for (int e : entities.getUniqueIds()) {
      int[] superdoc;
      // Add neighbors if they exist.
      if (allNeighbors.contains(e)) {
        int[] neighbors = allNeighbors.get(e);
        superdoc = Arrays.copyOf(neighbors, neighbors.length + 1);
        superdoc[superdoc.length - 1] = e;
      } else
        // if not add only the entity itself to superdoc.
        superdoc = new int[] { e };
      allSuperDocs.put(e, superdoc);
    }
    logger.info("Got " + allSuperDocs.size() + " superdocs");

    List<EntityUnitCooccurrenceEntry> counts = new LinkedList<>();

    // TODO instead of iterating over units x superdocEntities:
    // iterate once over all superdocs, build one big map with all
    // unit counts, then get the counts for all units ...
    for (int entity : entities.getUniqueIds()) {
      // Get superdoc and size.
      int[] superDocEntities = allSuperDocs.get(entity);

      // Get unit counts for all superdocs.
      TIntSet unitsForEntity = entitiesUnits.get(entity);
      TIntIntHashMap unitCounts = new TIntIntHashMap(superDocEntities.length * 10);
      for (int superDocEntity : superDocEntities) {
        TIntHashSet superdocUnits = entitiesUnits.get(superDocEntity);
        if (superdocUnits != null) {
          for (TIntIterator itr = superdocUnits.iterator(); itr.hasNext(); ) {
            int unit = itr.next();
            // Only add units that are actually associated to the entity.
            if (unitsForEntity.contains(unit)) {
              unitCounts.adjustOrPutValue(unit, 1, 1);
            }
          }
        }
      }

      int eCount = 0;

      // Add all the counts for the given entity and units.      
      for (TIntIntIterator itr = unitCounts.iterator(); itr.hasNext(); ) {
        itr.advance();
        int unit = itr.key();
        int count = itr.value();
        counts.add(new EntityUnitCooccurrenceEntry(entity, unit, count));
      }

      // Log progress.
      if (++eCount % 10000 == 0) {
        logger.info("Finished " + eCount + " entities");
      }
    }

    return counts.iterator();
  }
}
