package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.keyvaluestore.KeyValueStore;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Counter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DataAccessKeyValueStore implements DataAccessInterface {

  private static final Logger logger = LoggerFactory.getLogger(DataAccessKeyValueStore.class);

  // the load factor for all trove maps
  public static final float troveLoadFactor = Constants.DEFAULT_LOAD_FACTOR;

  private final DataAccess.type dataAccessType;

  public DataAccessKeyValueStore(DataAccess.type dataAccessType) {
    this.dataAccessType = dataAccessType;
    DataAccessKeyValueStoreHandler.singleton();
  }

  public DataAccess.type getAccessType() {
    return dataAccessType;
  }

  @Override public TIntDoubleHashMap getEntityPriors(String mention, boolean isNamedentity) throws EntityLinkingDataAccessException {
    TIntDoubleHashMap entityPriors = new TIntDoubleHashMap();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.DICTIONARY_MENTION;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      mention = EntityLinkingManager.conflateToken(mention, isNamedentity);
      KeyValueStore<byte[],byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      byte[] resultBytes = keyValueStore.get(codec.encodeKey(mention));
      if (resultBytes != null) {
        TIntDoubleHashMap tempResults = (TIntDoubleHashMap) codec.decodeValue(resultBytes);
        TIntObjectHashMap<EntityType> entityClasses = DataAccessCache.singleton().getEntityClasses(tempResults.keys());

        for(int entityId: entityClasses.keys()) {
          if (isNamedentity) {
            if (!(entityClasses.get(entityId) == EntityType.CONCEPT)) {
              entityPriors.put(entityId, tempResults.get(entityId));
            }
          }
          else {
            if (!(entityClasses.get(entityId) == EntityType.NAMED_ENTITY)) {
              entityPriors.put(entityId, tempResults.get(entityId));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entityPriors;
  }

  @Override public TIntObjectHashMap<List<MentionObject>> getMentionsForEntities(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<List<MentionObject>> result = new TIntObjectHashMap<>(entities.size(), 1.0f);
    try {
      if (entities.size() == 0) {
        return result;
      }
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_MENTION;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }

      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (Entity e : entities) {
        encodedKeys.add(codec.encodeKey(e.getId()));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);
      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        Integer id = (Integer) codec.decodeKey(entry.getKey());
        String[] mentions = (String[]) codec.decodeValue(entry.getValue());
        result.put(id, Arrays.stream(mentions)
                        .map(s-> new MentionObject(s, 0.0))
                        .collect(Collectors.toList()));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return result;
  }

  public TIntObjectHashMap<KBIdentifiedEntity> getKnowlegebaseEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<KBIdentifiedEntity> entityIds = new TIntObjectHashMap<>(getCapacity(ids.length), troveLoadFactor);
    if (ids.length == 0) {
      return entityIds;
    }
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_IDS_ID;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }

      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : ids) {
        encodedKeys.add(codec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        KeyValueStoreRow row = (KeyValueStoreRow) codec.decodeValue(entry.getValue());
        entityIds.put((int) codec.decodeKey(entry.getKey()), KBIdentifiedEntity.getKBIdentifiedEntity(row.getString(0), row.getString(1)));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entityIds;
  }

  public TObjectIntHashMap<String> getIdsForWords(Collection<String> keywords) throws EntityLinkingDataAccessException {
    logger.debug("Getting ids for words.");
    TObjectIntHashMap<String> wordIds = new TObjectIntHashMap<>(getCapacity(keywords.size()), troveLoadFactor);
    if (keywords.isEmpty()) {
      return wordIds;
    }
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.WORD_IDS_WORD;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }

      logger.debug("Creating codec.");
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);

      logger.debug("Creating key-value store for db {}.", db.getName());
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      logger.debug("Key-value store returned.");
      List<byte[]> encodedKeys = new ArrayList<>();
      for (String word : keywords) {
        logger.debug("Encoding keys for word {}.", word);
        encodedKeys.add(codec.encodeKey(word));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        wordIds.put((String) codec.decodeKey(entry.getKey()), (int) codec.decodeValue(entry.getValue()));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new EntityLinkingDataAccessException(e);
    }
    logger.debug("Word ids fetched.");
    return wordIds;
  }

  public String getConfigurationName() throws EntityLinkingDataAccessException {
    String confName = "";
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.METADATA_KEY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      byte[] resultBytes = keyValueStore.get(codec.encodeKey("confName"));
      if (resultBytes != null) {
        confName = (String) codec.decodeValue(resultBytes);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return confName;
  }

  public String getLanguages() throws EntityLinkingDataAccessException {
    String language = "";
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.METADATA_KEY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      byte[] resultBytes = keyValueStore.get(codec.encodeKey("language"));
      if (resultBytes != null) {
        language = (String) codec.decodeValue(resultBytes);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return language;
  }

  public int getCollectionSize() throws EntityLinkingDataAccessException {
    int collectionSize = 0;
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.METADATA_KEY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      byte[] resultBytes = keyValueStore.get(codec.encodeKey("collection_size"));
      if (resultBytes != null) {
        String sizeString = (String) codec.decodeValue(resultBytes);
        collectionSize = Integer.parseInt(sizeString);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return collectionSize;
  }

  @Override public String getDumpVersion() throws EntityLinkingDataAccessException {
    String dumpVersion = "";
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.METADATA_KEY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      KeyValueStore.EntryIterator it = keyValueStore.entryIterator();
      while (it.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = it.next();
        String key = (String) codec.decodeKey(entry.getKey());

        if (key.contains("WikipediaSource_")) {
          String value = (String) codec.decodeValue(entry.getValue());
          String currVersion = value.replaceAll("\\D+", "");
          if (currVersion.compareTo(dumpVersion) >= 1) {
            dumpVersion = currVersion;
          }
        }

      }

    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return dumpVersion;
  }

  @Override public Date getDumpCreationDate() throws EntityLinkingDataAccessException {
    Date dumpCreationDate = null;
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.METADATA_KEY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }

      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      byte[] resultBytes = keyValueStore.get(codec.encodeKey("creationDate"));
      if (resultBytes != null) {
        String dumpCreationString = (String) codec.decodeValue(resultBytes);
        //Extract the creation date from string

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z yyyy");
        LocalDateTime dateTime = LocalDateTime.parse(dumpCreationString, formatter);
        dumpCreationDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return dumpCreationDate;
  }

  @Override public int getTypeCollectionSize() throws EntityLinkingDataAccessException {
    int count = 0;
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.TYPE_IDS_ID;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      count = (int) keyValueStore.countRows();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return count;
  }

  //TODO maxEntityRank not implemented
  public Map<String, Entities> getEntitiesForMentions(Collection<String> mentions, double maxEntityRank,
                                                      int topByPrior, boolean isNamedentity) throws EntityLinkingDataAccessException {
    Map<String, Entities> candidates = new HashMap<>(mentions.size(), 1.0f);
    if (mentions.isEmpty()) {
      return candidates;
    }
    List<String> queryMentions = new ArrayList<>(mentions.size());
    for (String m : mentions) {
      queryMentions.add(EntityLinkingManager.conflateToken(m, isNamedentity));
      // Add an emtpy candidate set as default.
      candidates.put(m, new Entities());
    }
    Map<String, Map<Integer, Double>> queryMentionCandidates = new HashMap<>(queryMentions.size(), 1.0f);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.DICTIONARY_MENTION;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codecMentions = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (String queryMention : queryMentions) {
        encodedKeys.add(codecMentions.encodeKey(queryMention));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;

        String mention = (String) codecMentions.decodeKey(entry.getKey());
        TIntDoubleHashMap entitiesPriors = (TIntDoubleHashMap) codecMentions.decodeValue(entry.getValue());
        TIntObjectHashMap<EntityType> entityClasses = DataAccessCache.singleton().getEntityClasses(entitiesPriors.keys());

        Map<Integer, Double> entities = queryMentionCandidates.get(mention);
        if (entities == null) {
          entities = new HashMap<>(entitiesPriors.size(), 1.0f);
          queryMentionCandidates.put(mention, entities);
        }
        TIntDoubleIterator it = entitiesPriors.iterator();
        while(it.hasNext()) {
          it.advance();
          if (isNamedentity) {
            if (!(entityClasses.get(it.key()) == EntityType.CONCEPT)) {
              entities.put(it.key(), it.value());
            }
          }
          else {
            if (!(entityClasses.get(it.key()) == EntityType.NAMED_ENTITY)) {
              entities.put(it.key(), it.value());
            }
          }
        }
      }
      // Get the candidates for the original Strings.
      for (Map.Entry<String, Entities> entry: candidates.entrySet()) {
        String queryMention = EntityLinkingManager.conflateToken(entry.getKey(), isNamedentity);
        Map<Integer, Double> entityPriors = queryMentionCandidates.get(queryMention);
        if (entityPriors != null) {
          Integer[] ids;
          if (topByPrior > 0) {
            List<Integer> topIds = CollectionUtils.getTopKeys(entityPriors, topByPrior);
            int droppedByPrior = entityPriors.size() - topIds.size();
            Counter.incrementCountByValue("CANDIDATES_DROPPED_BY_PRIOR", droppedByPrior);
            ids = topIds.toArray(new Integer[topIds.size()]);
          } else {
            ids = entityPriors.keySet().toArray(new Integer[entityPriors.size()]);
          }
          TIntObjectHashMap<KBIdentifiedEntity> yagoEntityIds = getKnowlegebaseEntitiesForInternalIds(ArrayUtils.toPrimitive(ids));
          Entities entities = entry.getValue();
          for (int i = 0; i < ids.length; ++i) {
            entities.add(new Entity(yagoEntityIds.get(ids[i]), ids[i]));
          }
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return candidates;
  }

  public TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) throws EntityLinkingDataAccessException {
    if (entities.isEmpty()) {
      return new TIntObjectHashMap<>();
    }

    TIntObjectHashMap<int[]> neighbors = new TIntObjectHashMap<>(getCapacity(entities.size()), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_INLINKS_ENTITY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityInlinksCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      List<byte[]> encodedKeys = new ArrayList<>();
      for (Entity entity : entities) {
        encodedKeys.add(entityInlinksCodec.encodeKey(entity.getId()));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      if (keyValueMap.size() != entities.size()) {
        for (Entity entity : entities) {
          neighbors.put(entity.getId(), new int[0]);
        }
      }

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;

        int entity = (Integer) entityInlinksCodec.decodeKey(entry.getKey());
        int[] curNeighbors = (int[]) entityInlinksCodec.decodeValue(entry.getValue());
        neighbors.put(entity, curNeighbors);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return neighbors;
  }

  public TObjectIntHashMap<KBIdentifiedEntity> getInternalIdsForKBEntities(Collection<KBIdentifiedEntity> kbEntities)
      throws EntityLinkingDataAccessException {
    if (kbEntities.isEmpty()) {
      return new TObjectIntHashMap<>();
    }

    TObjectIntHashMap<KBIdentifiedEntity> entities = new TObjectIntHashMap<>(getCapacity(kbEntities.size()), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_IDS_ENTITY_KNOWLEDGEBASE;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityIdsEKBCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      KeyValueStoreRow keyHolder = new KeyValueStoreRow(new Object[2]);
      List<byte[]> encodedKeys = new ArrayList<>();
      for (KBIdentifiedEntity kbEntity : kbEntities) {
        keyHolder.values[0] = kbEntity.getIdentifier();
        keyHolder.values[1] = kbEntity.getKnowledgebase();
        encodedKeys.add(entityIdsEKBCodec.encodeKey(keyHolder));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      if (keyValueMap.size() != kbEntities.size()) {
        for (KBIdentifiedEntity kbEntity : kbEntities) {
          entities.put(kbEntity, 0);
        }
      }

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        KeyValueStoreRow row = (KeyValueStoreRow) entityIdsEKBCodec.decodeKey(entry.getKey());
        int id = (int) entityIdsEKBCodec.decodeValue(entry.getValue());
        entities.put(KBIdentifiedEntity.getKBIdentifiedEntity(row.getString(0), row.getString(1)), id);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entities;
  }

  public TIntObjectHashMap<EntityMetaData> getEntitiesMetaData(int[] entitiesIds) throws EntityLinkingDataAccessException {
    if (entitiesIds == null || entitiesIds.length == 0) {
      return new TIntObjectHashMap<>();
    }

    TIntObjectHashMap<EntityMetaData> entitiesMetaData = new TIntObjectHashMap<>(getCapacity(entitiesIds.length), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_METADATA_ENTITY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityMetadataCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : entitiesIds) {
        encodedKeys.add(entityMetadataCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int entity = (int) entityMetadataCodec.decodeKey(entry.getKey());
        //Retrieves more than one value we get the first one
        KeyValueStoreRow[] rows = (KeyValueStoreRow[]) entityMetadataCodec.decodeValue(entry.getValue());
        entitiesMetaData.put(entity,
            new EntityMetaData(entity, rows[0].getString(0), rows[0].getString(1), rows[0].getString(2), rows[0].getString(3), rows[0].getString(4),
                rows[0].getString(5), rows[0].getString(6)));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entitiesMetaData;
  }

  public TIntObjectHashMap<int[]> getTypesIdsForEntitiesIds(int[] entitiesIds) throws EntityLinkingDataAccessException {
    if (entitiesIds.length == 0) {
      return new TIntObjectHashMap<>();
    }

    TIntObjectHashMap<int[]> typesIds = new TIntObjectHashMap<>(getCapacity(entitiesIds.length), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_TYPES_ENTITY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityTypesCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : entitiesIds) {
        encodedKeys.add(entityTypesCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      if (keyValueMap.size() != entitiesIds.length) {
        for (int entitiesId : entitiesIds) {
          typesIds.put(entitiesId, new int[0]);
        }
      }

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int entity = (int) entityTypesCodec.decodeKey(entry.getKey());
        int[] curTypesIds = (int[]) entityTypesCodec.decodeValue(entry.getValue());
        typesIds.put(entity, curTypesIds);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return typesIds;
  }

  public TIntObjectHashMap<int[]> getCategoryIdsForEntitiesIds(int[] entitiesIds) throws EntityLinkingDataAccessException {
    if (entitiesIds.length == 0) {
      return new TIntObjectHashMap<>();
    }

    TIntObjectHashMap<int[]> typesIds = new TIntObjectHashMap<>(getCapacity(entitiesIds.length), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.CONCEPT_CATEGORIES;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityTypesCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : entitiesIds) {
        encodedKeys.add(entityTypesCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      if (keyValueMap.size() != entitiesIds.length) {
        for (int entitiesId : entitiesIds) {
          typesIds.put(entitiesId, new int[0]);
        }
      }

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int entity = (int) entityTypesCodec.decodeKey(entry.getKey());
        int[] curTypesIds = (int[]) entityTypesCodec.decodeValue(entry.getValue());
        typesIds.put(entity, curTypesIds);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return typesIds;
  }


  public TIntObjectHashMap<Type> getTypesForIds(int[] ids) throws EntityLinkingDataAccessException {
    if (ids.length == 0) return new TIntObjectHashMap<>();

    TIntObjectHashMap<Type> typeNames = new TIntObjectHashMap<>(getCapacity(ids.length), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.TYPE_IDS_ID;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityIdsCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : ids) {
        encodedKeys.add(entityIdsCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int type = (int) entityIdsCodec.decodeKey(entry.getKey());
        //Multiple values retrieve get the first one
        KeyValueStoreRow[] rows = (KeyValueStoreRow[]) entityIdsCodec.decodeValue(entry.getValue());
        typeNames.put(type, new Type(rows[0].getString(1), rows[0].getString(0)));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return typeNames;
  }

  @Override
  public TIntObjectHashMap<Type> getAllWikiCategoryTypes() throws EntityLinkingDataAccessException {
    TIntObjectHashMap<Type> typeNames = new TIntObjectHashMap<>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.CATEGORY_IDS_ID;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec categoryIdsCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      KeyValueStore.EntryIterator iterator = keyValueStore.entryIterator();
      int read = 0;
      int categoryId,name;
      while (iterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = iterator.next();
        categoryId = (int) categoryIdsCodec.decodeKey(entry.getKey());
        //Multiple values retrieve get the first one
        KeyValueStoreRow[] rows = (KeyValueStoreRow[]) categoryIdsCodec.decodeValue(entry.getValue());
        typeNames.put(categoryId, new Type(rows[0].getString(1), rows[0].getString(0)));

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " wikipedia categories.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return typeNames;
  }

  @Override //TODO: language not used
  public Map<String, int[]> getCategoryIdsForMentions(Set<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    Map<String, Entities> entities = getEntitiesForMentions(mentions, 1.0, 0, isNamedEntity);

    Map<String, Set<Integer>> mentionCategories = new HashMap<>(entities.size());

    for (Entry<String, Entities> entry:entities.entrySet()) {
      Set<Integer> types = mentionCategories.get(entry.getKey());
      if (types == null) {
        types = new HashSet<>();
      }
      List<Integer> entityIds = new ArrayList<>();
      for (Entity entity:entry.getValue()) {
        entityIds.add(entity.getId());
      }
      TIntObjectHashMap<int[]> categories = getCategoryIdsForEntitiesIds(ArrayUtils.toPrimitive(entityIds.toArray(new Integer[0])));
      TIntObjectIterator<int[]> it = categories.iterator();
      while (it.hasNext()) {
        types.addAll(Arrays.asList(ArrayUtils.toObject(it.value())));
      }
    }

    Map<String, int[]> ret = new HashMap<>();
    for (String key:mentionCategories.keySet()) {
      Integer[] temp = mentionCategories.get(key).toArray(new Integer[0]);
      ret.put(key, ArrayUtils.toPrimitive(temp));
    }

    return ret;
  }

  public TIntObjectHashMap<int[]> getTaxonomy() throws EntityLinkingDataAccessException  {
    TIntObjectHashMap<int[]> taxonomy = new TIntObjectHashMap<>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.TYPE_TAXONOMY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      KeyValueStore.EntryIterator iterator = keyValueStore.entryIterator();
      int read = 0;
      while (iterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = iterator.next();
        int type = (int) codec.decodeKey(entry.getKey());
        int[] parents = (int[]) codec.decodeValue(entry.getValue());
        taxonomy.put(type, parents);

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " type-taxonomy.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return taxonomy;
  }

  public TIntDoubleHashMap getEntitiesImportances(int[] entitiesIds) throws EntityLinkingDataAccessException {
    if (entitiesIds == null || entitiesIds.length == 0) {
      return new TIntDoubleHashMap();
    }

    TIntDoubleHashMap entitiesImportances = new TIntDoubleHashMap(getCapacity(entitiesIds.length), troveLoadFactor);
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.ENTITY_RANK_ENTITY;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec entityRankCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);

      List<byte[]> encodedKeys = new ArrayList<>();
      for (int id : entitiesIds) {
        encodedKeys.add(entityRankCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        //More than one value, not sure why?
        int entity = (int) entityRankCodec.decodeKey(entry.getKey());
        double[] rank = (double[]) entityRankCodec.decodeValue(entry.getValue());
        entitiesImportances.put(entity, rank[0]);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entitiesImportances;
  }

  public int[] getAllWordExpansions() throws EntityLinkingDataAccessException {
    TIntIntHashMap wordExpansions = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading word expansions.");
      DatabaseKeyValueStore db = DatabaseKeyValueStore.WORD_EXPANSION_WORD;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      Codec wordExpansionsCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore.EntryIterator iterator = keyValueStore.entryIterator();
      int read = 0;
      int word, expansion;
      while (iterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = iterator.next();
        word = (int) wordExpansionsCodec.decodeKey(entry.getKey());
        expansion = (int) wordExpansionsCodec.decodeValue(entry.getValue());
        wordExpansions.put(word, expansion);
        if (word > maxId) {
          maxId = word;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " word expansions.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    // Transform hash to int array.
    int[] expansions = new int[maxId + 1];
    for (TIntIntIterator itr = wordExpansions.iterator(); itr.hasNext(); ) {
      itr.advance();
      assert itr.key() < expansions.length && itr.key() > 0;  // Ids start at 1.
      expansions[itr.key()] = itr.value();
    }
    return expansions;
  }

  public int[] getAllWordContractions() throws EntityLinkingDataAccessException {
    TIntIntHashMap wordContraction = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading word contractions.");
      DatabaseKeyValueStore db = DatabaseKeyValueStore.WORD_EXPANSION_WORD;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      Codec wordExpansionsCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore.EntryIterator iterator = keyValueStore.entryIterator();
      int read = 0;
      int word, expansion;
      while (iterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = iterator.next();
        word = (int) wordExpansionsCodec.decodeKey(entry.getKey());
        expansion = (int) wordExpansionsCodec.decodeValue(entry.getValue());
        wordContraction.put(expansion, word);
        if (expansion > maxId) {
          maxId = expansion;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " word contractions.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    // Transform hash to int array.
    int[] contractions = new int[maxId + 1];
    for (TIntIntIterator itr = wordContraction.iterator(); itr.hasNext(); ) {
      itr.advance();
      assert itr.key() < contractions.length && itr.key() > 0;  // Ids start at 1.
      contractions[itr.key()] = itr.value();
    }
    return contractions;
  }

  public int[] getAllUnitDocumentFrequencies(UnitType unitType) throws EntityLinkingDataAccessException {
    TIntIntHashMap unitCounts = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading " + unitType.getUnitName() + " counts.");
      DatabaseKeyValueStore db = unitType.getUnitCountsKeyValueStore();
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      Codec unitCountCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore.EntryIterator iterator = keyValueStore.entryIterator();
      int read = 0;
      int unit, count;
      while (iterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = iterator.next();
        unit = (int) unitCountCodec.decodeKey(entry.getKey());
        count = (int) unitCountCodec.decodeValue(entry.getValue());
        unitCounts.put(unit, count);
        if (unit > maxId) {
          maxId = unit;
        }
        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " " + unitType.getUnitName() + " counts.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    if (maxId == -1) return null;

    // Transform hash to int array. This will contain a lot of zeroes as 
    // the keyphrase ids are not part of this (but need to be considered).
    int[] counts = new int[maxId + 1];
    for (TIntIntIterator itr = unitCounts.iterator(); itr.hasNext(); ) {
      itr.advance();
      int unitId = itr.key();
      // assert unitId < counts.length && unitId > 0 : "Failed for " + unitId;  // Ids start at 1.
      // actually, units should not contain a 0 id, but they do.
      assert unitId < counts.length : "Failed for " + unitId;  // Ids start at 1.
      counts[unitId] = itr.value();
    }
    return counts;
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntityUnitIntersectionCount(Entities entities, UnitType unitType)
      throws EntityLinkingDataAccessException {
    TIntObjectHashMap<TIntIntHashMap> entityKeywordIC = new TIntObjectHashMap<TIntIntHashMap>();

    if (entities == null || entities.size() == 0) {
      return entityKeywordIC;
    }

    try {
      DatabaseKeyValueStore db = unitType.getEntityUnitCooccurrenceKeyValueStore();
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      Codec unitCountCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      List<byte[]> encodedKeys = new ArrayList<>();
      for (Entity entity : entities) {
        encodedKeys.add(unitCountCodec.encodeKey(entity.getId()));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);

      if (keyValueMap.size() != entities.size()) {
        for (Entity entity : entities) {
          entityKeywordIC.put(entity.getId(), new TIntIntHashMap());
        }
      }

      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int entity = (int) unitCountCodec.decodeKey(entry.getKey());
        int[] valuePairs = (int[]) unitCountCodec.decodeValue(entry.getValue());
        TIntIntHashMap curEntityUnitCounts = new TIntIntHashMap(getCapacity(valuePairs.length), troveLoadFactor);
        for (int i = 0; i < valuePairs.length; ) {
          curEntityUnitCounts.put(valuePairs[i], valuePairs[i + 1]);
          i += 2;
        }
        entityKeywordIC.put(entity, curEntityUnitCounts);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    return entityKeywordIC;
  }

  public Map<String, int[]> getDictionary() throws EntityLinkingDataAccessException {
    Map<String, int[]> candidates = new HashMap<>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.DICTIONARY_MENTION;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec dictionaryMentionCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      KeyValueStore.EntryIterator entryIterator = keyValueStore.entryIterator();
      int read = 0;
      while (entryIterator.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = entryIterator.next();
        String mention = (String) dictionaryMentionCodec.decodeKey(entry.getValue());
        int[] entities = (int[]) dictionaryMentionCodec.decodeValue(entry.getValue());
        candidates.put(mention, entities);
        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " dictionary entries.");
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return candidates;
  }

  @Override public Set<String> getMentionsforLanguage(Language language, Boolean isNamedEntity, int limit) throws EntityLinkingDataAccessException {
    Set<String> result = new HashSet<>(10000000);
    try {
      DatabaseKeyValueStore db;
      if (isNamedEntity) {
        db = DatabaseKeyValueStore.DICTIONARY_LANGUAGE_NE;
      }
      else {
        db = DatabaseKeyValueStore.DICTIONARY_LANGUAGE_C;
      }
      Codec dictionaryMentionCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      if(db.getPartitions() > 1) {
        for(int i = 1; i < db.getPartitions(); i++) {
          byte[] key = dictionaryMentionCodec.encodeKey(language.getID()+"-"+Integer.toString(i));
          byte[] value = keyValueStore.get(key);
          result.addAll(Arrays.asList((String[]) dictionaryMentionCodec.decodeValue(value)));
        }
      } else {
        byte[] key = dictionaryMentionCodec.encodeKey(language.getID());
        byte[] value = keyValueStore.get(key);
        result.addAll(Arrays.asList((String[]) dictionaryMentionCodec.decodeValue(value)));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    if (limit != 0) {
      return new HashSet<String>(Arrays.asList((String[]) ArrayUtils.subarray(result.toArray(), 0, limit-1)));
    }
    return result;
  }

  // This is used in Domain Filtering to translate English mentions to other languages, but not really translate them.
  // It gets all entities for English mentions, then get all mentions for the entities in the language.
  public List<String> getMentionsInLanguageForEnglishMentions(Collection<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    // Here the language must be English, but it is not considered. (later we can have a query of getting Entities for Mention in a specific Language
    Map<String, Entities> entities = getEntitiesForMentions(mentions, 1.0, 0, isNamedEntity);
    Entities allEntities = new Entities();
    for (Map.Entry<String, Entities> entry:entities.entrySet()) {
      allEntities.addAll(entry.getValue());
    }
    TIntObjectHashMap<List<MentionObject>> entityMentions = getMentionsForEntities(allEntities);
    Set<String> allMentions = new HashSet<>();
    TIntObjectIterator<List<MentionObject>> it = entityMentions.iterator();
    while (it.hasNext()) {
      for (MentionObject mo:it.value()) {
        allMentions.add(mo.getMention());
      }
    }

    Set<String> languageMentions = new HashSet<>(10000000);
    try {
      DatabaseKeyValueStore db;
      if (isNamedEntity) {
        db = DatabaseKeyValueStore.DICTIONARY_LANGUAGE_NE;
      }
      else {
        db = DatabaseKeyValueStore.DICTIONARY_LANGUAGE_C;
      }
      Codec dictionaryMentionCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      if(db.getPartitions() > 1) {
        for(int i = 1; i < db.getPartitions(); i++) {
          byte[] key = dictionaryMentionCodec.encodeKey(language.getID()+"-"+Integer.toString(i));
          byte[] value = keyValueStore.get(key);
          languageMentions.addAll(Arrays.asList((String[]) dictionaryMentionCodec.decodeValue(value)));
        }
      } else {
        byte[] key = dictionaryMentionCodec.encodeKey(language.getID());
        byte[] value = keyValueStore.get(key);
        languageMentions.addAll(Arrays.asList((String[]) dictionaryMentionCodec.decodeValue(value)));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }

    languageMentions.retainAll(allMentions);
    return new ArrayList<>(languageMentions);
  }

  @Override public Map<String, Integer> getInternalIdsfromWikidataIds(List<String> ids) throws EntityLinkingDataAccessException {
    Map<String, Integer> result = new HashMap<>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.WIKIDATA_ID;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec wikidataIdCodec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      List<byte[]> encodedKeys = new ArrayList<>();
      for (String id : ids) {
        encodedKeys.add(wikidataIdCodec.encodeKey(id));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);
      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        String id = (String) wikidataIdCodec.decodeKey(entry.getKey());
        int internalId = (int) wikidataIdCodec.decodeValue(entry.getValue());
        result.put(id, internalId);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return result;
  }

  public TObjectIntHashMap<String> getAllWordIds() throws EntityLinkingDataAccessException {
    TObjectIntHashMap<String> wordIds;
    try {
      wordIds = new TObjectIntHashMap<>();
      DatabaseKeyValueStore db = DatabaseKeyValueStore.WORD_IDS_WORD;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      KeyValueStore.EntryIterator it = keyValueStore.entryIterator();
      while (it.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = it.next();
        String key = (String) codec.decodeKey(entry.getKey());
        Integer value = (Integer) codec.decodeValue(entry.getValue());
        wordIds.put(key, value);
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return wordIds;
  }

  @Override
  public TIntObjectHashMap<EntityType> getEntityClasses(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<EntityType> result = new TIntObjectHashMap<>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.DICTIONARY_ENTITIES;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      List<byte[]> encodedKeys = new ArrayList<>();
      for (Entity e : entities) {
        encodedKeys.add(codec.encodeKey(e.getId()));
      }
      Map<byte[], byte[]> keyValueMap = keyValueStore.getAll(encodedKeys);
      for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) continue;
        int id = (int) codec.decodeKey(entry.getKey());
        int classId = (int) codec.decodeValue(entry.getValue());
        result.put(id, EntityType.getNameforDBId(classId));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return result;
  }

  @Override
  public TIntObjectHashMap<EntityType> getAllEntityClasses() throws EntityLinkingDataAccessException {
    TIntObjectHashMap<EntityType> entityClasses = new TIntObjectHashMap<EntityType>();
    try {
      DatabaseKeyValueStore db = DatabaseKeyValueStore.DICTIONARY_ENTITIES;
      if(db.getPartitions() != 1) {
        throw new IllegalArgumentException("Multiple partitions not supported for this key-value store");
      }
      Codec codec = DataAccessKeyValueStoreHandler.singleton().getCodec(db);
      KeyValueStore<byte[], byte[]> keyValueStore = DataAccessKeyValueStoreHandler.singleton().getKeyValueStore(db);
      KeyValueStore.EntryIterator it = keyValueStore.entryIterator();
      while (it.hasNext()) {
        KeyValueStore.Entry<byte[], byte[]> entry = it.next();
        Integer key = (Integer) codec.decodeKey(entry.getKey());
        Integer value = (Integer) codec.decodeValue(entry.getValue());
        entityClasses.put(key, EntityType.getNameforDBId(value));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return entityClasses;
  }

  @Override public Map<String, int[]> getEntityMentionsforLanguageAndEntities(int[] ids, Language language)
      throws EntityLinkingDataAccessException {
    throw new NotImplementedException("getAllKeyphraseSources  is not implemented in DataAccessKeyValueStore.");
  }

  @Override public Entities getEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException {
    throw new NotImplementedException("getAllKeyphraseTokens  is not implemented in DataAccessKeyValueStore.");
  }

  @Override
  public Set<String> getEntityURL(String namedEntity) throws EntityLinkingDataAccessException {
    return null;
  }

	@Override
	public LinkedHashMap<Integer, String> getTopEntitiesByRank(int start, int entityNumber, String language) throws EntityLinkingDataAccessException {
		return null;
	}

  public static int getCapacity(int numElements) {
    return (int) (numElements / troveLoadFactor);
  }

  // =========================
  // === DEPRECATED METHODS
  // =

  public TIntObjectHashMap<int[]> getAllKeyphraseTokens() {
    throw new NotImplementedException("getAllKeyphraseTokens  is not implemented in DataAccessKeyValueStore.");
  }

  public TObjectIntHashMap<String> getAllKeyphraseSources() {
    throw new NotImplementedException("getAllKeyphraseSources  is not implemented in DataAccessKeyValueStore.");
  }

  public Keyphrases getEntityKeyphrases(Entities entities, Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight,
      int maxEntityKeyphraseCount) {
    throw new NotImplementedException("getEntityKeyphrases  is not implemented in DataAccessKeyValueStore.");
  }

  public Entities getEntitiesForMentionByFuzzyMatching(String mention, double minSimilarity, boolean isNamedentity) {
    throw new NotImplementedException("getEntitiesForMentionByFuzzyMatching()  is not implemented in DataAccessKeyValueStore.");
  }

  public void getEntityKeyphraseTokens(Entities entities, TIntObjectHashMap<int[]> entityKeyphrases, TIntObjectHashMap<int[]> keyphraseTokens) {
    throw new NotImplementedException("getEntityKeyphraseTokens() is not implemented in DataAccessKeyValueStore.");
  }

  public TIntIntHashMap getEntitySuperdocSize(Entities entities) {
    throw new NotImplementedException("getEntitySuperdocSize()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) {
    throw new NotImplementedException("getEntityKeywordIntersectionCount()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntIntHashMap getKeyphraseDocumentFrequencies(TIntHashSet keyphrases) {
    throw new NotImplementedException("getKeyphraseDocumentFrequencies()  is not implemented in DataAccessKeyValueStore.");
  }

  public List<String> getParentTypes(String queryType) {
    throw new NotImplementedException("getParentTypes()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<int[]> getEntityLSHSignatures(Entities entities) {
    throw new NotImplementedException("getEntityLSHSignatures()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<int[]> getEntityLSHSignatures(Entities entities, String table) {
    throw new NotImplementedException("getEntityLSHSignatures()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<String> getWordsForIds(int[] ids) {
    throw new NotImplementedException("getWordsForIds()  is not implemented in DataAccessKeyValueStore.");
  }

  public TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds() {
    throw new NotImplementedException("getAllEntityIds()  is not implemented in DataAccessKeyValueStore.");
  }

  public TObjectIntHashMap<Type> getAllTypeIds() {
    throw new NotImplementedException("getAllTypeIds()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntDoubleHashMap getAllEntityRanks() {
    throw new NotImplementedException("getAllEntityRanks()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<int[]> getAllEntityTypes() {
    throw new NotImplementedException("getAllEntityTypes()  is not implemented in DataAccessKeyValueStore.");
  }

  public Entities getAllEntities() {
    throw new NotImplementedException("getAllEntities()  is not implemented in DataAccessKeyValueStore.");
  }

  public int getWordExpansion(int wordId) {
    throw new NotImplementedException("getWordExpansion()  is not implemented in DataAccessKeyValueStore.");
  }

  public boolean isYagoEntity(Entity entity) {
    throw new NotImplementedException("isYagoEntity()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<int[]> getAllInlinks() {
    throw new NotImplementedException("getAllInlinks()  is not implemented in DataAccessKeyValueStore.");
  }

  public int getMaximumEntityId() {
    throw new NotImplementedException("getMaximumEntityId()  is not implemented in DataAccessKeyValueStore.");
  }

  public int getMaximumWordId() {
    throw new NotImplementedException("getMaximumWordId()  is not implemented in DataAccessKeyValueStore.");
  }

  public TObjectIntHashMap<String> getIdsForTypeNames(Collection<String> typeNames) {
    throw new NotImplementedException("getIdsForTypeNames()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntObjectHashMap<int[]> getEntitiesIdsForTypesIds(int[] typesIds) {
    throw new NotImplementedException("getEntitiesIdsForTypesIds()  is not implemented in DataAccessKeyValueStore.");
  }

  public Map<String, List<String>> getAllEntitiesMetaData(String startingWith) {
    throw new NotImplementedException("getAllEntitiesMetaData()  is not implemented in DataAccessKeyValueStore.");
  }

  public Map<String, Double> getKeyphraseSourceWeights() {
    throw new NotImplementedException("getKeyphraseSourceWeights()  is not implemented in DataAccessKeyValueStore.");
  }

  public int[] getAllKeywordDocumentFrequencies() {
    throw new NotImplementedException("getAllKeywordDocumentFrequencies()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntIntHashMap getGNDTripleCount(Entities entities) {
    throw new NotImplementedException("getGNDTripleCount()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntIntHashMap getGNDTitleCount(Entities entities) {
    throw new NotImplementedException("getGNDTitleCount()  is not implemented in DataAccessKeyValueStore.");
  }

  public TIntIntHashMap getYagoOutlinkCount(Entities entities) {
    throw new NotImplementedException("getYagoOutlinkCount()  is not implemented in DataAccessKeyValueStore.");
  }

  public Pair<Integer, Integer> getImportanceComponentMinMax(String importanceComponentId) {
    throw new NotImplementedException("getImportanceComponentMinMax()  is not implemented in DataAccessKeyValueStore.");
  }

  @Override
  public double getGlobalEntityPrior(Entity entity) throws EntityLinkingDataAccessException {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, String> getWikidataIdsForEntities(Set<String> entities) {
    // Auto-generated method stub
    return null;
  }

  @Override
  public Entities getAllEntities(boolean isNE) throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }

  @Override
  public TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds(boolean isNamedEntity) throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }

  @Override
  public TIntObjectHashMap<String> getWordsForIdsLowerCase(int[] wordIds) throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }

  @Override
  public List<Integer> getWordsForExpansion(int expansionId) throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }

  @Override
  public TIntObjectHashMap<TIntArrayList> getWordsForExpansions(int[] expansionIds) throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllWikidataIds() throws EntityLinkingDataAccessException {
    // Auto-generated method stub
    return null;
  }


}
