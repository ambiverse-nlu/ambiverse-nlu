package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;

import java.util.*;
import java.util.Map.Entry;

/**
 * Constructed from a mention to entity-collection map, this allows 
 * easy lookups by Mention objects.
 */
public class CandidateDictionary {

  private Map<String, Entities> mentionEntityDictionary_ = new HashMap<>();

  private Set<Entity> allEntities_ = new HashSet<>();

  private Map<KBIdentifiedEntity, Integer> kbIdentifier2id_ = new HashMap<>();

  boolean isNamedEntity;

  public CandidateDictionary(boolean isNamedEntity) {
    this.isNamedEntity = isNamedEntity;
  }

  /**
   * The format of the keys is UTF-8 strings used as lookup keys.
   * The collection must be in the form of KBIdentifiedEntity strings, e.g.
   * "YAGO:Jimmy_Page".
   *
   * The keys are looked up in the main repository - this enabled the addition of new
   * dictionary relations.
   *
   * @param mentionEntityDictionary Mention-Entity pairs for additional lookup.
   */
  public CandidateDictionary(
      Map<String, List<KBIdentifiedEntity>> mentionEntityDictionary, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    // Get starting id for non-conflicting entity ids - external context is assumed to be state-less, so they
    // can conflict with each other, but not with the entity repository.
    int maxEntityId = DataAccess.getMaximumEntityId();
    this.isNamedEntity = isNamedEntity;

    // Gather all entities that really exist
    Set<KBIdentifiedEntity> allAddedEntities = new HashSet<>();
    mentionEntityDictionary.values().stream().forEach(cands -> allAddedEntities.addAll(cands));
    Entities entities = EntityLinkingManager.getEntities(allAddedEntities);
    Map<KBIdentifiedEntity, Entity> existingEntities = new HashMap<>();
    entities.getEntities().stream().forEach(e -> existingEntities.put(e.getKbIdentifiedEntity(), e));

    allEntities_ = new HashSet<>();
    kbIdentifier2id_ = new HashMap<>();

    mentionEntityDictionary_ = new HashMap<>();
    for (Entry<String, List<KBIdentifiedEntity>> e : mentionEntityDictionary.entrySet()) {
      Entities candidates = new Entities();
      for (KBIdentifiedEntity kbId : e.getValue()) {
        Entity entityToAdd;
        if (existingEntities.containsKey(kbId)) {
          // Add real entity if it is part of the entity repository.
          entityToAdd = existingEntities.get(kbId);
        } else {
          // Otherwise create transient id.
          Integer internalId = kbIdentifier2id_.get(kbId);
          if (internalId == null) {
            internalId = ++maxEntityId;
            kbIdentifier2id_.put(kbId, internalId);
          }
          entityToAdd = new Entity(kbId, internalId);
        }
        candidates.add(entityToAdd);
        allEntities_.add(entityToAdd);
      }
      mentionEntityDictionary_.put(EntityLinkingManager.conflateToken(e.getKey(), isNamedEntity), candidates);
    }
  }

  /**
   * Return the candidate entities for the given mention.
   *
   * @param m Mention to perform lookup for.
   * @return Candidate entities.
   */
  public Entities getEntities(Mention m, boolean isNamedEntity) {
    Entities entities = new Entities();
    for (String mention : m.getNormalizedMention()) {
      String normalizedMention = EntityLinkingManager.conflateToken(mention, isNamedEntity);
      Entities candidates = mentionEntityDictionary_.get(normalizedMention);
      if (candidates != null) {
        entities.addAll(candidates);
      }
    }
    return entities;
  }

  /**
   * Checks if the dictionary contains the entity as candidate for any mention.
   * @param entity
   * @return
   */
  public boolean contains(Entity entity) {
    return allEntities_.contains(entity);
  }

  /**
   * @return All external entities.
   */
  public Set<Entity> getAllEntities() {
    return allEntities_;
  }

  /**
   * Returns the transient, internal entity ID for the kbIdentifier.
   * @param kbIdentifier
   * @return Transient entity ID.
   */
  public int getEntityId(KBIdentifiedEntity kbIdentifier) {
    return kbIdentifier2id_.get(kbIdentifier);
  }
}
