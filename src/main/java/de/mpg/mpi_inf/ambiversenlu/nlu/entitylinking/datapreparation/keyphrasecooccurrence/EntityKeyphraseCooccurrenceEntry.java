package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence;

public class EntityKeyphraseCooccurrenceEntry {

  private int entity;

  private int keyphrase;

  private int count;

  public EntityKeyphraseCooccurrenceEntry(int entity, int keyphrase, int count) {
    super();
    this.entity = entity;
    this.keyphrase = keyphrase;
    this.count = count;
  }

  public int getEntity() {
    return entity;
  }

  public int getKeyphrase() {
    return keyphrase;
  }

  public int getCount() {
    return count;
  }

  @Override public String toString() {
    return "E:" + entity + ", KP:" + keyphrase + ", COUNT: " + count;
  }

}
