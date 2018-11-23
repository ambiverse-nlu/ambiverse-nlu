package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.util.Arrays;
import java.util.List;

public enum EntityType {
  NAMED_ENTITY("NAMED_ENTITY", "TRUE", "<true>"),
  CONCEPT("CONCEPT", "FALSE", "<false>"),
  BOTH("BOTH", "<both>"),
  UNKNOWN("UNKNOWN", "<unknown>");
  
  List<String> values;
  EntityType (String ...values) {
    this.values = Arrays.asList(values);
  }

  public static EntityType find(String name) {
    for (EntityType type : EntityType.values()) {
      if (type.getValues().contains(name)) {
        return type;
      }
    }
    return null;
  }
  
  public int getDBId() {
    switch (this) {
      case NAMED_ENTITY:
        return NAMED_ENTITY.ordinal();
      case CONCEPT:
        return CONCEPT.ordinal();
      default:
        return BOTH.ordinal();
    }
  }
  
  public static EntityType getNameforDBId(int dbId) {
    switch (dbId) {
      case 0:
        return NAMED_ENTITY;
      case 1:
        return CONCEPT;
      default:
        return BOTH;//for both, unknown we assume it is both
    }
  }
  
  private List<String> getValues() {
    return values;
  }
}
