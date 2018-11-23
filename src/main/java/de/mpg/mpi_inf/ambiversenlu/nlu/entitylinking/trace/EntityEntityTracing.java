package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.KeytermEntityEntityMeasureTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.MeasureTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.util.measure.EvaluationMeasures;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang.ArrayUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

public class EntityEntityTracing {

  private String mainEntity;

  private Map<Integer, Integer> correctRanking;

  private Map<Integer, Map<Integer, MeasureTracer>> entityEntityMeasureTracer = new HashMap<Integer, Map<Integer, MeasureTracer>>();

  // below is document EE specific
  private boolean doDocumentEETracing;

  private Map<String, Integer> mention2correctEntity;

  private Map<Mention, Set<Integer>> mention2candidates;

  private Collection<Integer> correctEntities;

  private int totalRanks = 0;

  private double totalReciprocalRanks = 0.0;

  private Map<Integer, Double> weightedDegress;

  private String html = null;

  private DecimalFormat df = new DecimalFormat("0.0E0");

  private Map<Integer, Map<Integer, Double>> entityContext = new HashMap<Integer, Map<Integer, Double>>();

  public EntityEntityTracing() {
    this.doDocumentEETracing = false;
  }

  public EntityEntityTracing(boolean doDocumentEETracing) {
    this.doDocumentEETracing = doDocumentEETracing;
  }

  public String getHtml(TIntObjectHashMap<EntityMetaData> entitiesMetaData) throws EntityLinkingDataAccessException {
    // make sure the html is generated, but don't generate twice    
    if (html == null) {
      generateHtml(entitiesMetaData);
    }

    return html;
  }

  /**
   * Call this method to generate/store tracer output in <em>html</em> and free all other stuff.
   * This uses a lot less memory and is necessary to have tracing for multiple documents.
   * @param entitiesMetaData
   */
  public void generateHtml(TIntObjectHashMap<EntityMetaData> entitiesMetaData) throws EntityLinkingDataAccessException {
    html = generateOutput(entitiesMetaData);

    entityEntityMeasureTracer = null;
    mention2correctEntity = null;
    mention2candidates = null;
    weightedDegress = null;
  }

  public String generateOutput(TIntObjectHashMap<EntityMetaData> entitiesMetaData) throws EntityLinkingDataAccessException {
    StringBuilder sb = new StringBuilder();

    Set<Integer> entityIds = getAllEntities();
    int[] internalIds = ArrayUtils.toPrimitive(entityIds.toArray(new Integer[entityIds.size()]));
    final TIntObjectHashMap<EntityMetaData> emd = DataAccess.getEntitiesMetaData(internalIds);

    List<Integer> sortedEntities = new ArrayList<Integer>(entityIds.size());
    sortedEntities.addAll(entityIds);
    Collections.sort(sortedEntities, new Comparator<Integer>() {

      @Override public int compare(Integer te1, Integer te2) {
        return emd.get(te1).getHumanReadableRepresentation().compareTo(emd.get(te2).getHumanReadableRepresentation());
      }
    });

    if (doDocumentEETracing) {
      List<Mention> sortedMentions = new ArrayList<Mention>(mention2candidates.keySet());
      Collections.sort(sortedMentions);

      sb.append("<h1>Mentions</h1>");
      sb.append("<table class='mmTable'>").append(
          "<td style='font-size:small'><strong>right:</strong> connected mentions<br /><strong>down</strong>: mentions with correct entity</td>");
      for (Mention m : sortedMentions) {
        String mTitle = "<strong>" + m.getMention() + "</strong>/" + m.getCharOffset();
        sb.append("<td>").append(mTitle).append("</td>");
      }

      for (Mention m1 : sortedMentions) {
        String m1Title =
            "<strong>" + m1.getMention() + "</strong>/" + m1.getCharOffset() + " (" + mention2correctEntity.get(m1.getIdentifiedRepresentation())
                + ")";
        sb.append("<tr><td>").append(m1Title).append("</td>");
        for (Mention m2 : sortedMentions) {
          sb.append(getMentionMentionEntities(m1, m2, entitiesMetaData));
        }
        sb.append("</tr>");
      }
      sb.append("</table>");

      sb.append("<h2>Scores</h2>");

      double mrr = totalReciprocalRanks / (double) totalRanks;

      sb.append("<ul>");
      sb.append("<li>Mean Reciprocal Rank: " + mrr + "</li>");
      sb.append("</ul>");

      sb.append("<h1>Entities ordered by Weighted Degree</h1><ol>");
      for (Entry<Integer, Double> e : weightedDegress.entrySet()) {
        String color = (correctEntities.contains(e.getKey())) ? "#ADFF2F" : "#FFA500";
        sb.append("<li style='background-color:").append(color).append(";'>").append(e.getKey()).append(": ").append(e.getValue()).append("</li>");
      }
      sb.append("</ol>");
    }

    sb.append("<p><a href='#sims'>sims</a> - <a href='#overview'>overview</a></p>");

    sb.append("<h1><a name='sims'>Entity-Entity Similarities");
    if (mainEntity != null) {
      sb.append(": ").append(mainEntity);
    }
    sb.append("</a></h1>");

    if (doDocumentEETracing) {
      sb.append("<a name='eesims'></a>");
      for (int entity : sortedEntities) {
        //        if (doDocumentEETracing && (!correctEntities.contains(entity) && !correctEntities.contains(Entity.EXISTS))) {
        //          continue;
        //        }
        sb.append("<a href='" + emd.get(entity).getUrl() + "'>" + emd.get(entity).getHumanReadableRepresentation() + "</a>, ");
      }
    }

    sb.append("<table style='width=300px;padding:10px'>");
    for (int entity : sortedEntities) {
      //      if (doDocumentEETracing && (!correctEntities.contains(entity) && !correctEntities.contains(Entity.EXISTS))) {
      //        continue;
      //      } else
      if (!doDocumentEETracing && !mainEntity.equals(entity)) {
        continue;
      }
      sb.append(getEntityDetailedOutput(entity));
    }
    sb.append("</table>");

    if (getEntityContext(mainEntity) != null) {
      sb.append(getEntityOverviewOutput(mainEntity));
    }

    return sb.toString();
  }

