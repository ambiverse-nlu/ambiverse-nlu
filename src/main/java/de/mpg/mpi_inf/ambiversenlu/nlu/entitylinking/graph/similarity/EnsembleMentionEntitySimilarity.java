package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import com.google.common.collect.Iterables;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance.EntityImportance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.EntityImportanceMeasureTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.PriorMeasureTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.MathUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class uses learned weights for all MentionEntitySimilarityMeasure X EntityContext
 * combination to create a combined MentionEntitySimilarity measure.
 *
 * The prior probability of a mention-entity pair can also be used, and should
 * generally improve results.
 *
 * TODO the constructor should not use the Entities object but build the union of
 * all candidate entities from the Mentions (getCandidateEntities()) themselves.
 */
public class EnsembleMentionEntitySimilarity {

  private static final Logger logger = LoggerFactory.getLogger(EnsembleMentionEntitySimilarity.class);

  private List<MentionEntitySimilarity> mesNoPrior;

  private List<MentionEntitySimilarity> mesWithPrior;

  private Map<String, double[]> mesMinMax;

  Map<String, Map<Mention, TIntDoubleHashMap>> precomputedScores;

  /**
   * EntityImportances need to be in [0, 1].
   */
  private List<EntityImportance> eisNoPrior;

  private List<EntityImportance> eisWithPrior;

  private PriorProbability pp = null;

  private SimilaritySettings settings;

  private Tracer tracer = null;

