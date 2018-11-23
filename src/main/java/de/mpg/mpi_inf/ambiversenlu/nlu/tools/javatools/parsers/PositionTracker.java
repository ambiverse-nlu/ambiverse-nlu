package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class is part of the Java Tools (see
 * http://mpii.de/yago-naga/javatools). It is licensed under the Creative
 * Commons Attribution License (see http://creativecommons.org/licenses/by/3.0)
 * by the YAGO-NAGA team (see http://mpii.de/yago-naga).
 *
 * This class implements position change trackers that keep track of position
 * changes within a String, e.g. caused through normalization etc.
 * This allows for instance, given a position int the normalized string
 * to get the corresponding position in the original non-normalized string 
 *
 *
 *
 * backward position tracker - 
 * tracking several replacement/text changes allowing to trace a position in the modified
 * text back to the corresp. position in the original text
 * for the other direction see ForwardPositionTracker 
 *
 * @author smetzger */
public class PositionTracker {

  private SortedMap<Integer, Integer> positionMap;

  private SortedMap<Integer, Integer> positionChanges;

  private SortedMap<Integer, Integer> old2NewMap;

  private int accumulatedModifier = 0;

  public PositionTracker() {
    positionMap = new TreeMap<Integer, Integer>();
    positionChanges = new TreeMap<Integer, Integer>();
    old2NewMap = new TreeMap<Integer, Integer>();
  }

  public void addPositionChange(int pos, int modifier) {
    if (modifier != 0) {
      int oldModifier = 0;
      old2NewMap.put(pos, modifier);
      accumulatedModifier += modifier;
      if (positionChanges.containsKey(pos + accumulatedModifier)) oldModifier = positionChanges.get(pos + accumulatedModifier);
      positionChanges.put(pos + accumulatedModifier, modifier * -1 + oldModifier);
    }
  }

  /** Closes the current changing run by Merging new position changes into the existing position change map
   *  after each round (one round=consecutive changes along the text) you need to call closeRun() before submitting more position changes from a new round,
   *  i.e. whenever you passed the string to be modified once call closeRun() before starting to run over the string again with more replacements
   * Do this every time you ran once over the text making changes to be tracked*/
  public void closeRun() {
    if (positionChanges.isEmpty()) return;

    SortedMap<Integer, Integer> temp = positionChanges;

    //adapt old positions to new mapping
    while (!positionMap.isEmpty()) {
      Integer key = positionMap.firstKey();
      Collection<Integer> modifiers = old2NewMap.headMap(key + 1).values();
      Integer newposition = key;
      for (Iterator<Integer> it = modifiers.iterator(); it.hasNext(); newposition += it.next()) {
      }
      Integer value = positionMap.get(key);
      if (positionChanges.containsKey(newposition)) value += positionChanges.get(newposition);
      positionChanges.put(newposition, value);
      positionMap.remove(key);
    }

    positionChanges = positionMap;
    positionMap = temp;
    old2NewMap.clear();
    accumulatedModifier = 0;
    return;
  }
}