  private String getEntityOverviewOutput(String main) {
    StringBuilder sb = new StringBuilder();

    sb.append("<h1><a name='overview'>Overview: ").append(main).append("</a></h1>");
    sb.append(
        "<table border='1'><tr><th width='30%'>Keyphrase (Weight)</th><th width='55%'>List of Matching Entities (Details)</th><th>Source</th></tr>");

    // get all keyphrases/words for 
    Map<Integer, Double> sortedK = CollectionUtils.sortMapByValue(getEntityContext(main), true);
    for (Integer kId : sortedK.keySet()) {
      String k = KeytermEntityEntityMeasureTracer.id2word.get(kId);
      sb.append("<tr>");
      sb.append("<td>").append("<strong>").append(k).append("</strong>").append(" (").append(df.format(sortedK.get(k))).append(") </td>");

      // find all matching entities + keywords
      Map<Integer, Double> matchingEntities = new HashMap<Integer, Double>();

      for (Integer other : entityContext.keySet()) {
        if (other.equals(main)) continue;

        if (entityContext.get(other).containsKey(k)) {
          matchingEntities.put(other, entityContext.get(other).get(k));
        }
      }

      Map<Integer, Double> sortedMatches = CollectionUtils.sortMapByValue(matchingEntities, true);
      sb.append("<td>");
      for (Integer e : sortedMatches.keySet()) {
        sb.append("<strong>").append(e).append("</strong>").append(" (").append(df.format(sortedMatches.get(e))).append(") - ");
      }
      sb.append("</td>");

      //      sb.append("<td>"+DataAccess.getKeyphraseSource(main, k)+"</td></tr>");
    }

    sb.append("</table>");
    return sb.toString();
  }

