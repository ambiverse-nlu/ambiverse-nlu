package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance.EntityImportanceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * This providers iterates over all entities and get their importance
 * based on inlink counts
 * IMPORTANT: You have to iterate over all records till the end, otherwise, 
 * there will be dangling DB connections!
 *
 */
public class YagoInlinkBasedEntityImportanceCountsDataProvider extends EntityImportanceEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(YagoInlinkBasedEntityImportanceCountsDataProvider.class);

  private String knowledgebaseName;

  //because this is shared between YAGO2, and YAGO3, which knowledgebase should be 
  //provided from outside
  public YagoInlinkBasedEntityImportanceCountsDataProvider(String knowledgebaseName) {
    this.knowledgebaseName = knowledgebaseName;
  }

  @Override public Iterator<Entry<String, Double>> iterator() {

    logger.info("Getting entities and links.");
    TObjectIntHashMap<KBIdentifiedEntity> kbentities2IdsMap = null;
    try {
      kbentities2IdsMap = DataAccess.getAllEntityIds();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }
    TObjectIntHashMap<String> entities2IdsMap = new TObjectIntHashMap<String>(kbentities2IdsMap.size());
    for (TObjectIntIterator<KBIdentifiedEntity> itr = kbentities2IdsMap.iterator(); itr.hasNext(); ) {
      itr.advance();
      entities2IdsMap.put(itr.key().getIdentifier(), itr.value());
    }

    TIntObjectHashMap<String> ids2EntitiesMap = reverseMap(entities2IdsMap);

    Set<String> entityNames = new HashSet<String>(entities2IdsMap.size());
    for (String e : entities2IdsMap.keySet()) {
      entityNames.add(e);
    }

    TIntObjectHashMap<int[]> allInlinks = null;
    try {
      allInlinks = DataAccess.getAllInlinks();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    }

    // Get inlinks for sorting.
    List<EntityInlink> entityInlinks = new ArrayList<EntityInlink>(allInlinks.size());
    for (TIntObjectIterator<int[]> itr = allInlinks.iterator(); itr.hasNext(); ) {
      itr.advance();
      String entityName = ids2EntitiesMap.get(itr.key());
      entityInlinks.add(new EntityInlink(entityName, itr.value().length));
      entityNames.remove(entityName);
    }

    logger.info("Sorting by link count.");
    Collections.sort(entityInlinks);
    // Add the remaining entities (with no inlinks) to the end of the list.
    // entityIds only contains the remaining entities.
    for (String e : entityNames) {
      entityInlinks.add(new EntityInlink(e, 0));
    }
    assert (entityInlinks.size() == entities2IdsMap.keySet().size());

    Map<String, Double> entitiesImportances = new HashMap<String, Double>();
    int entityCount = entities2IdsMap.keySet().size();
    int i = 0;
    for (EntityInlink el : entityInlinks) {
      double normRank = (double) i / (double) entityCount;
      entitiesImportances.put(el.entity, normRank);

      ++i;
    }

    return entitiesImportances.entrySet().iterator();
  }

  private TIntObjectHashMap<String> reverseMap(TObjectIntHashMap<String> map) {
    TIntObjectHashMap<String> reversedMap = new TIntObjectHashMap<String>();
    for (String key : map.keySet()) {
      reversedMap.put(map.get(key), key);
    }
    return reversedMap;

  }

  class EntityInlink implements Comparable<EntityInlink> {

    String entity;

    int inlinkCount;

    public EntityInlink(String entity, int inlinkCount) {
      super();
      this.entity = entity;
      this.inlinkCount = inlinkCount;
    }

    @Override public int compareTo(EntityInlink o) {
      // Need descending sort, reverse order.
      return new Integer(o.inlinkCount).compareTo(inlinkCount);
    }
  }

  @Override public String getKnowledgebaseName() {
    return knowledgebaseName;
  }

}
