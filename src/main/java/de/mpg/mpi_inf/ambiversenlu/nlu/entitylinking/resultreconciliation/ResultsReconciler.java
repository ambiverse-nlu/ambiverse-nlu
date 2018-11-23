package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.resultreconciliation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class ResultsReconciler {

  private Logger logger_ = LoggerFactory.getLogger(ResultsReconciler.class);

  private int chunksCount;

  private List<ResultMention> resultMentions;

  private List<ResultEntity> allResultEntities;

  // surface name to identified entities count
  private Map<String, Map<String, Double>> surfaceFormEntityAggregatedScore;

  // entity name to result entity for lookup
  private Map<String, ResultEntity> entityMap;

  private void init() {
    resultMentions = new ArrayList<>();
    allResultEntities = new ArrayList<>();
    surfaceFormEntityAggregatedScore = new HashMap<>();
    entityMap = new HashMap<>();
  }

  public ResultsReconciler() {
    init();
  }

  public ResultsReconciler(int count) {
    init();
    chunksCount = count;
  }

  public void setTotalChunkCount(int count) {
    chunksCount = count;
  }

  /**
   * Add a (chunk's) mentions to be reconciled.
   *
   * @param rms ResultMentions to reconcile.
   */
  public void addMentions(Collection<ResultMention> rms) {
    resultMentions.addAll(rms);
    for (ResultMention rm : rms) {
      allResultEntities.addAll(rm.getResultEntities());
      for (ResultEntity re : rm.getResultEntities()) {
        updateSurfaceNameEntityPair(rm.getMention(), re);
      }
    }
  }

  /**
   * Groups all result mention with same surface forms. Combines all identified entities
   * and returns a single entities list sorted based on maximum aggregated score.
   *
   * @return A Map of ResultMention to List of ResultEntity
   * @param docId docId of the reconciled document (will be kept in the mention).
   */
  public List<ResultMention> reconcile(String docId) {
    Integer runId = RunningTimer.recordStartTime("reconcile");
    if (chunksCount <= 1) {
      logger_.debug("Single chunk : Returning existing mapping.");
      RunningTimer.recordEndTime("reconcile", runId);
      return resultMentions;
    }

    generateEntityMap();
    for (ResultMention rm : resultMentions) {
      List<ResultEntity> res = new ArrayList<>();
      Map<String, Double> aggregatedEntityScore = surfaceFormEntityAggregatedScore.get(rm.getMention());
      aggregatedEntityScore = CollectionUtils.sortMapByValue(aggregatedEntityScore, true);
      Iterator<Entry<String, Double>> it = aggregatedEntityScore.entrySet().iterator();
      while (it.hasNext()) {
        res.add(entityMap.get(it.next().getKey()));
      }
      rm.setResultEntities(res);
    }
    logger_.debug("Reconciled Results for " + chunksCount + " chunks.");
    RunningTimer.recordEndTime("reconcile", runId);
    return resultMentions;
  }

  private void generateEntityMap() {
    for (ResultEntity re : allResultEntities) {
      if (!entityMap.containsKey(re.getEntity())) {
        entityMap.put(re.getEntity(), re);
      }
    }
  }

  private void updateSurfaceNameEntityPair(String mentionName, ResultEntity re) {
    Map<String, Double> entityScore;
    String entity = re.getEntity();
    double score = re.getScore();
    if (surfaceFormEntityAggregatedScore.containsKey(mentionName)) {
      entityScore = surfaceFormEntityAggregatedScore.get(mentionName);
      if (entityScore.containsKey(entity)) {
        entityScore.put(entity, entityScore.get(entity) + score);
      } else {
        entityScore.put(entity, score);
      }
    } else {
      entityScore = new HashMap<String, Double>();
      surfaceFormEntityAggregatedScore.put(mentionName, entityScore);
      entityScore.put(entity, score);
    }
  }
}