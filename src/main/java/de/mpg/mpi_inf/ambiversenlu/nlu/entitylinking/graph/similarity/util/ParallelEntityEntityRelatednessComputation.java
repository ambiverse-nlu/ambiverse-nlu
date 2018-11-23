package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ParallelEntityEntityRelatednessComputation {

  private int numThreads = 4; // default.

  private long totalNumCalcs = 0; // this is only valid if the object is created anew for each entitiy set - used for timing experiments

  public ParallelEntityEntityRelatednessComputation() {
    this(Integer.parseInt(EntityLinkingConfig.get(EntityLinkingConfig.GRAPH_ENTITIES_COMP_THREADS)));
  }

  public ParallelEntityEntityRelatednessComputation(int numThreads) {
    this.numThreads = numThreads;
  }

  public Map<Entity, Map<Entity, Double>> computeRelatedness(EnsembleEntityEntitySimilarity entitySimilarity, Entities entities)
      throws InterruptedException {
    return computeRelatedness(entitySimilarity, entities, null);
  }

  public Map<Entity, Map<Entity, Double>> computeRelatedness(EnsembleEntityEntitySimilarity entitySimilarity, Entities entities, Mentions mentions)
      throws InterruptedException {
    Integer runId = RunningTimer.recordStartTime("computeRelatedness");
    Map<Entity, Map<Entity, Double>> entityEntitySimilarities = Collections.synchronizedMap(new HashMap<Entity, Map<Entity, Double>>());

    Map<Entity, List<Mention>> entityMentionsMap = null;
    if (mentions != null) {
      entityMentionsMap = prepareEntityMentionsMap(mentions);
    }

    List<Set<Entity>> entityPartitions = new LinkedList<Set<Entity>>();
    List<Entity> allEntities = new ArrayList<Entity>(entities.getEntities());

    int overall = 0;
    Set<Entity> part = null;
    int partSize = entities.size() / numThreads;

    for (int currentPart = 0; currentPart < numThreads; currentPart++) {
      part = new HashSet<Entity>();
      entityPartitions.add(part);

      for (int j = 0; j < partSize; j++) {
        int total = (currentPart * partSize) + j;
        part.add(allEntities.get(total));

        overall++;
      }
    }

    // add rest to last part
    for (; overall < allEntities.size(); overall++) {
      part.add(allEntities.get(overall));
    }

    // create threads and run
    CountDownLatch cdl = new CountDownLatch(numThreads);

    List<ParallelEntityEntityRelatednessComputationThread> scs = new LinkedList<ParallelEntityEntityRelatednessComputationThread>();

    for (int i = 0; i < numThreads; i++) {
      ParallelEntityEntityRelatednessComputationThread sc = new ParallelEntityEntityRelatednessComputationThread(entityPartitions.get(i), entities,
          entitySimilarity, entityEntitySimilarities, entityMentionsMap, cdl);
      scs.add(sc);
      sc.start();
    }

    // wait for calculation to finish
    cdl.await();

    // sum up total number of calculations
    for (ParallelEntityEntityRelatednessComputationThread sc : scs) {
      totalNumCalcs += sc.getNumCalcs();
    }
    RunningTimer.recordEndTime("computeRelatedness", runId);
    return entityEntitySimilarities;
  }

  private Map<Entity, List<Mention>> prepareEntityMentionsMap(Mentions mentions) {
    Map<Entity, List<Mention>> entityMentionsMap = new HashMap<Entity, List<Mention>>();

    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        Entities entities = mention.getCandidateEntities();
        for (Entity entity : entities) {
          List<Mention> entityMentions = entityMentionsMap.get(entity);
          if (entityMentions == null) {
            entityMentions = new LinkedList<Mention>();
            entityMentionsMap.put(entity, entityMentions);
          }
          entityMentions.add(mention);
        }
      }
    }

    return entityMentionsMap;
  }

  public long getTotalNumCalcs() {
    return totalNumCalcs;
  }
}
