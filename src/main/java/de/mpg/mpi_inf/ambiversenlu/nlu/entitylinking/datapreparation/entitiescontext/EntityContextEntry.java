package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

public class EntityContextEntry {

  public String context;

  public String source;

  public Language language;

  @Deprecated public EntityContextEntry(String context, String source) {
    this.context = context;
    this.source = source;
  }

  public EntityContextEntry(String context, String source, Language language) {
    this.context = context;
    this.source = source;
    this.language = language;
  }

  @Override public boolean equals(Object obj) {
    EntityContextEntry context1 = (EntityContextEntry) obj;
    return context.equals(context1.context) && source.equals(context1.source);
  }

  @Override public int hashCode() {
    return (context + source).hashCode();
  }

  @Override public String toString() {

    return "context:" + context + ", source:" + source;
  }

}
