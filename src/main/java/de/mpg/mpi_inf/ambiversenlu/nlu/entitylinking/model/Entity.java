package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char;

import java.io.Serializable;
import java.util.List;

public class Entity implements Serializable, Comparable<Entity>, Cloneable {

  private static final long serialVersionUID = 131444964369556633L;

  private KBIdentifiedEntity kbEntity;

  private List<String> surroundingMentionNames;

  private int internalId = 0;
  
  public static final String OOKBE = "--OOKBE--";

  public static final String EXISTS = "--EXISTS--";

  /**
   * Use this field to represent the mention-entity similarity computed with 
   * some method (not the score stored in the DB). This field will not be set 
   * in the constructor. We set it later on, when we compute the similarity.
   */
  private double mentionEntitySimilarity;

  public Entity(String entityId, String knowledgebase, int internalId) {
    this.kbEntity = KBIdentifiedEntity.getKBIdentifiedEntity(entityId, knowledgebase);
    this.mentionEntitySimilarity = -1.0;
    this.internalId = internalId;
  }

  public Entity(KBIdentifiedEntity entity, int internalId) {
    this(entity.getIdentifier(), entity.getKnowledgebase(), internalId);
  }

  public String toString() {
    return kbEntity + " (" + internalId + ")";
  }

  public String tohtmlString() {
    return "<td></td><td></td><td>" + Char.toHTML(kbEntity.toString()) + "</td><td></td><td></td><td></td>";
  }

  public int getId() {
    return internalId;
  }

  public double getMentionEntitySimilarity() {
    return this.mentionEntitySimilarity;
  }

  public void setMentionEntitySimilarity(double mes) {
    this.mentionEntitySimilarity = mes;
  }

  public int compareTo(Entity e) {
    return getKbIdentifiedEntity().compareTo(e.getKbIdentifiedEntity());
  }

  public boolean equals(Object o) {
    if (o instanceof Entity) {
      Entity e = (Entity) o;
      return kbEntity.equals(e.getKbIdentifiedEntity());
    } else {
      return false;
    }
  }

  public int hashCode() {
    return internalId;
  }

  public boolean isOOKBentity() {
    return Entities.isOokbeName(kbEntity.getIdentifier());
  }

  /**
   * @return The normalized name from an out-of-knowledge base entity, i.e.
   *         the name in it's original form.
   */
  public String getNMEnormalizedName() {
    String normName = kbEntity.getIdentifier().replace("-" + OOKBE, "").replace(' ', '_');
    return normName;
  }

  public List<String> getSurroundingMentionNames() {
    return surroundingMentionNames;
  }

  public void setSurroundingMentionNames(List<String> surroundingMentionNames) {
    this.surroundingMentionNames = surroundingMentionNames;
  }

  public String getKnowledgebase() {
    return kbEntity.getKnowledgebase();
  }

  public String getIdentifierInKb() {
    return kbEntity.getIdentifier();
  }

  public KBIdentifiedEntity getKbIdentifiedEntity() {
    return kbEntity;
  }

}
