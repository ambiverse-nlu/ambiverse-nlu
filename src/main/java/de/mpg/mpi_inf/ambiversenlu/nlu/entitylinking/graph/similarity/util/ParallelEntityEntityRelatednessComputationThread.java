package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ParallelEntityEntityRelatednessComputationThread extends Thread {

  private static final Logger logger = LoggerFactory.getLogger(ParallelEntityEntityRelatednessComputationThread.class);

  private Set<Entity> partition;

  private Entities allEntities;

  private EnsembleEntityEntitySimilarity eeSimMeasure;

  private Map<Entity, Map<Entity, Double>> entityEntitySimilarities;

  private Map<Entity, List<Mention>> entityMentionsMap;

  private CountDownLatch cdl;

  private int numCalcs = 0;

  public ParallelEntityEntityRelatednessComputationThread(Set<Entity> partition, Entities allEntities, EnsembleEntityEntitySimilarity eeSim,
      Map<Entity, Map<Entity, Double>> entityEntitySimilarities, Map<Entity, List<Mention>> entityMentionsMap, CountDownLatch cdl) {
    this.partition = partition;
    this.allEntities = allEntities;
    this.eeSimMeasure = eeSim;
    this.entityEntitySimilarities = entityEntitySimilarities;
    this.entityMentionsMap = entityMentionsMap;
    this.cdl = cdl;
  }

  @Override public void run() {
    for (Entity e1 : partition) {
      for (Entity e2 : allEntities) {
        // only calculate and add if e1 < e2 (similarities are
        // symmetric, calculate in lexicographic order)
        if (e1.compareTo(e2) < 0) {
          double sim = 0.0;
          // calculate only if they belong to different mentions
          if (shouldCalculate(e1, e2)) {
            try {
              sim = eeSimMeasure.calcSimilarity(e1, e2);
              numCalcs++;
              // negative is not allowed
              if (sim < 0) {
                logger.warn("Coherence of '" + e1 + "' and '" + e2 + "' was < 0, set to 0");
                sim = 0.0;
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            continue;
          }

          Map<Entity, Double> sims = entityEntitySimilarities.get(e1);
          if (sims == null) {
            sims = new HashMap<Entity, Double>();
            entityEntitySimilarities.put(e1, sims);
          }
          sims.put(e2, sim);
        }
      }
    }
    cdl.countDown();
  }

  public int getNumCalcs() {
    return numCalcs;
  }

  protected boolean shouldCalculate(Entity e1, Entity e2) {
    if (entityMentionsMap != null) {
      Set<Mention> mentions1 = new HashSet<Mention>();

      for (Mention m : entityMentionsMap.get(e1)) {
        mentions1.add(m);
      }

      Set<Mention> mentions2 = new HashSet<Mention>();

      for (Mention m : entityMentionsMap.get(e2)) {
        mentions2.add(m);
      }

      if (mentions1.size() != mentions2.size()) return true;

      for (Mention mention : mentions1) {
        if (!mentions2.contains(mention)) return true;
      }
      return false;
    } else {
      return true;
    }
  }
}
