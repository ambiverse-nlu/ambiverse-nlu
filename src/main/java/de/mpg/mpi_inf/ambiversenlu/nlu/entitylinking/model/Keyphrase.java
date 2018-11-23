package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

/**
 * Stores a keyphrase and count.
 */
public class Keyphrase {

  String keyphrase;

  int count;

  public Keyphrase(String keyphrase) {
    this.keyphrase = keyphrase;
    this.count = 0;
  }

  public Keyphrase(String keyphrase, int count) {
    this.keyphrase = keyphrase;
    this.count = count;
  }

  public String getKeyphrase() {
    return keyphrase;
  }

  public void setKeyphrase(String keyphrase) {
    this.keyphrase = keyphrase;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override public boolean equals(Object obj) {
    if (obj instanceof Keyphrase) {
      Keyphrase k = (Keyphrase) obj;
      return k.getKeyphrase().equals(keyphrase);
    } else {
      return false;
    }
  }

  @Override public int hashCode() {
    return keyphrase.hashCode() + count;
  }

  public String toString() {
    return keyphrase + " (" + count + ")";
  }
}