  private String getMentionMentionEntities(Mention m1, Mention m2, TIntObjectHashMap<EntityMetaData> entitiesMetaData) {
    if (m1.equals(m2)) {
      return "<td><span style='background-color:black;color:white'>same_mention</span></td>";
    }

    int e1 = mention2correctEntity.get(m1.getIdentifiedRepresentation());
    int e2 = mention2correctEntity.get(m2.getIdentifiedRepresentation());

    if (e1 <= 0 || e2 <= 0) {
      return "<td>no_entity</td>";
    } else if (e1 == e2) {
      return "<td>same_entity</td>";
    }

    Map<Integer, Double> connectedEntities = new HashMap<Integer, Double>();

    if (entityEntityMeasureTracer.containsKey(e1)) {
      for (Entry<Integer, MeasureTracer> e : entityEntityMeasureTracer.get(e1).entrySet()) {
        // only add those mentions that are candidates of the mention
        if (mention2candidates.get(m2).contains(e.getKey())) {
          connectedEntities.put(e.getKey(), e.getValue().getScore());
        }
      }
    }

    Map<Integer, Double> sortedNeighbors = CollectionUtils.sortMapByValue(connectedEntities, true);

    StringBuilder sb = new StringBuilder();

    int positionCorrect = Integer.MAX_VALUE;
    int i = 0;
    int maxEntities = 3;

    for (Integer entityId : sortedNeighbors.keySet()) {
      i++;

      if (entityId.equals(e2)) {
        positionCorrect = i;

        // do not draw everything
        if (!(i > Math.min(maxEntities, correctEntities.size()))) {
          sb.append("<strong>").append(entitiesMetaData.get(entityId).getHumanReadableRepresentation()).append(": ")
              .append(df.format(sortedNeighbors.get(entityId))).append("</strong>").append("<br />");
        }
      } else if (!(i > Math.min(maxEntities, correctEntities.size()))) {
        sb.append(entityId).append(": ").append(df.format(sortedNeighbors.get(entityId))).append("<br />");
      }
    }

    if (positionCorrect > maxEntities) {
      sb.append("...<br />");
      Double value = sortedNeighbors.get(e2);
      if (value == null) {
        value = Double.NaN;
      }
      sb.append("<strong>").append(positionCorrect).append(". ").append(e2).append(": ").append(df.format(value)).append("<strong>").append("<br />");
    }

    totalRanks++;

    double rr = 0.0;

    if (positionCorrect != Integer.MAX_VALUE) {
      rr = (double) 1 / (double) (positionCorrect);
      totalReciprocalRanks += rr;
    }

    String color = calcRankColor(positionCorrect);

    String cell = "<td style='background-color:" + color + ";'>" + sb.toString() + "</td>";

    return cell;
  }

  private Set<Integer> getAllEntities() {
    return entityEntityMeasureTracer.keySet();
  }

  public synchronized void addEntityEntityMeasureTracer(int e1, int e2, MeasureTracer mt) {
    Map<Integer, MeasureTracer> second = entityEntityMeasureTracer.get(e1);

    if (second == null) {
      second = new HashMap<Integer, MeasureTracer>();
      entityEntityMeasureTracer.put(e1, second);
    }

    second.put(e2, mt);
  }

  //  public void addEntityContextTracer(String entity, TracerPart mt) {
  //    entityContextTracers.put(entity, mt);
  //  }

  private String getEntityDetailedOutput(Integer entity) {
    StringBuilder sb = new StringBuilder();
    sb.append("<tr>");

    // entity itself
    sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;border-right:1px solid gray;'>").append("<strong><a name='").append(entity)
        .append("'>").append(entity).append("</a></strong><br />");

    //    if (entityContextTracers.get(entity.entity) != null) {
    //      sb.append(entityContextTracers.get(entity.entity).getOutput());
    //    }
    sb.append("</td>");

    Map<Integer, Double> connectedEntities = new HashMap<Integer, Double>();
    for (Entry<Integer, MeasureTracer> e : entityEntityMeasureTracer.get(entity).entrySet()) {
      connectedEntities.put(e.getKey(), e.getValue().getScore());
    }
    Map<Integer, Double> sortedNeighbors = CollectionUtils.sortMapByValue(connectedEntities, true);

    // all correct entities
    sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;border-right:1px solid gray'>");
    sb.append("<div style='width:90%;padding:5px;color:#CCCCCC;text-align:right'><em>Related entities</em></div>");

    List<List<Integer>> rankedNeighbors = convertToRanks(sortedNeighbors);
    Map<Integer, Double> neighbor2rank = EvaluationMeasures.convertToAverageRanks(rankedNeighbors);
    Map<Integer, Double> sortedNeighbor2rank = CollectionUtils.sortMapByValue(neighbor2rank, false);

    for (Integer entityId : sortedNeighbor2rank.keySet()) {
      if (doDocumentEETracing && !correctEntities.contains(entityId)) {
        continue;
      }

      double actualRank = neighbor2rank.get(entityId); // average can be 0.5

      sb.append("<span style='font-size:14pt; font-weight:bold;'>");
      if (!doDocumentEETracing) {
        int correctRank = correctRanking.get(entityId);
        double diff = actualRank - correctRank;
        String color = calcRelDistColor(1 - (double) Math.abs(diff) / (double) sortedNeighbors.size());
        sb.append("<span style='background-color:" + color + ";'>").append(actualRank + ". (" + correctRank + "|" + diff + ")</span> ");
      }
      sb.append(entityId).append(":</span> <span style='font-size:14pt; font-style:italic;'>").append(sortedNeighbors.get(entityId)).append("</span>")
          .append("<br />");

      MeasureTracer targetMt = getEntityEntityMeasureTracer(entity, entityId);
      if (targetMt != null) {
        String mtOutput = targetMt.getOutput();
        sb.append(mtOutput).append("<br /><br />\n");
      }
    }
    sb.append("</td>");

    // top connected entities 
    if (doDocumentEETracing) {
      sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;'>");
      sb.append("<div style='width:90%;padding:5px;color:#CCCCCC;text-align:right'><em>Best Entities</em></div>");

      int i = 0;

      for (Integer entityId : sortedNeighbors.keySet()) {
        sb.append("<strong>").append(entityId).append(": ").append(sortedNeighbors.get(entityId)).append("</strong>").append("<br />");

        MeasureTracer targetMt = getEntityEntityMeasureTracer(entity, entityId);
        if (targetMt != null) {
          String mtOutput = targetMt.getOutput();
          sb.append(mtOutput).append("<br /><br />");
        }

        if (++i > Math.max(5, correctEntities.size())) {
          break;
        }
      }
      sb.append("</td>");
    }
    sb.append("</tr>");

    return sb.toString();
  }

