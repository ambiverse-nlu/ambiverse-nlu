package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

public class KeytermEntityEntityMeasureTracer extends MeasureTracer {

  private static final Logger logger = LoggerFactory.getLogger(KeytermEntityEntityMeasureTracer.class);

  public static TIntObjectHashMap<String> id2word = new TIntObjectHashMap<String>();

  Map<Integer, Double> terms;

  Map<Integer, TermTracer> matchedTerms;

  TIntObjectHashMap<int[]> keyphraseTokens;

  private DecimalFormat sFormatter = new DecimalFormat("0.00E0");

  private DecimalFormat percentFormatter = new DecimalFormat("#0.0");

  public static final String UI_PREFIX = "KWCSEEMT";

  public static int countForUI = 0;

  public KeytermEntityEntityMeasureTracer(String name, double weight, Map<Integer, Double> keyphraseWeights, Map<Integer, TermTracer> matches)
      throws EntityLinkingDataAccessException {
    this(name, weight, keyphraseWeights, matches, null);
  }

  // keyphraseTokens must be present if the tracer should support inner matches!
  public KeytermEntityEntityMeasureTracer(String name, double weight, Map<Integer, Double> keyphraseWeights, Map<Integer, TermTracer> matches,
      TIntObjectHashMap<int[]> keyphraseTokens) throws EntityLinkingDataAccessException {
    super(name, weight);

    this.terms = keyphraseWeights;
    this.matchedTerms = matches;
    this.keyphraseTokens = keyphraseTokens;

    synchronized (id2word) {
      if (id2word.size() == 0) {
        logger.debug("Reading all word ids for tracing.");
        id2word = getAllWordIds();
        logger.debug("Reading all word ids for tracing done.");
      }
    }
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
    int keywordCount = 0;

    StringBuilder sb = new StringBuilder();

    //    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;<em>eesim: " + weight + "</em><br />");

    Map<Integer, TermTracer> sortedMatches = CollectionUtils.sortMapByValue(matchedTerms, true);

    double totalWeight = 0.0;
    for (TermTracer tt : matchedTerms.values()) {
      totalWeight += tt.getTermWeight();
    }

    double currentWeight = 0.0;

    for (Entry<Integer, TermTracer> k : sortedMatches.entrySet()) {
      int termId = k.getKey();
      String term = id2word.get(termId);
      if (term == null) {
        term = "COULD_NOT_GET_WORD";
      }
      keywordCount++;

      if (keywordCount == 1) {
        countForUI++;
        sb.append(" <a onclick=\"setVisibility('div" + UI_PREFIX + countForUI
            + "', 'block');\">More ...</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick=\"setVisibility('div" + countForUI + "', 'none');\">Less ...</a>");
        sb.append("<div id='div" + UI_PREFIX + countForUI + "' style='display:none'>");
      }

      for (int innerId : keyphraseTokens.get(termId)) {
        String inner = id2word.get(innerId);
        if ((k.getValue().getInnerMatches() != null) && k.getValue().getInnerMatches().containsKey(innerId)) {
          sb.append("<span style='background-color:#FFAA70;'>").append(inner).append(" (")
              .append(sFormatter.format(k.getValue().getInnerMatches().get(innerId))).append(")</span> ");
        } else {
          sb.append(inner).append(" ");
        }
      }
      double termWeight = terms.get(termId);
      sb.append(": ").append(sFormatter.format(termWeight));
      //      part = "<span style='background-color: #ADFF2F;'>" +
      try {
        if (terms.containsKey(termId)) {
          double matchWeight = matchedTerms.get(termId).getTermWeight();
          currentWeight += matchWeight;
          double contribPercent = matchWeight / termWeight;
          sb.append(" (" + percentFormatter.format(contribPercent) + " contrib, " + sFormatter.format(matchWeight) + ")");
        }
      } catch (IllegalArgumentException e) {
        logger.warn("Could not format weight for '" + term + "': " + terms.get(term));
      }

      sb.append("<br />");

      if (keywordCount % 10 == 0) {
        double percent = currentWeight / totalWeight * 100;
        sb.append(
            "<div style='margin: 5px auto 5px 10px; font-weight:bold;'>" + sFormatter.format(currentWeight) + " (" + percentFormatter.format(percent)
                + "%)</div>");
      }
    }

    if (keywordCount >= 1) {
      sb.append("</div>");
    }

    sb.append("<div style='font-weight:bold;text-align:right;width:80%'>Matches: " + keywordCount + "/" + terms.size() + "</div>");

    return sb.toString();
  }

  public Map<Integer, TermTracer> getMatchedKeywords() {
    return matchedTerms;
  }
}
