package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeytermsCache<T> implements Iterable<Pair<Integer, T>> {

  private List<Pair<Integer, T>> entries = new ArrayList<Pair<Integer, T>>();

  public void add(int entityId, T etd) {
    entries.add(new Pair<Integer, T>(entityId, etd));
  }

  public void addAll(int eId, List<T> etds) {
    for (T etd : etds) {
      add(eId, etd);
    }
  }

  @Override public Iterator<Pair<Integer, T>> iterator() {
    return entries.iterator();
  }
}
