package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Measures {

  /**
   * Calculate the weighted Jaccard Coefficient
   *
   * @param as  Set a
   * @param bs  Set b
   * @param weights Map containing a weight for every a \in as and b \in bs
   * @return weighted Jaccard Coefficient
   */
  public static double getWeightedJaccard(Set<Object> as, Set<Object> bs, Map<Object, Double> weights) {
    return getWeightedJaccard(as, bs, weights, weights);
  }

  /**
   * Calculate the weighted Jaccard Coefficient (use this if objects in sets have different weights according to sets)
   *
   * @param as  Set a
   * @param bs  Set b
   * @param aWeights Map containing a weight for every a \in as 
   * @param bWeights Map containing a weight for every b \in bs
   * @return weighted Jaccard Coefficient
   */
  public static double getWeightedJaccard(Set<Object> as, Set<Object> bs, Map<Object, Double> aWeights, Map<Object, Double> bWeights) {
    // weight of intersection
    double intersectWeight = 0.0;

    for (Object a : as) {
      if (bs.contains(a)) {
        Double aTmp = aWeights.get(a);
        Double bTmp = bWeights.get(a);
        if (aTmp == null) {
          throw new NoSuchElementException("The weight for '" + a + "' is missing in aWeights");
        } else if (bTmp == null) {
          throw new NoSuchElementException("The weight for '" + a + "' is missing in bWeights");
        } else {
          intersectWeight += ((aTmp + bTmp) / 2);
        }
      }
    }

    // weight of as AND bs (might overlap)
    double totalWeight = 0.0;

    for (Object a : as) {
      Double tmp = aWeights.get(a);
      if (tmp != null) {
        totalWeight += tmp;
      } else {
        throw new NoSuchElementException("The weight for '" + a + "' is missing");
      }
    }

    for (Object b : bs) {
      Double tmp = bWeights.get(b);
      if (tmp != null) {
        totalWeight += tmp;
      } else {
        throw new NoSuchElementException("The weight for '" + b + "' is missing");
      }
    }

    // elements in intersection are counted twice
    double unionWeight = totalWeight - intersectWeight;

    double weightedJaccard = intersectWeight / unionWeight;

    return weightedJaccard;
  }

  /**
   * Calculate the weighted Jaccard Coefficient for Trove Sets (use this if objects in sets have different weights according to sets)
   *
   * @param as  Set a
   * @param bs  Set b
   * @param aWeights Map containing a weight for every a \in as 
   * @param bWeights Map containing a weight for every b \in bs
   * @return weighted Jaccard Coefficient
   */
  public static double getWeightedJaccard(TIntHashSet as, TIntHashSet bs, TIntDoubleHashMap aWeights, TIntDoubleHashMap bWeights) {
    if (as.isEmpty() || bs.isEmpty()) {
      return 0.0;
    }

    // weight of intersection
    double intersectWeight = 0.0;

    for (TIntIterator itr = as.iterator(); itr.hasNext(); ) {
      int a = itr.next();
      if (bs.contains(a)) {
        Double aTmp = aWeights.get(a);
        Double bTmp = bWeights.get(a);
        if (aTmp == null) {
          throw new NoSuchElementException("The weight for '" + a + "' is missing in aWeights");
        } else if (bTmp == null) {
          throw new NoSuchElementException("The weight for '" + a + "' is missing in bWeights");
        } else {
          intersectWeight += ((aTmp + bTmp) / 2);
        }
      }
    }

    // weight of as AND bs (might overlap)
    double totalWeight = 0.0;

    for (TIntIterator itr = as.iterator(); itr.hasNext(); ) {
      int a = itr.next();
      Double tmp = aWeights.get(a);
      if (tmp != null) {
        totalWeight += tmp;
      } else {
        throw new NoSuchElementException("The weight for '" + a + "' is missing");
      }
    }

    for (TIntIterator itr = bs.iterator(); itr.hasNext(); ) {
      int b = itr.next();
      Double tmp = bWeights.get(b);
      if (tmp != null) {
        totalWeight += tmp;
      } else {
        throw new NoSuchElementException("The weight for '" + b + "' is missing");
      }
    }

    // elements in intersection are counted twice
    double unionWeight = totalWeight - intersectWeight;

    double weightedJaccard = intersectWeight / unionWeight;

    return weightedJaccard;
  }
}
