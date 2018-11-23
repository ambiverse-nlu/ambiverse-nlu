package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.kgmapping.KGMapping;

import java.io.Serializable;

public class KBIdentifiedEntity implements Serializable, Comparable<KBIdentifiedEntity> {

  /** original knowledgebase identifier */
  private String identifier;

  /** knowledgbase from which entity is coming  */
  private String knowledgebase;

  public KBIdentifiedEntity() {
    // bean.
  }

  public static KBIdentifiedEntity getKBIdentifiedEntity(String identifier, String knowledgebase) {
    String kbid = KGMapping.getMapping(knowledgebase, identifier);
    if(kbid == null) {
      return new KBIdentifiedEntity(identifier, knowledgebase);
    }
    String kb = kbid.substring(0,kbid.indexOf(':'));
    String id = kbid.substring(kbid.indexOf(':')+1);
    return new KBIdentifiedEntity(id, kb);
  }

  private KBIdentifiedEntity(String identifier, String knowledgebase) {
    super();
    this.identifier = identifier;
    this.knowledgebase = knowledgebase;
  }

  public KBIdentifiedEntity(String knowledgebaseIdentifier) {
    setDictionaryKey(knowledgebaseIdentifier);
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getKnowledgebase() {
    return knowledgebase;
  }

  public void setKnowledgebase(String knowledgebase) {
    this.knowledgebase = knowledgebase;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KBIdentifiedEntity that = (KBIdentifiedEntity) o;

    if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
    return knowledgebase != null ? knowledgebase.equals(that.knowledgebase) : that.knowledgebase == null;

  }

  @Override public int hashCode() {
    int result = identifier != null ? identifier.hashCode() : 0;
    result = 31 * result + (knowledgebase != null ? knowledgebase.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "KBIdentifiedEntity{" + "identifier='" + identifier + '\'' + ", knowledgebase='" + knowledgebase + '\'' + '}';
  }

  public void setDictionaryKey(String kbEntity) {
    int splitIndex = kbEntity.indexOf(':');
    if (splitIndex == -1) {
      throw new IllegalArgumentException(
          "':' separator is missing. knowledgebaseIdentifier needs to be of format " + "'KBIdentifier:EntityIdentifier'");
    }

    if (splitIndex == kbEntity.length() - 1) {
      throw new IllegalArgumentException(
          "':' separator is the last char. knowledgebaseIdentifier needs to be of format " + "'KBIdentifier:EntityIdentifier'");
    }

    this.knowledgebase = kbEntity.substring(0, splitIndex);
    this.identifier = kbEntity.substring(splitIndex + 1, kbEntity.length());
  }

  public String getDictionaryKey() {
    return knowledgebase + ":" + identifier;
  }

  public int compareTo(KBIdentifiedEntity kbIdentifiedEntity) {
    return getDictionaryKey().compareTo(kbIdentifiedEntity.getDictionaryKey());
  }
}
