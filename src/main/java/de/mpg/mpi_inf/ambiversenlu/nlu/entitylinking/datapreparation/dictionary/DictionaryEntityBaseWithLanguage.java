package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

public class DictionaryEntityBaseWithLanguage extends DictionaryEntityBase {

  public Language language;

  public static DictionaryEntityBaseWithLanguage getDictionaryEntityBaseWithLanguage(String entity, String knowledgebase,
      Language language, EntityType isNamedEntity) {
    KBIdentifiedEntity kbentity = KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase);
    return new DictionaryEntityBaseWithLanguage(kbentity.getIdentifier(), kbentity.getKnowledgebase(), language, isNamedEntity);
  }


  protected DictionaryEntityBaseWithLanguage(String entity, String knowledgebase,
      Language language, EntityType isNamedEntity) {
    super(entity, knowledgebase, isNamedEntity);
    this.language = language;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DictionaryEntityBaseWithLanguage that = (DictionaryEntityBaseWithLanguage) o;

    if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;
    if (knowledgebase != null ? !knowledgebase.equals(that.knowledgebase) : that.knowledgebase != null) return false;
    if (language != null ? !language.equals(that.language) : that.language != null) return false;
    return isNamedEntity.equals(that.isNamedEntity);

  }

  @Override public int hashCode() {
    int result = entity != null ? entity.hashCode() : 0;
    result = 31 * result + (knowledgebase != null ? knowledgebase.hashCode() : 0);
    result = 31 * result + (language != null ? language.hashCode() : 0);
    result = 31 * result + (isNamedEntity != null ? isNamedEntity.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "DictionaryEntity{" + "entity='" + entity + '\'' + ", knowledgebase='" + knowledgebase + '\'' + ", language=" + language + ", isNamedEntity=" + isNamedEntity + '}';
  }
}
