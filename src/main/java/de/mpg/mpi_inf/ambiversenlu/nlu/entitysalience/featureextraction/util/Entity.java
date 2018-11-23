package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import java.io.Serializable;

/**
 * Entity and its type.
 */
public class Entity extends DocumentAnnotation implements Serializable {


  public Entity(String id) {
    super(id);
  }

  public static Entity fromUimaEntity(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity entity) {
    return new Entity(entity.getID());
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Entity)) return false;

    Entity entity = (Entity) o;

    return getId().equals(entity.getId());
  }

  @Override public int hashCode() {
    if(getId()==null) {
      return 0;
    }
    int result = getId().hashCode();
    result = 31 * result;
    return result;
  }

  public String toString() {
    return getId();
  }
}
