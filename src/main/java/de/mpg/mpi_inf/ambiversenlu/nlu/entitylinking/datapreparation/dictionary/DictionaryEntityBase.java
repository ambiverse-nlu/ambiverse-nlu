package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;


public class DictionaryEntityBase {

  public String entity;

  public String knowledgebase;

  /* TODO The isNamedEntity variable is here because it may be needed to calculate the priors.
   However, it does not fit conceptually here, it is not a characteristic of the dictionary entry
   but of the entity alonr. Ideally this shoould have its own dataprovider. However this will require
   also to disentangle the prior computation in case we make a distintion between named entities
   and concepts*/
  public EntityType isNamedEntity;

  public static DictionaryEntityBase getDictionaryEntityBase (String entity, String knowledgebase, EntityType isNamedEntity) {
    KBIdentifiedEntity kbentity = KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase);
    return new DictionaryEntityBase(kbentity.getIdentifier(), kbentity.getKnowledgebase(), isNamedEntity);
  }


  protected DictionaryEntityBase(String entity, String knowledgebase, EntityType isNamedEntity) {
    super();
    this.entity = entity;
    this.knowledgebase = knowledgebase;
    this.isNamedEntity = isNamedEntity;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DictionaryEntityBase that = (DictionaryEntityBase) o;

    if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;
    if (knowledgebase != null ? !knowledgebase.equals(that.knowledgebase) : that.knowledgebase != null) return false;
    return isNamedEntity.equals(that.isNamedEntity);

  }

  @Override public int hashCode() {
    int result = entity != null ? entity.hashCode() : 0;
    result = 31 * result + (knowledgebase != null ? knowledgebase.hashCode() : 0);
    result = 31 * result + (isNamedEntity != null ? isNamedEntity.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "DictionaryEntity{" + "entity='" + entity + '\'' + ", knowledgebase='" + knowledgebase + '\'' + ", isNamedEntity=" + isNamedEntity + '}';
  }

}
