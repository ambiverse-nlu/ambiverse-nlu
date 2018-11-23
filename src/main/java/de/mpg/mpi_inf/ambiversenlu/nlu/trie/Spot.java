package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import java.util.Comparator;

public class Spot {

  private int begin;

  private int end;

  private long match;

  public Spot(int begin, int end, long match) {
    this.begin = begin;
    this.end = end;
    this.match = match;
  }

  public int getBegin() {
    return begin;
  }

  public int getEnd() {
    return end;
  }

  public long getMatch() {
    return match;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Spot spot = (Spot) o;

    if (begin != spot.begin) return false;
    if (end != spot.end) return false;
    return match == spot.match;

  }

  @Override public int hashCode() {
    int result = begin;
    result = 31 * result + end;
    result = 31 * result + (int) (match ^ (match >>> 32));
    return result;
  }
  
  
  public static class SortbyLength implements Comparator<Spot> {
    public int compare(Spot a, Spot b)
    {
        return (b.end-b.begin) - (a.end-a.begin);
    }
  }

}