  /**
   * Use this constructor if the context is the same for all mention-entity pairs.
   *
   * TODO For efficieny reasons, the MentionEntitySimilarity should get the SAME Context object passed in the 
   * constructor, and the calcSimilarity method should ONLY get the offsets with respect to the Context. Otherwise
   * it's not possible to efficiently pre-process the Context, e.g. creating indexes or bigrams. At the moment,
   * Context objects are passed to be able to have dynamic token-windows around the mention, but then any pre-processing
   * has to be done in EACH calcSimilarity call.
   *
   * Solution: 
   *  - Pass Context to MentionEntitySimilarity constructor
   *  - instead of Context in calcSimilarity, pass [from,to] pair.
   *
   * @param mentions
   * @param entities
   * @param context
   * @param externalContext
   *@param settings
   * @param tracer   @throws Exception
   */
  public EnsembleMentionEntitySimilarity(Mentions mentions, Entities entities, Context context,
      ExternalEntitiesContext externalContext, SimilaritySettings settings, Tracer tracer, boolean isNamedEntity) throws Exception {
    Map<Mention, Context> sameContexts = new HashMap<Mention, Context>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        sameContexts.put(m, context);
      }
    }
    init(mentions, entities, sameContexts, externalContext, settings, tracer, isNamedEntity);
    logger.debug("Instantiating EnsableMentionEntitySimilarity.");
  }
  
  public EnsembleMentionEntitySimilarity(Mentions mentions, Entities entities, Map<Mention, Context> mentionsContexts, SimilaritySettings settings, 
      Tracer tracer, boolean isNamedEntity) throws Exception {
    init(mentions, entities, mentionsContexts, new ExternalEntitiesContext(), settings, tracer, isNamedEntity);
  }

  private void init(Mentions mentions, Entities entities, Map<Mention, Context> mentionsContexts, ExternalEntitiesContext externalContext, 
      SimilaritySettings settings, Tracer tracer, boolean isNamedEntity) throws Exception {
    logger.debug("Initializing EnsembleMentionEntitySimilarity.");
    this.settings = settings;
    double prior = settings.getPriorWeight();
    Set<Mention> mentionNames = new HashSet<>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        mentionNames.add(m);
      }
    }
    pp = new MaterializedPriorProbability(mentionNames, isNamedEntity);
    pp.setWeight(prior);
    MentionEntitySimilarityPackage mes = settings.getMentionEntitySimilarities(entities, externalContext, tracer);
    mesNoPrior = mes.getMentionEntitySimilarityNoPrior();
    mesWithPrior = mes.getMentionEntitySimilarityWithPrior();
    eisNoPrior = settings.getEntityImportances(entities, false);
    eisWithPrior = settings.getEntityImportances(entities, true);
    this.tracer = tracer;
    mesMinMax = precomputeMinMax(mentions, mentionsContexts);
  }

  /**
   * Updates precomputedScores. 
   *
   * @param mentions
   * @param mentionsContexts
   * @return
   * @throws Exception
   */
  private Map<String, double[]> precomputeMinMax(Mentions mentions, Map<Mention, Context> mentionsContexts) throws Exception {
    precomputedScores = new HashMap<String, Map<Mention, TIntDoubleHashMap>>();
    //scores stay the same for the switched measures, only weights change
    logger.debug("Precomputing MinMax.");
    for (MentionEntitySimilarity s : Iterables.concat(mesWithPrior, mesNoPrior)) {
      if (precomputedScores.containsKey(s.getIdentifier())) {
        continue;
      }
      Map<Mention, TIntDoubleHashMap> measureScores = new HashMap<Mention, TIntDoubleHashMap>();
      precomputedScores.put(s.getIdentifier(), measureScores);
      for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
        for (Mention m : innerMap.values()) {
          Context context = mentionsContexts.get(m);
          TIntDoubleHashMap mentionScores = new TIntDoubleHashMap();
          measureScores.put(m, mentionScores);
          for (Entity e : m.getCandidateEntities()) {
            double sim = s.calcSimilarity(m, context, e);
            mentionScores.put(e.getId(), sim);
          }
        }
      }
    }
    Map<String, double[]> measureMinMaxs = new HashMap<String, double[]>();
    for (String s : precomputedScores.keySet()) {
      double[] minMax = new double[] { Double.MAX_VALUE, 0.0 };
      measureMinMaxs.put(s, minMax);
      Map<Mention, TIntDoubleHashMap> measureScores = precomputedScores.get(s);
      for (TIntDoubleHashMap mentionScores : measureScores.values()) {
        for (TIntDoubleIterator itr = mentionScores.iterator(); itr.hasNext(); ) {
          itr.advance();
          double score = itr.value();
          minMax[0] = Math.min(minMax[0], score);
          minMax[1] = Math.max(minMax[1], score);
        }
      }
    }
    return measureMinMaxs;
  }

  public static double[] rescaleArray(double[] in) {
    double[] out = new double[in.length];

    double total = 0;

    for (double i : in) {
      total += i;
    }

    // rescale
    for (int i = 0; i < in.length; i++) {
      double norm = in[i] / total;
      out[i] = norm;
    }

    return out;
  }

  public double calcSimilarity(Mention mention, Context context, Entity entity) throws Exception {
    Integer id = RunningTimer.recordStartTime("EnsembleMESCalcSim");
    double bestPrior = pp.getBestPrior(mention);
    boolean shouldSwitch = settings.getPriorThreshold() > 0.0;
    // If non-switch sim is computed, prior is always used. Otherwise determine based on the threshold and the distribution.
    boolean shouldUsePrior = !shouldSwitch || shouldIncludePrior(bestPrior, settings.getPriorThreshold(), mention);
    List<MentionEntitySimilarity> mesToUse = shouldUsePrior ? mesWithPrior : mesNoPrior;
    List<EntityImportance> eisToUse = shouldUsePrior ? eisWithPrior : eisNoPrior;

    double weightedSimilarity = 0.0;

    for (MentionEntitySimilarity s : mesToUse) {
      double singleSimilarity = precomputedScores.get(s.getIdentifier()).get(mention).get(entity.getId());
      double[] minMax = mesMinMax.get(s.getIdentifier());
      singleSimilarity = rescale(singleSimilarity, minMax[0], minMax[1]);
      weightedSimilarity += singleSimilarity * s.getWeight();
    }

    switch (settings.getImportanceAggregationStrategy()) {
      case LINEAR_COMBINATION:
        for (EntityImportance ei : eisToUse) {
          double singleImportance = ei.getImportance(entity);
          weightedSimilarity += singleImportance * ei.getWeight();

          EntityImportanceMeasureTracer mt = new EntityImportanceMeasureTracer(ei.getIdentifier(), ei.getWeight());
          mt.setScore(singleImportance);
          tracer.addMeasureForMentionEntity(mention, entity.getId(), mt);
        }
        break;
      case AVERGAE:
        int count = 0;
        double importancesSum = 0;
        for (EntityImportance ei : eisToUse) {
          double singleImportance = ei.getImportance(entity);
          if (singleImportance >= 0) { //entity has an importance from that measure
            importancesSum += singleImportance * ei.getWeight();
            count++;
          }
          EntityImportanceMeasureTracer mt = new EntityImportanceMeasureTracer(ei.getIdentifier(), ei.getWeight());
          mt.setScore(singleImportance);
          tracer.addMeasureForMentionEntity(mention, entity.getId(), mt);
        }
        if (count != 0) {
          weightedSimilarity += (importancesSum / count);
        }
        break;
      default:
        logger.error("Unknown ImportanceAggregationStrategy");
        break;
    }

    if (shouldUsePrior && pp != null && settings.getPriorWeight() > 0.0) {
      double weightedPrior = pp.getPriorProbability(mention, entity);
      if (settings.shouldPriorTakeLog()) {
        weightedPrior = MathUtil.logDamping(weightedPrior, settings.getPriorDampingFactor());
      }
      weightedSimilarity += weightedPrior * pp.getWeight();

      PriorMeasureTracer mt = new PriorMeasureTracer("Prior", pp.getWeight());
      mt.setScore(weightedPrior);
      tracer.addMeasureForMentionEntity(mention, entity.getId(), mt);
    }

    tracer.setMentionEntityTotalSimilarityScore(mention, entity.getId(), weightedSimilarity);
    RunningTimer.recordEndTime("EnsembleMESCalcSim", id);
    return weightedSimilarity;
  }

  /**
   * First half of similarity measures MUST BE the switched ones. 
   * All other ones are just used normally

   * @param mentionEntitySimilarities
   * @param shouldSwitch
   * @param shouldUsePrior
   * @return
   */
  private MentionEntitySimilarity[] getMentionEntitySimilarities(List<MentionEntitySimilarity> mentionEntitySimilarities, boolean shouldUsePrior,
      boolean shouldSwitch) {
    int start = 0;
    int end = mentionEntitySimilarities.size();
    if (shouldSwitch) {
      start = 0;
      end = mentionEntitySimilarities.size() / 2;
      if (shouldUsePrior) {
        start = mentionEntitySimilarities.size() / 2;
        end = mentionEntitySimilarities.size();
      }
    }
    MentionEntitySimilarity[] mesToUse = new MentionEntitySimilarity[end - start];
    for (int i = start; i < end; i++) {
      mesToUse[i - start] = mentionEntitySimilarities.get(i);
    }
    return mesToUse;
  }

  private boolean shouldIncludePrior(double bestPrior, double priorThreshold, Mention mention) {
    boolean shouldUse = bestPrior > priorThreshold;

    if (!shouldUse) {
      return false;
    } else {
      // make sure that at least 10% of all candidates have a prior to make up for lacking data
      int total = 0;
      int withPrior = 0;

      for (Entity e : mention.getCandidateEntities()) {
        total++;

        if (pp.getPriorProbability(mention, e) > 0.0) {
          withPrior++;
        }
      }

      double priorRatio = (double) withPrior / (double) total;

      if (priorRatio >= 0.2) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static double rescale(double value, double min, double max) {
    if (value < min) {
      logger.debug("Wrong normalization, " + value + " not in [" + min + "," + max + "], " + "renormalizing to 0.0.");
      return min;
    } else if (value > max) {
      logger.debug("Wrong normalization, " + value + " not in [" + min + "," + max + "], " + "renormalizing to 1.0.");
      return max;
    }

    if (min == max) {
      // No score or only one, return max.
      return max;
    }

    return (value - min) / (max - min);
  }
}
