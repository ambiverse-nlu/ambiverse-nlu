package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.util.*;
import java.util.Map.Entry;

public class CollectionUtils {

  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map) {
    return sortMapByValue(map, false);
  }

  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map, final boolean descending) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        int comp = (o1.getValue()).compareTo(o2.getValue());

        if (descending) {
          comp = comp * (-1);
        }

        return comp;
      }
    });

    LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static Map<String, Double> getWeightStringsAsMap(List<String[]> weightStrings) {
    Map<String, Double> weights = new HashMap<String, Double>();
    for (String[] s : weightStrings) {
      String name = s[0];
      Double weight = Double.parseDouble(s[1]);
      weights.put(name, weight);
    }
    return weights;
  }

  /**
   * Normalizes scores by summing up. First shifts so that the min is 0 if
   * it was negative.
   *
   * @param values
   * @return
   */
  public static <T> Map<T, Double> shiftAndNormalizeValuesToSum(Map<T, Double> values) {
    Double min = Double.MAX_VALUE;
    for (Double s : values.values()) {
      if (s < min) {
        min = s;
      }
    }
    Map<T, Double> shiftScores = new HashMap<T, Double>(values);
    if (min < 0) {
      for (T key : values.keySet()) {
        Double prevScore = values.get(key);
        Double shiftScore = prevScore - min;
        shiftScores.put(key, shiftScore);
      }
    }
    return normalizeValuesToSum(shiftScores);
  }

  /**
   * Normalizes values so that they sum up to 1.
   *
   * @param values
   * @param <T>
   * @return Normalized values.
   */
  public static <T> Map<T, Double> normalizeValuesToSum(Map<T, Double> values) {
    Map<T, Double> normalizedScores = new HashMap<T, Double>();
    double total = 0;
    for (Double d : values.values()) {
      total += d;
    }
    if (total == 0) {
      return values;
    }
    for (Entry<T, Double> entry : values.entrySet()) {
      Double normalizedScore = entry.getValue() / total;
      normalizedScores.put(entry.getKey(), normalizedScore);
    }
    return normalizedScores;
  }

  /**
   * Normalizes values so that they sum up to 1.
   *
   * @param values
   * @return Normalized values.
   */
  public static TIntDoubleHashMap normalizeValuesToSum(TIntDoubleHashMap values) {
    TIntDoubleHashMap normalizedScores = new TIntDoubleHashMap();
    double total = 0;
    for (TIntDoubleIterator itr = values.iterator(); itr.hasNext(); ) {
      itr.advance();
      total += itr.value();
    }
    if (total == 0) {
      return values;
    }
    for (TIntDoubleIterator itr = values.iterator(); itr.hasNext(); ) {
      itr.advance();
      Double normalizedScore = itr.value() / total;
      normalizedScores.put(itr.key(), normalizedScore);
    }
    return normalizedScores;
  }

  /**
   * Normalizes values so that they sum up to 1.
   *
   * @param values
   * @param <T>
   * @return Normalized values.
   */
  public static <T> TObjectDoubleHashMap<T> normalizeValuesToSum(TObjectDoubleHashMap<T> values) {
    TObjectDoubleHashMap<T> normalizedScores = new TObjectDoubleHashMap<T>();
    double total = 0;
    for (TObjectDoubleIterator<T> itr = values.iterator(); itr.hasNext(); ) {
      itr.advance();
      total += itr.value();
    }
    if (total == 0) {
      return values;
    }
    for (TObjectDoubleIterator<T> itr = values.iterator(); itr.hasNext(); ) {
      itr.advance();
      Double normalizedScore = itr.value() / total;
      normalizedScores.put(itr.key(), normalizedScore);
    }
    return normalizedScores;
  }

  public static double getMaxValue(TIntDoubleHashMap map) {
    if (map.isEmpty()) {
      return 0.0;
    }

    double max = -Double.MAX_VALUE;
    for (TIntDoubleIterator itr = map.iterator(); itr.hasNext(); ) {
      itr.advance();
      max = Math.max(itr.value(), max);
    }
    return max;
  }

  /**
   * Groups the input data by the specified column. Will first sort then
   * group.
   *
   * @param input Rows of data.
   * @param groupingRange Position of data to group by.
   * @return Data grouped by position. Key is <TAB> joined data.
   */
  public static Map<String, List<String[]>> groupData(List<String[]> input, final int[] groupingRange) {
    Map<String, List<String[]>> grouped = new HashMap<String, List<String[]>>();
    if (input.size() == 0) {
      return grouped;
    }

    // Sort the input by the grouping position.
    Collections.sort(input, new StringArrayComparator(groupingRange));

    // Iterate and group.
    String[] first = input.get(0);
    String[] group = TsvUtils.getElementsInRange(first, groupingRange);
    List<String[]> part = new ArrayList<String[]>();
    grouped.put(StringUtils.join(group, "\t"), part);
    StringArrayComparator comp = new StringArrayComparator(new int[] { 0, group.length - 1 });
    for (String[] row : input) {
      String[] currentGroup = TsvUtils.getElementsInRange(row, groupingRange);
      String[] currentData = TsvUtils.getElementsNotInRange(row, groupingRange);
      if (comp.compare(group, currentGroup) != 0) {
        part = new ArrayList<String[]>();
        grouped.put(StringUtils.join(currentGroup, "\t"), part);
        group = currentGroup;
      }
      part.add(currentData);
    }
    return grouped;
  }

  static class StringArrayComparator implements Comparator<String[]> {

    private int[] groupingRange_;

    public StringArrayComparator(int[] groupingRange) {
      groupingRange_ = groupingRange;
    }

    public int compare(String[] a, String[] b) {
      String[] aComp = TsvUtils.getElementsInRange(a, groupingRange_);
      String[] bComp = TsvUtils.getElementsInRange(b, groupingRange_);
      for (int i = 0; i < aComp.length; ++i) {
        int comp = aComp[i].compareTo(bComp[i]);
        if (comp != 0) {
          return comp;
        }
      }
      // Everythin is equal.
      return 0;
    }
  }

  /**
   * Selects an element from elements according to the probability distribution
   * given by upperBounds. 
   *
   * @param elements  Elements to choose from.
   * @param upperBounds Probability distribution given by the upper bounds of 
   *                    the interval [0.0, 1.0]. 1.0 must be included.
   * @param rand  Random object
   * @return Randomly selected element.
   */
  public static <T> T getConditionalElement(T[] elements, double[] upperBounds, Random rand) {
    assert elements.length == upperBounds.length;
    double r = rand.nextDouble();
    // Do binary search to get the bound
    int i = bisectSearch(upperBounds, r);
    return elements[i];
  }

  public static int bisectSearch(double[] bounds, double x) {
    int l = 0;
    int r = bounds.length - 1;
    while (l < r) {
      int mid = (l + r) / 2;
      if (x < bounds[mid]) {
        r = mid;
      } else {
        l = mid + 1;
      }
    }
    return l;
  }

  /**
   * Convenience method for the call above.
   *
   * @param elementProbabilities Map with elements as keys and their 
   *  probabilities as values. Values are expected to sum up to 1.
   * @param rand  Random generator to use.
   * @return Randomly selected element according to probabilities.
   */
  public static Integer getConditionalElement(TIntDoubleHashMap elementProbabilities, Random rand) {
    Integer[] elements = new Integer[elementProbabilities.size()];
    double[] probs = new double[elementProbabilities.size()];
    double currentProb = 0.0;
    int i = 0;
    for (TIntDoubleIterator itr = elementProbabilities.iterator(); itr.hasNext(); ) {
      itr.advance();
      elements[i] = itr.key();
      currentProb += itr.value();
      probs[i] = currentProb;
      ++i;
    }
    return getConditionalElement(elements, probs, rand);
  }

  /**
   * Get top k entries in map.
   *
   * CAVEAT: This is done by sorting, heap-based topK retrieval would give 
   * better performance!
   *
   * @param map
   * @param limit
   * @return Best entries of the passed map.
   */
  public static <K, V extends Comparable<? super V>> List<K> getTopKeys(Map<K, V> map, int limit) {
    map = CollectionUtils.sortMapByValue(map, true);
    List<K> topKeys = new ArrayList<>(limit);
    int i = 0;
    for (K k : map.keySet()) {
      if (++i > limit) {
        break;
      }
      topKeys.add(k);
    }
    return topKeys;
  }

  /**
   * Generates all combinations of items in the input of the given length.
   *
   * @param input   Input to generate the combinations from.
   * @param length  Number of items per combination.
   * @return All combinations of length in the input.
   */
  public static <T> Set<List<T>> getAllItemCombinations(T[] input, int length) {
    if (length > input.length) {
      throw new IllegalArgumentException("Length must not be larger than the length of the input.");
    }
    if (input == null || input.length == 0) {
      return new HashSet<>();
    }
    long totalCombinations = ArithmeticUtils.binomialCoefficient(input.length, length);
    if (totalCombinations > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Too many combinations for the given input and length");
    }
    Set<List<T>> allCombinations = new HashSet<>((int) totalCombinations);
    computeAllItemCombinationsRecursive(input, allCombinations, null, 0, length);
    return allCombinations;
  }

  private static <T> void computeAllItemCombinationsRecursive(T[] input, Set<List<T>> output, List<T> combination, int pos, int maxLength) {
    if (pos == input.length && combination.size() == maxLength) {
      output.add(combination);
    }

    if (pos < input.length) {
      if (combination != null) {
        combination.add(input[pos]);
        if (combination.size() == maxLength) {
          output.add(combination);
        } else {
          for (int i = pos; i < input.length; ++i) {
            computeAllItemCombinationsRecursive(input, output, combination, i + 1, maxLength);
          }
        }
      }
      int lastPosToStartFrom = input.length - maxLength + 1;
      for (int i = pos; i < lastPosToStartFrom; ++i) {
        combination = new ArrayList<>(maxLength);
        combination.add(input[pos]);
        computeAllItemCombinationsRecursive(input, output, combination, i + 1, maxLength);
      }
    }
  }
}
