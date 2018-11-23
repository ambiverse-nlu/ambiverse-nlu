package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationCounts {

  String id;

  private int truePositives;

  private int trueNegatives;

  private int falsePositives;

  private int falseNegatives;

  private int ignored;

  private Timer timer;

  private boolean isCollection = false;

  private int totalDocs;

  public double getDocAveragePrecision() {
    if (!isCollection) {
      return getPrecision();
    }
    return docAveragePrecision;
  }

  public double getDocAverageRecall() {
    if (!isCollection) {
      return getRecall();
    }
    return docAverageRecall;
  }

  public double getDocAverageF1() {
    if (!isCollection) {
      return getFScore(1);
    }
    return docAverageF1;
  }

  private double docAveragePrecision;

  private double docAverageRecall;

  private double docAverageF1;

  public EvaluationCounts(String id, int truePositives, int trueNegatives, int falsePositives, int falseNegatives, int ignored) {
    this.truePositives = truePositives;
    this.trueNegatives = trueNegatives;
    this.falsePositives = falsePositives;
    this.falseNegatives = falseNegatives;
    this.ignored = ignored;
  }

  public EvaluationCounts() {
  }

  public double getPrecision() {
    return (double) truePositives / (double) (truePositives + falsePositives);
  }

  public double getRecall() {
    return (double) truePositives / (double) (truePositives + falseNegatives);
  }

  public double getFScore(int beta) {
    double prec = getPrecision();
    double recall = getRecall();
    if (prec == 0 && recall == 0) {
      return 0;
    }
    double factor = Math.pow(beta, 2);
    return (1 + factor) * ((prec * recall) / (factor * (prec + recall)));
  }

  public int getTruePositives() {
    return truePositives;
  }

  public int getTrueNegatives() {
    return trueNegatives;
  }

  public int getFalsePositives() {
    return falsePositives;
  }

  public int getFalseNegatives() {
    return falseNegatives;
  }

  public void setTruePositives(int truePositives) {
    this.truePositives = truePositives;
  }

  public void setTrueNegatives(int trueNegatives) {
    this.trueNegatives = trueNegatives;
  }

  public void setFalsePositives(int falsePositives) {
    this.falsePositives = falsePositives;
  }

  public void setFalseNegatives(int falseNegatives) {
    this.falseNegatives = falseNegatives;
  }

  public int getIgnored() {
    return ignored;
  }

  public void setIgnored(int ignored) {
    this.ignored = ignored;
  }

  public int getTotalDocs() {
    return totalDocs;
  }

  public int getNotIgnored() {
    return getTruePositives() + getTrueNegatives() + getFalsePositives() + getFalseNegatives();
  }

  public int getTotalCount() {
    return getNotIgnored() + getIgnored();
  }

  public static EvaluationCounts summarize(String id, List<EvaluationCounts> all) {
    int truePositives = 0;
    int trueNegatives = 0;
    int falsePositives = 0;
    int falseNegatives = 0;
    int ignored = 0;
    double docAveragePrecision = 0;
    double docAverageRecall = 0;
    double docAverageF1 = 0;
    Map<Timer.STAGE, Long> times = new HashMap<>();
    for (EvaluationCounts c : all) {
      truePositives += c.getTruePositives();
      trueNegatives += c.getTrueNegatives();
      falsePositives += c.getFalsePositives();
      falseNegatives += c.getFalseNegatives();
      ignored += c.getIgnored();
      if (!Double.isNaN(c.getPrecision()) && !Double.isNaN(c.getRecall())) {
        docAveragePrecision += c.getPrecision();
        docAverageRecall += c.getRecall();
        docAverageF1 += c.getFScore(1);
      }
      for (Map.Entry<Timer.STAGE, Long> t : c.getTimer().getTimes().entrySet()) {
        if (!times.containsKey(t.getKey())) {
          times.put(t.getKey(), 0l);
        }
        times.put(t.getKey(), times.get(t.getKey()) + t.getValue());
      }
    }
    for (Map.Entry<Timer.STAGE, Long> t : times.entrySet()) {
      times.put(t.getKey(), t.getValue() / all.size());
    }
    EvaluationCounts ec = new EvaluationCounts(id, truePositives, trueNegatives, falsePositives, falseNegatives, ignored);
    ec.isCollection = true;
    ec.docAveragePrecision = docAveragePrecision / all.size();
    ec.docAverageRecall = docAverageRecall / all.size();
    ec.docAverageF1 = docAverageF1 / all.size();
    ec.getTimer().setTimes(times);
    ec.totalDocs = all.size();
    return ec;
  }

  public Timer getTimer() {
    if (timer == null) {
      timer = new Timer();
    }
    return timer;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PRECISION: " + getPrecision() + "\n");
    sb.append("RECALL: " + getRecall() + "\n");
    sb.append("F1: " + getFScore(1) + "\n");

    if (isCollection) {
      sb.append("TOTAL DOCS: " + getTotalDocs() + "\n\n");

      sb.append("DOCUMENT AVERAGE PRECISION: " + getDocAveragePrecision() + "\n");
      sb.append("DOCUMENT AVERAGE RECALL: " + getDocAverageRecall() + "\n");
      sb.append("DOCUMENT AVERAGE F1: " + getDocAverageF1() + "\n\n");

      sb.append("TOTAL MENTIONS: " + getTotalCount() + "\n");
      sb.append("PROCESSED MENTIONS: " + getNotIgnored() + "\n");
      sb.append("IGNORED MENTIONS: " + getIgnored() + "\n\n");
    }
    return sb.toString();
  }
}
