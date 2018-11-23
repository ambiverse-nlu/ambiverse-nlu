package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import java.io.Serializable;

public class Type implements Serializable {

  private String knowledgeBase;

  private String name;

  public Type(String knowledgeBase, String name) {
    super();
    this.knowledgeBase = knowledgeBase;
    this.name = name;
  }

  public String getKnowledgeBase() {
    return knowledgeBase;
  }

  public void setKnowledgeBase(String knowledgeBase) {
    this.knowledgeBase = knowledgeBase;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override public int hashCode() {
    return (knowledgeBase + name).hashCode();
  }

  @Override public boolean equals(Object obj) {
    Type type = (Type) obj;
    return knowledgeBase.equals(type.knowledgeBase) && name.equals(type.name);
  }

  public String getIdentifierInKb() {
    return knowledgeBase + ":" + name;
  }

  @Override public String toString() {
    return knowledgeBase + "_" + name;
  }

}
