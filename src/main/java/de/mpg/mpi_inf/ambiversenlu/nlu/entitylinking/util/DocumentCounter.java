package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.DisambiguationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Observable;

public class DocumentCounter extends Observable {

  private static final Logger logger = LoggerFactory.getLogger(DocumentCounter.class);

  private String resultFilePath;

  private int completed;

  private int total;

  private long startTime;

  private Map<String, DisambiguationResults> resultsMap;

  public DocumentCounter(int total, String resultFilePath, Map<String, DisambiguationResults> resultsMap) {
    completed = 0;
    this.total = total;
    this.resultFilePath = resultFilePath;
    this.resultsMap = resultsMap;
    startTime = System.currentTimeMillis();
  }

  public synchronized void oneDone() {
    setChanged();
    completed++;
    notifyObservers(resultsMap);

    long runtime = (System.currentTimeMillis() - startTime) / 1000;
    logger.info(completed + "/" + total + " DONE (" + runtime + "s total)");
  }

  public Map<String, DisambiguationResults> getResultsMap() {
    return resultsMap;
  }

  public String getResultFilePath() {
    return resultFilePath;
  }
}