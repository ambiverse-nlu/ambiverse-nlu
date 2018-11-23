package de.mpg.mpi_inf.ambiversenlu.nlu.lsh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MinHasher<T> {

  private Logger logger_ = LoggerFactory.getLogger(MinHasher.class);

  // Default magic number.
  private int dimensions_ = 1999999;

  private int hashCount_;

  private int threadCount_;

  private int[] a_;

  private int[] b_;

  private int D_;

  /***
   * @param hashCount: The number of hashes to generate. 
   *  Set this according to the parameters for LSH you need 
   *  (bandSize * bandCount).
   * @param threadCount: Number of threads to use for parallel computation.
   */
  public MinHasher(int hashCount, int threadCount) {
    this.hashCount_ = hashCount;
    this.threadCount_ = threadCount;

    a_ = new int[hashCount_];
    b_ = new int[hashCount_];

    D_ = getPrime(dimensions_);

    Random r = new Random(42);

    for (int i = 0; i < hashCount_; i++) {
      a_[i] = 1 + (int) Math.floor(r.nextDouble() * (D_ - 1));
      b_[i] = (int) Math.floor(r.nextDouble() * D_);
    }
  }

  public Map<T, int[]> createSignatures(Map<T, int[]> itemsFeatures) {
    Map<T, int[]> sigs = new HashMap<>();

    // Divide the work.
    List<List<T>> workPackages = new ArrayList<>(threadCount_);
    for (int i = 0; i < threadCount_; ++i) {
      workPackages.add(new ArrayList<T>());
    }
    int i = 0;
    // Fill workers round robin.
    for (Entry<T, int[]> entry : itemsFeatures.entrySet()) {
      workPackages.get(i % threadCount_).add(entry.getKey());
      ++i;
    }

    ExecutorService es = Executors.newFixedThreadPool(threadCount_);
    for (i = 0; i < threadCount_; ++i) {
      SigComputer sc = new SigComputer(workPackages.get(i), itemsFeatures, sigs);
      es.submit(sc);
    }
    es.shutdown();
    try {
      es.awaitTermination(1, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger_.warn("MinHasher was interrupted, not all signatures are available.");
    }
    return sigs;
  }

  /**
   * Creates minhashes for the given feature ids.
   *
   * @param featureIds Id representation of the item features.
   * @return Minhashes for featuresIds.
   */
  public int[] minhash(int[] featureIds) {
    int[] itemSigs = new int[hashCount_];
    for (int i = 0; i < hashCount_; i++) {
      int min = Integer.MAX_VALUE;
      for (int j = 0; j < featureIds.length; j++) {
        int tmp = (a_[i] * featureIds[j] + b_[i]) % D_;
        if (tmp < min) {
          min = tmp;
        }
      }
      itemSigs[i] = min;
    }
    return itemSigs;
  }

  private int getPrime(int n) {
    while (!isPrime(n)) n++;
    return n;
  }

  private boolean isPrime(int n) {
    if (n <= 2) return n == 2;
    else if (n % 2 == 0) return false;
    for (int i = 3, end = (int) Math.sqrt(n); i <= end; i += 2)
      if (n % i == 0) return false;
    return true;
  }

  class SigComputer extends Thread {

    List<T> items_;

    Map<T, int[]> itemsFeatures_;

    Map<T, int[]> sigs_;

    public SigComputer(List<T> items, Map<T, int[]> itemsFeatures, Map<T, int[]> sigs) {
      items_ = items;
      itemsFeatures_ = itemsFeatures;
      sigs_ = sigs;
    }

    @Override public void run() {
      for (T item : items_) {
        int[] featureIds = itemsFeatures_.get(item);
        int[] itemSigs = minhash(featureIds);
        sigs_.put(item, itemSigs);
      }
    }
  }
}
