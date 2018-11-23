package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class KeyphrasesMeasureTracer extends MeasureTracer {

  private static Logger sLogger_ = LoggerFactory.getLogger(KeyphrasesMeasureTracer.class);

  public static int countForUI = 0;

  private static TIntObjectHashMap<String> id2word = new TIntObjectHashMap<String>();

  private List<keyphraseTracingObject> keyphrases = null;

  private DecimalFormat formatter = new DecimalFormat("#0.00000");

  public KeyphrasesMeasureTracer(String name, double weight) throws EntityLinkingDataAccessException {
    super(name, weight);
    if (weight < 0.0) {
      sLogger_.error("Weight should not be < 0");
    }
    synchronized (id2word) {
      if (id2word.size() == 0) {
        sLogger_.debug("Reading all word ids for tracing.");
        id2word = getAllWordIds();
        sLogger_.debug("Reading all word ids for tracing done.");
      }
    }
    keyphrases = new LinkedList<keyphraseTracingObject>();
  }

  public static TIntObjectHashMap<String> getAllWordIds() throws EntityLinkingDataAccessException {
    TObjectIntHashMap<String> wordIds = DataAccess.getAllWordIds();
    TIntObjectHashMap<String> idWords = new TIntObjectHashMap<String>(wordIds.size());
    for (TObjectIntIterator<String> itr = wordIds.iterator(); itr.hasNext(); ) {
      itr.advance();
      idWords.put(itr.value(), itr.key());
    }
    return idWords;
  }

  @Override public String getOutput() {
    Collections.sort(keyphrases);

    TIntLinkedList wordIds = new TIntLinkedList();
    for (keyphraseTracingObject kto : keyphrases) {
      for (int keyword : kto.keyphraseTokens) {
        wordIds.add(keyword);
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<strong style='color: #0000FF;'> score = " + formatter.format(score) + " for " + keyphrases.size() + " keyphrases</strong><br />");
    int keyphraseCount = 0;
    for (keyphraseTracingObject keyphrase : keyphrases) {
      if (keyphraseCount == 5) {
        countForUI++;
        sb.append("<a class='showMore' onclick=\"setVisibility('div" + countForUI
            + "', 'block');\">More ...</a>&nbsp;&nbsp;&nbsp;<a class='showLess' onclick=\"setVisibility('div" + countForUI
            + "', 'none');\">Less ...</a>");
        sb.append("<div id='div" + countForUI + "' style='display:none'>");
      }
      sb.append("<span style='color: #005500;'>" + formatter.format(keyphrase.score) + "</span> - <span>\"");
      sb.append(buildKeyhraseHTMLEntry(keyphrase.keyphraseTokens, keyphrase.matchedKeywords, id2word));
      sb.append("\" </span> ");
      sb.append("<br />");
      keyphraseCount++;
    }
    if (keyphraseCount >= 5) {
      sb.append("</div>");
    }
    return sb.toString();
  }

  private String buildKeyhraseHTMLEntry(int[] keyphraseTokens, TIntDoubleHashMap matchedKeywords, TIntObjectHashMap<String> id2word) {
    StringBuilder sb = new StringBuilder();
    for (int token : keyphraseTokens) {
      if (matchedKeywords.containsKey(token)) {
        sb.append("<span class='matchedKeyword' style='BACKGROUND-COLOR: #FFAA70;'><strong>" + id2word.get(token) + "</strong> <small>(" + formatter
            .format(matchedKeywords.get(token)) + ")</small></span> ");
      } else {
        sb.append("<strong>" + id2word.get(token) + "</strong> ");
      }
    }

    return sb.toString();
  }

  /**
   * @param keyphraseTokens the keyphrases to add
   * @param weight the average weight of the keyphrase
   * @param score how much score this keyphrase contributes to the total similarity
   * @param matchedKeywords the keywords within this keyphrase and their weights
   */
  public void addKeyphraseTraceInfo(int[] keyphraseTokens, double weight, double score, TIntDoubleHashMap matchedKeywords) {
    keyphrases.add(new keyphraseTracingObject(keyphraseTokens, score, matchedKeywords));
  }

  private class keyphraseTracingObject implements Comparable<keyphraseTracingObject> {

    private int[] keyphraseTokens;

    private double score;

    private TIntDoubleHashMap matchedKeywords;

    public keyphraseTracingObject(int[] keyphraseTokens, double score, TIntDoubleHashMap matchedKeywords) {
      this.keyphraseTokens = keyphraseTokens;
      this.score = score;
      this.matchedKeywords = matchedKeywords;
    }

    @Override public int compareTo(keyphraseTracingObject o) {
      if (score < o.score) return 1;
      else if (score == o.score) return 0;
      else return -1;
    }
  }
}