  private List<List<Integer>> convertToRanks(Map<Integer, Double> sortedNeighbors) {
    List<List<Integer>> rankedNeighbors = new LinkedList<List<Integer>>();

    List<Integer> currentRank = null;
    double currentValue = -1.0;

    for (Entry<Integer, Double> e : sortedNeighbors.entrySet()) {
      Integer rankedEntity = e.getKey();
      double rankValue = e.getValue();

      if (rankValue == currentValue) {
        currentRank.add(rankedEntity);
      } else {
        currentRank = new LinkedList<Integer>();
        currentRank.add(rankedEntity);
        rankedNeighbors.add(currentRank);
        currentValue = rankValue;
      }
    }

    return rankedNeighbors;
  }

  private MeasureTracer getEntityEntityMeasureTracer(Integer e1, Integer e2) {
    Integer first = e1;
    Integer second = e2;

    Map<Integer, MeasureTracer> two = entityEntityMeasureTracer.get(first);

    if (two != null) {
      return two.get(second);
    } else {
      return null;
    }
  }

  public void setMention2Candidates(Map<Mention, Set<Integer>> m2c) {
    this.mention2candidates = m2c;
  }

  public void setMention2CorrectEntity(Map<String, Integer> m2ce) {
    this.mention2correctEntity = m2ce;
  }

  public void setCorrectEntities(Collection<Integer> correctEntities) {
    this.correctEntities = correctEntities;
  }

  private String calcRankColor(int rank) {
    String color = "#FFFFFF";

    if (rank > 3) {
      color = "#FF0000";
    }

    switch (rank) {
      case 1:
        color = "#00FF00";
        break;
      case 2:
        color = "#ADFF2F";
        break;
      case 3:
        color = "#FFA500";
        break;
    }

    return color;
  }

  private String calcRelDistColor(double test) {
    String color = "#FFFFFF";
    test -= 0.35;
    if (test < 0) {
      test = 0;
    }

    try {
      if (test > 1) {
        test = 1;
      }
      boolean red = false;
      int depth = 0;
      if (test < 0.5) {
        red = true;
        depth = (int) ((double) (200) / 0.5 * test);
      } else {
        test = 1 - test;
        depth = (int) ((double) (200) / 0.5 * test);
      }
      String hex = Integer.toHexString(depth);
      if (hex.length() == 1) {
        hex = 0 + hex;
      }
      if (red) {
        color = "#FF" + hex + hex;
      } else {
        color = "#" + hex + "FF" + hex;
      }
    } catch (Exception e) {
    }
    return color;
  }

  public void setWeightedDegrees(Map<Integer, Double> sortedWeightedDegrees) {
    this.weightedDegress = sortedWeightedDegrees;
  }

  public String getMainEntity() {
    return mainEntity;
  }

  public void setMainEntity(String mainEntity) {
    this.mainEntity = mainEntity;
  }

  public void setCorrectRanking(Map<Integer, Integer> correctRanking) {
    this.correctRanking = correctRanking;
  }

  public void addEntityContext(int entity, Map<Integer, Double> e1keyphrases) {
    entityContext.put(entity, e1keyphrases);
  }

  public Map<Integer, Double> getEntityContext(String entity) {
    return entityContext.get(entity);
  }
}
