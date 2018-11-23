package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph;

import com.google.common.collect.Ordering;
import com.google.common.math.DoubleMath;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessCache;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.extraction.ExtractGraph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleMentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MaterializedPriorProbability;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullGraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Counter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class GraphGenerator {

  public class MaximumGraphSizeExceededException extends Exception {

    private static final long serialVersionUID = -4159436792558733318L;

    public MaximumGraphSizeExceededException() {
      super();
    }

    public MaximumGraphSizeExceededException(String message) {
      super(message);
    }
  }

  private static final double EPSILON = 1E-5;

  private static final Logger logger = LoggerFactory.getLogger(GraphGenerator.class);

  private final Mentions conceptMentions;
  private final Mentions namedEntityMentions;
  
  private final Context context;

  private String docId;

  private DisambiguationSettings settings;

  private Tracer tracer = null;

  
  private Map<Integer, Map<Integer, ResultMention>> resultMentions;
  
  // set this to 20000 for web server so it can still run for web server
  private int maxNumCandidateEntitiesForGraph = 0;

  public GraphGenerator(Mentions conceptMentions, Mentions namedEntityMentions, Context context, String docId, DisambiguationSettings settings, Tracer tracer) {
    this(conceptMentions, namedEntityMentions, context, docId, null, settings, tracer);
  }

  public GraphGenerator(Mentions conceptMentions, Mentions namedEntityMentions, Context context, String docId, Map<Integer, Map<Integer, ResultMention>> resultMentions,
      DisambiguationSettings settings, Tracer tracer) {
    //    this.storePath = content.getStoreFile();
    this.conceptMentions = conceptMentions;
    this.namedEntityMentions = namedEntityMentions;
    this.context = context;
    this.docId = docId;
    this.resultMentions = resultMentions;
    this.settings = settings;
    this.tracer = tracer;
    try {
      if (EntityLinkingConfig.get(EntityLinkingConfig.GRAPH_ENTITIES_MAXCOUNT) != null) {
        maxNumCandidateEntitiesForGraph = Integer.parseInt(EntityLinkingConfig.get(EntityLinkingConfig.GRAPH_ENTITIES_MAXCOUNT));
      }
    } catch (Exception e) {
      maxNumCandidateEntitiesForGraph = 0;
    }
  }

  public Graph run() throws Exception {
    logger.debug("Graph generator run.");
    Graph gData = null;
    gData = generateGraph();
    return gData;
  }

  private Graph generateGraph() throws Exception {
    int timerId = RunningTimer.recordStartTime("GraphGenerator");
    
    // Add result mentions to current mentions
    Set<KBIdentifiedEntity> resultEntities = new HashSet<>();
    for (Map<Integer, ResultMention> innerMap:resultMentions.values()) {
      for (ResultMention rm:innerMap.values()) {
        resultEntities.add(rm.getBestEntity().getKbEntity());
      }
    }
    TObjectIntHashMap<KBIdentifiedEntity> resultEntityInternalIds = DataAccess.getInternalIdsForKBEntities(resultEntities);
    TIntObjectHashMap<EntityType> resultEntityClasses = DataAccessCache.singleton().getEntityClasses(resultEntityInternalIds.values());
    

    for (Map<Integer, ResultMention> innerMap:resultMentions.values()) {
      for (ResultMention rm:innerMap.values()) {
        int internalId = resultEntityInternalIds.get(rm.getBestEntity().getKbEntity());
        Entity entity = new Entity(rm.getBestEntity().getKbEntity(), internalId);
        Mention mention = new Mention(rm.getMention(), rm.getCharacterOffset(), rm.getCharacterOffset() + rm.getCharacterLength(), entity);
        mention.setCharOffset(rm.getCharacterOffset());
        mention.setCharLength(rm.getCharacterLength());
        
        EntityType entityType = resultEntityClasses.get(internalId);
        if (entityType == EntityType.NAMED_ENTITY) {
          namedEntityMentions.addMention(mention);
        }
        else if (entityType == EntityType.CONCEPT) {
          conceptMentions.addMention(mention);
        }
        else {
          namedEntityMentions.addMention(mention);
          conceptMentions.addMention(mention);
        }
      }
    }
    
    // Final mentions, entities
    Mentions mentions = new Mentions();
    Map<Mention, TIntDoubleHashMap> mentionEntityLocalSims = new HashMap<Mention, TIntDoubleHashMap>();
    Entities allEntities = new Entities();
    

    // Filling mentions and entities with new mentions and candidates:
    if (!namedEntityMentions.getMentions().isEmpty()) {
      fillEntities(namedEntityMentions, mentions, allEntities, mentionEntityLocalSims, true, true);
    }
    if (!conceptMentions.getMentions().isEmpty()) {
      fillEntities(conceptMentions, mentions, allEntities, mentionEntityLocalSims, false, false);
    }
    
    EnsembleEntityEntitySimilarity eeSim;
    //When both mentions are availbe choose the E-E setting with more number of mentions. Since E-E similarity is also calculated between concepts and named entities. (only in joint disambiguation this happens)
    if (namedEntityMentions.getMentions().size() > conceptMentions.getMentions().size()) {
      eeSim = 
          new EnsembleEntityEntitySimilarity(
              allEntities, settings.getSimilaritySettings(true), tracer);
    }
    else {
      eeSim = 
          new EnsembleEntityEntitySimilarity(
              allEntities, settings.getSimilaritySettings(false), tracer);
    }
    
    ExtractGraph egraph = 
        new ExtractGraph(
            docId, mentions, allEntities, eeSim, settings.getGraphSettings().getAlpha());
    Graph gData = egraph.generateGraph();
    gData.setMentionEntitySim(mentionEntityLocalSims);
    
    
    
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (namedEntityMentions.containsOffsetAndLength(m.getCharOffset(), m.getCharLength())) {
          namedEntityMentions.setMentionForOffset(m.getCharOffset(), m);
        }
        if (conceptMentions.containsOffsetAndLength(m.getCharOffset(), m.getCharLength())) {
          conceptMentions.setMentionForOffset(m.getCharOffset(), m);
        }
      }
    }
    
    RunningTimer.recordEndTime("GraphGenerator", timerId);
    logger.debug("Graph mention and entities filled finished.");
    return gData;
  }

  
  private void fillEntities(Mentions inputMentions, Mentions outputMentions, Entities allEntitiesResults, 
      Map<Mention, TIntDoubleHashMap> mentionEntityLocalSims, boolean isNamedEntity, boolean preprocess) throws Exception {
    Integer id = RunningTimer.recordStartTime("GatherCandidateEntities");
    Entities allEntitiesTemp = EntityLinkingManager.getAllEntities(inputMentions, new ExternalEntitiesContext(), tracer);
    RunningTimer.recordEndTime("GatherCandidateEntities", id);
    
    // Check if the number of candidates exceeds the threshold (for memory
    // issues).
    if (maxNumCandidateEntitiesForGraph != 0 && allEntitiesTemp.size() > maxNumCandidateEntitiesForGraph) {
      throw new MaximumGraphSizeExceededException(
          "Maximum number of candidate entites for graph exceeded " + allEntitiesTemp.size());
    }

    id = RunningTimer.recordStartTime("GG-LocalSimilarityCompute");
    
    logger.debug("Computing the mention-entity similarities...");
    
    // Counters for keeping track.
    int solvedByCoherenceRobustnessHeuristic = 0;
    int solvedByConfidenceThresholdHeuristic = 0;
    int solvedByEasyMentionsHeuristic = 0;
    int preGraphNullMentions = 0;
    int prunedMentionsCount = 0;
    Integer timer;
    
    Map<Mention, Double> mentionL1s = null;
    if (preprocess) {
      timer = RunningTimer.recordStartTime("MentionPriorSimL1DistCompute");
      if (settings.getGraphSettings().shouldUseCoherenceRobustnessTest(isNamedEntity)) {
        mentionL1s = computeMentionPriorSimL1Distances(inputMentions, allEntitiesTemp, isNamedEntity);
      }
      RunningTimer.recordEndTime("MentionPriorSimL1DistCompute", timer);
    }
    
    timer = RunningTimer.recordStartTime("EnsembleMentionEntitySimInit");
    EnsembleMentionEntitySimilarity mentionEntitySimilarity = new EnsembleMentionEntitySimilarity(
        inputMentions, allEntitiesTemp, context,
        new ExternalEntitiesContext(), settings.getSimilaritySettings(isNamedEntity), tracer, isNamedEntity);
      
    logger.debug("Computing the mention-entity similarities...");
    RunningTimer.recordEndTime("EnsembleMentionEntitySimInit", timer);

    // Keep the similarities for all mention-entity pairs, as some are
    // dropped later on.
    
    allEntitiesTemp = new Entities();
    if (settings.isIncludeNullAsEntityCandidate()) {
      allEntitiesTemp.setIncludesOokbeEntities(true);
    }
    
    timer = RunningTimer.recordStartTime("CalcSim(AndRobustness)ForCandidateEntities");
    for (Map<Integer, Mention> innerMap : inputMentions.getMentions().values()) {
      for (Mention currentMention : innerMap.values()) {
        Counter.incrementCount("MENTIONS_TOTAL");
        
        TIntDoubleHashMap entityLocalSims = new TIntDoubleHashMap();
        if (!mentionEntityLocalSims.containsKey(currentMention)) {
          mentionEntityLocalSims.put(currentMention, entityLocalSims);
        }
        
        Entities originalCandidateEntities = currentMention.getCandidateEntities();
        
        // Compute similarities for all candidates.   
        for (Entity candidate : originalCandidateEntities) {
          // Keyphrase-based mention/entity similarity.
          double similarity = mentionEntitySimilarity.calcSimilarity(currentMention, context, candidate);
          candidate.setMentionEntitySimilarity(similarity);
          if (entityLocalSims.containsKey(candidate.getId())) {
            if (entityLocalSims.get(candidate.getId()) != similarity) {
              logger.info("*SIM NOT EQ: " + entityLocalSims.get(candidate.getId()) + " " + similarity);
            }
          }
          entityLocalSims.put(candidate.getId(), (entityLocalSims.get(candidate.getId()) + similarity)/2);
        }    
        logger.debug("Building the graph...");
        
        if (!preprocess) {
          // Add all candidates to the graph
          currentMention.setCandidateEntities(originalCandidateEntities);
          allEntitiesTemp.addAll(originalCandidateEntities);
        }
        else {
          TIntDoubleHashMap normalizedEntityLocalSims = 
              CollectionUtils.normalizeValuesToSum(entityLocalSims);
          
          Entity bestEntity = null;
    
          // Do pre-graph algorithm null-mention discovery
          if (settings.getGraphSettings().isPreCoherenceNullMappingDiscovery()) {
            double max = CollectionUtils.getMaxValue(normalizedEntityLocalSims);
            if (DoubleMath.fuzzyCompare(max, settings.getGraphSettings().getPreCoherenceNullMappingDiscoveryThreshold(), EPSILON) < 0) {
              bestEntity = new NullEntity();
              ++preGraphNullMentions;
              GraphTracer.gTracer.addMentionToEasy(
                  docId, currentMention.getMention(), currentMention.getCharOffset());
              Counter.incrementCount("PRE_GRAPH_NULL_MENTIONS");
            }
          }
          
          // If there are multiple candidates, try to determine the correct
          // entity before running the joint disambiguation according
          // to some heuristics.
          if (bestEntity == null && originalCandidateEntities.size() > 1) {
            if (bestEntity == null) {
              bestEntity = 
                  doConfidenceThresholdCheck(currentMention, normalizedEntityLocalSims);
              if (bestEntity != null) {
                GraphTracer.gTracer.addMentionToConfidenceThresh(
                    docId, currentMention.getMention(), currentMention.getCharOffset());  
                ++solvedByConfidenceThresholdHeuristic;
                Counter.incrementCount("MENTIONS_BY_CONFIDENCE_THRESHOLD_HEURISTIC");
              }
            } 
            
            if (bestEntity == null) {
              bestEntity = doEasyMentionsCheck(currentMention);
              if (bestEntity != null) {
                ++solvedByEasyMentionsHeuristic;
                Counter.incrementCount("MENTIONS_BY_EASY_MENTIONS_HEURISTIC");
                GraphTracer.gTracer.addMentionToEasy(
                    docId, currentMention.getMention(), currentMention.getCharOffset());
              }
            }  
            
            if (bestEntity == null) {
              bestEntity = doCoherenceRobustnessCheck(
                  currentMention, mentionL1s, isNamedEntity);
              if (bestEntity != null) {
                ++solvedByCoherenceRobustnessHeuristic;
                Counter.incrementCount("MENTIONS_BY_COHERENCE_ROBUSTNESS_HEURISTIC");
                GraphTracer.gTracer.addMentionToLocalOnly(
                    docId, currentMention.getMention(), currentMention.getCharOffset());          
              }
            }                           
          }
          if (bestEntity != null) {
            Entities candidates = new Entities();
            candidates.add(bestEntity);
            currentMention.setCandidateEntities(candidates);
            allEntitiesTemp.add(bestEntity);
          } else {
            // If all heuristics failed, prune candidates.
            Entities candidates = pruneCandidates(currentMention);
            if (candidates != null) {
              allEntitiesTemp.addAll(candidates);
              currentMention.setCandidateEntities(candidates);
              ++prunedMentionsCount;
              Counter.incrementCount("MENTIONS_PRUNED");
              GraphTracer.gTracer.addMentionToPruned(
                  docId, currentMention.getMention(), currentMention.getCharOffset());  
            } else {
              // Nothing changed from any heuristic/pruning.
              allEntitiesTemp.addAll(originalCandidateEntities);
            }
          }
        }
      }
    }
    
    if (!(GraphTracer.gTracer instanceof NullGraphTracer)) {
      gatherL1stats(docId, mentionL1s);
      GraphTracer.gTracer.addStat(
          docId, "Number of fixed mention by coherence robustness check", 
          Integer.toString(solvedByCoherenceRobustnessHeuristic));
      GraphTracer.gTracer.addStat(
          docId, "Number of fixed mention by confidence threshold check", 
          Integer.toString(solvedByConfidenceThresholdHeuristic));
      GraphTracer.gTracer.addStat(
          docId, "Number of fixed mention by easy mentions check", 
          Integer.toString(solvedByEasyMentionsHeuristic));
      GraphTracer.gTracer.addStat(
          docId, "Number of mentions with pruned candidates", 
          Integer.toString(prunedMentionsCount));
      GraphTracer.gTracer.addStat(
          docId, "Number of mentions set to null before running the algorithm",
          Integer.toString(preGraphNullMentions));
    }
    
    RunningTimer.recordEndTime("CalcSim(AndRobustness)ForCandidateEntities", timer);
    RunningTimer.recordEndTime("GG-LocalSimilarityCompute", id);
    
    allEntitiesResults.addAll(allEntitiesTemp);
    for (Map<Integer, Mention> innerMap : inputMentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (outputMentions.containsOffsetAndLength(m.getCharOffset(), m.getCharLength())) {
          Mention currentMention =  outputMentions.getMentionForOffsetAndLength(m.getCharOffset(), m.getCharLength());
          Entities previousCandidates = currentMention.getCandidateEntities();
          for (Entity e:m.getCandidateEntities()) {
            // If the (mention,entity) pair is both Named Entity and Concept do a average on similarities.
            if (previousCandidates.contains(e.getId())) {
              double sim1 = previousCandidates.getEntityById(e.getId()).getMentionEntitySimilarity();
              double sim2 = e.getMentionEntitySimilarity();
              e.setMentionEntitySimilarity((sim1 + sim2)/2);
            }
            currentMention.addCandidateEntity(e);
          }
        }
        else {
          outputMentions.addMention(m);
        }
      }
    }
    logger.debug("Entities Filled " + isNamedEntity);
  }

  /**
   * Checks if the coherence robustness check fires. If local sim and prior
   * agree on an entity, use it.
   *
   * @param mentionL1s
   * @param currentMention
   * @param isNamedEntity 
   * @return best candidate or null if check failed.
   */
  private Entity doCoherenceRobustnessCheck(Mention currentMention, Map<Mention, Double> mentionL1s, boolean isNamedEntity) {
    Entity bestCandidate = null;
    if (settings.getGraphSettings().shouldUseCoherenceRobustnessTest(isNamedEntity)) {
      if (mentionL1s.containsKey(currentMention) &&
          DoubleMath.fuzzyCompare(mentionL1s.get(currentMention),
          settings.getGraphSettings().getCohRobustnessThreshold(isNamedEntity), EPSILON) < 0) {
        bestCandidate = getBestCandidate(currentMention);
      }
    }
    return bestCandidate;
  }

  /**
   * Checks if the confidence of a mention disambiguation by local sim alone is 
   * high enough to fix it.
   *
   * @param mention
   * @return best candidate or null if check failed.
   */
  private Entity doConfidenceThresholdCheck(Mention mention, TIntDoubleHashMap normalizedEntityLocalSims) {
    Entity bestEntity = null;
    if (settings.getGraphSettings().shouldUseConfidenceThresholdTest()) {
      double max = CollectionUtils.getMaxValue(normalizedEntityLocalSims);
      if (DoubleMath.fuzzyCompare(max, settings.getGraphSettings().getConfidenceTestThreshold(), EPSILON) > 0) {
        bestEntity = getBestCandidate(mention);
      }
    }
    return bestEntity;
  }

  /**
   * For mentions with less than K candidates, assume that local similarity
   * is good enough to distinguish. K is given in graphSettings.
   *
   * @param mention
   * @return best candidate or null if check failed.
   */
  private Entity doEasyMentionsCheck(Mention mention) {
    Entity bestEntity = null;
    if (settings.getGraphSettings().shouldUseEasyMentionsTest()) {
      Entities candidates = mention.getCandidateEntities();
      if (candidates.size() < settings.getGraphSettings().getEasyMentionsTestThreshold()) {
        bestEntity = getBestCandidate(mention);
      }
    }
    return bestEntity;
  }

  /**
   * Prunes candidates, keeping only the top K elements for each mention. K is
   * set in graphSettings.
   *
   * @param mention
   *
   * @return
   */
  private Entities pruneCandidates(Mention mention) {
    Entities bestEntities = null;
    if (settings.getGraphSettings().shouldPruneCandidateEntities()) {
      int k = settings.getGraphSettings().getPruneCandidateThreshold();
      Entities candidates = mention.getCandidateEntities();
      if (candidates.size() > k) {
        Ordering<Entity> order = new Ordering<Entity>() {

          @Override public int compare(Entity e1, Entity e2) {
            return DoubleMath.fuzzyCompare(e1.getMentionEntitySimilarity(), e2.getMentionEntitySimilarity(), EPSILON);
          }
        };
        List<Entity> topEntities = order.greatestOf(candidates, k);
        bestEntities = new Entities();
        bestEntities.addAll(topEntities);
      }
    }
    return bestEntities;
  }

  private Map<Mention, Double> computeMentionPriorSimL1Distances(
      Mentions mentions, Entities allEntities, boolean isNamedEntity) throws Exception {
    logger.debug("Computing mention prior similarity L1 Distance.");
    // Precompute the l1 distances between each mentions
    // prior and keyphrase based similarity.
    Map<Mention, Double> l1s = new HashMap<Mention, Double>();
    Set<Mention> mentionObjects = new HashSet<>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        mentionObjects.add(m);
      }
    }    
    MaterializedPriorProbability pp = 
        new MaterializedPriorProbability(mentionObjects, isNamedEntity);
    EnsembleMentionEntitySimilarity keyphraseSimMeasure;
    if (isNamedEntity) {
      keyphraseSimMeasure = new EnsembleMentionEntitySimilarity(
          mentions, allEntities, context,
          new ExternalEntitiesContext(), settings.getGraphSettings().getCoherenceSimilaritySettingNE(),
      tracer, isNamedEntity);
    }
    else {
      throw new NoSuchMethodException("We don't have this for Concept disambiguagion now.");
//      keyphraseSimMeasure = new EnsembleMentionEntitySimilarity(
//          mentions, allEntities, context,
//          new ExternalEntitiesContext(), settings.getGraphSettings().getCoherenceSimilaritySettingC(),
//      tracer, isNamedEntity);
    }

    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        // get prior distribution
        TIntDoubleHashMap priorDistribution = calcPriorDistribution(mention, pp);
  
        // get similarity distribution per UnnormCOMB (IDF+MI)
        TIntDoubleHashMap simDistribution = calcSimDistribution(mention, keyphraseSimMeasure);

        // get L1 norm of both distributions, the graph algorithm can use
        // this for additional information. 
        // SOLVE_BY_LOCAL
        // otherwise, SOLVE_BY_COHERENCE
        double l1 = calcL1(priorDistribution, simDistribution);

        assert (l1 >= -0.00001 && l1 <= 2.00001) : "This cannot happen, L1 must be in [0,2]. Was '" + l1 + "' for mention: " + mention + ".";

        l1s.put(mention, l1);
      }
    }

    return l1s;
  }

  private void gatherL1stats(String docId, Map<Mention, Double> l1s) {
    double l1_total = 0.0;
    for (double l1 : l1s.values()) {
      l1_total += l1;
    }
    double l1_mean = l1_total / l1s.size();
    double varTemp = 0.0;
    for (double l1 : l1s.values()) {
      varTemp += Math.pow(Math.abs(l1_mean - l1), 2);
    }
    double variance = 0;
    if (l1s.size() > 1) {
      variance = varTemp / l1s.size();
    }
    GraphTracer.gTracer.addStat(docId, "L1 (prior-sim) Mean", Double.toString(l1_mean));
    GraphTracer.gTracer.addStat(docId, "L1 (prior-sim) StdDev", Double.toString(Math.sqrt(variance)));
  }

  private TIntDoubleHashMap calcPriorDistribution(Mention mention, MaterializedPriorProbability pp) {
    TIntDoubleHashMap priors = new TIntDoubleHashMap();

    for (Entity entity : mention.getCandidateEntities()) {
      priors.put(entity.getId(), pp.getPriorProbability(mention, entity));
    }

    return priors;
  }

  private TIntDoubleHashMap calcSimDistribution(Mention mention, EnsembleMentionEntitySimilarity combSimMeasure) throws Exception {
    TIntDoubleHashMap sims = new TIntDoubleHashMap();
    for (Entity e : mention.getCandidateEntities()) {
      sims.put(e.getId(), combSimMeasure.calcSimilarity(mention, context, e));
    }
    return CollectionUtils.normalizeValuesToSum(sims);
  }

  private double calcL1(TIntDoubleHashMap priorDistribution, TIntDoubleHashMap simDistribution) {
    double l1 = 0.0;

    for (TIntDoubleIterator itr = priorDistribution.iterator(); itr.hasNext(); ) {
      itr.advance();
      double prior = itr.value();
      double sim = simDistribution.get(itr.key());
      double diff = Math.abs(prior - sim);
      l1 += diff;
    }

    return l1;
  }

  private Entity getBestCandidate(Mention m) {
    double bestSim = Double.NEGATIVE_INFINITY;
    Entity bestCandidate = null;
    for (Entity e : m.getCandidateEntities()) {
      if (e.getMentionEntitySimilarity() > bestSim) {
        bestSim = e.getMentionEntitySimilarity();
        bestCandidate = e;
      }
    }
    return bestCandidate;
  }
}