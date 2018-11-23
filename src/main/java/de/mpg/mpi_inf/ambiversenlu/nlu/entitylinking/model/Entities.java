package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

public class Entities implements Serializable, Iterable<Entity> {

  private static final long serialVersionUID = -5405018666688695438L;

  private boolean includesOokbeEntities;

  private TIntObjectHashMap<Entity> id2entity;

  private Set<Entity> entities = null;

  public Entities() {
    id2entity = new TIntObjectHashMap<Entity>(16, 0.75f, -1);
    entities = new HashSet<Entity>();
  }

  public Entities(Set<Entity> entities) {
    this.entities = entities;
    id2entity = new TIntObjectHashMap<Entity>(entities.size(), 1.0f, -1);
    for (Entity entity : entities) {
      id2entity.put(entity.getId(), entity);
    }
  }

  public Entities(Entities otherEntities) {
    this(otherEntities.getEntities());
    this.includesOokbeEntities = otherEntities.isIncludesOokbeEntities();
  }

  public Collection<Integer> getUniqueIds() {
    return Arrays.asList(ArrayUtils.toObject(id2entity.keys()));
  }

  public Collection<String> getUniqueNames() {
    Set<String> names = new HashSet<String>();
    for (Entity e : entities) {
      names.add(e.getIdentifierInKb());
    }
    return names;
  }

  public int[] getUniqueIdsAsArray() {
    return id2entity.keys();
  }

  public Set<Entity> getEntities() {
    return entities;
  }

  /**
   * Should only be used for testing or if you know the exact id for each entity
   * @param entity
   */
  public void add(Entity entity) {
    entities.add(entity);
    id2entity.put(entity.getId(), entity);
  }

  public void addAll(Entities entities) {
    this.entities.addAll(entities.entities);
    for (Entity e : entities) {
      id2entity.put(e.getId(), e);
    }
  }

  public void remove(Entity entity) {
    entities.remove(entity);
    id2entity.remove(entity.getId());
  }

  /**
   * Adds all entities to the collection. Make sure not to add duplicates!
   *
   * @param entities
   */
  public void addAll(Collection<Entity> entities) {
    this.entities.addAll(entities);
    for (Entity e : entities) {
      id2entity.put(e.getId(), e);
    }
  }

  public int size() {
    return entities.size();
  }

  public boolean contains(int id) {
    return id2entity.containsKey(id);
  }

  @Override public Iterator<Entity> iterator() {
    return entities.iterator();
  }

  public boolean isEmpty() {
    return entities.isEmpty();
  }

  public boolean isIncludesOokbeEntities() {
    return includesOokbeEntities;
  }

  public void setIncludesOokbeEntities(boolean includesOokbeEntities) {
    this.includesOokbeEntities = includesOokbeEntities;
  }

  public static boolean isOokbeName(String name) {
    return (
        name.contains("-NME-") || 
        name.contains("-OOKBE-")); 
  }

  public static boolean isOokbEntity(String entity) {
    return (
        entity.equals(Entity.OOKBE) || 
        entity.equals("--NME--")); 
  }

  public static boolean isOokbEntity(KBIdentifiedEntity entity) {
    return isOokbEntity(entity.getIdentifier());
  }

  public static String getNameForOokbe(String nmeName) {
    String name = nmeName.replace("-" + Entity.OOKBE, "");
    return name;
  }

  public Entity getEntityById(int id) {
    return id2entity.get(id);
  }

  public String toString() {
    return entities.size() + " entities: " + StringUtils.join(entities, ", ");
  }
}
