package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.disambiguationtechnique;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms.DisambiguationAlgorithm;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleMentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public class LocalDisambiguation extends DisambiguationAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(LocalDisambiguation.class);

  protected SimilaritySettings ssNE;
  protected SimilaritySettings ssC;

  protected String chunkId;

  protected boolean includeNullAsEntityCandidate;

  protected boolean computeConfidence;

  private NumberFormat nf;

  public LocalDisambiguation(PreparedInputChunk input, ExternalEntitiesContext externalContext, DisambiguationSettings settings, Tracer tracer) {
    super(input, externalContext, settings, tracer);
    nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    nf.setMaximumFractionDigits(2);
    logger.debug("Preparing '" + input.getChunkId() + "' (" +
        input.getNamedEntityMentions().getMentions().size() + " NE mentions - " +
        input.getConceptMentions().getMentions().size() + " C mentions - " + ")");

    this.ssNE = settings.getSimilaritySettings(true);
    this.ssC = settings.getSimilaritySettings(false);
    this.chunkId = input.getChunkId();
    this.includeNullAsEntityCandidate = settings.isIncludeNullAsEntityCandidate();
    this.computeConfidence = settings.shouldComputeConfidence();
//    this.isNamedEntity = isNamedEntity;

    logger.debug("Finished preparing '" + input.getChunkId() + "'");
  }

  @Override public Map<ResultMention, List<ResultEntity>> disambiguate() throws Exception {
    Map<ResultMention, List<ResultEntity>> solutions = new HashMap<>();
    EnsembleMentionEntitySimilarity mes1 = prepapreMES(input_.getNamedEntityMentions(), ssNE, true);
    disambiguate(input_.getNamedEntityMentions(), mes1, solutions);
    
    EnsembleMentionEntitySimilarity mes2 = prepapreMES(input_.getConceptMentions(), ssC, false);
    disambiguate(input_.getConceptMentions(), mes2, solutions);
    
    return solutions;
  }

  private EnsembleMentionEntitySimilarity prepapreMES(Mentions mentions, SimilaritySettings ss, boolean isNE) throws Exception {
    Entities entities = EntityLinkingManager.getAllEntities(mentions, externalContext_, tracer_);

    if (includeNullAsEntityCandidate) {
      entities.setIncludesOokbeEntities(true);
    }

    return new EnsembleMentionEntitySimilarity(mentions, entities, input_.getContext(), externalContext_, ss, tracer_, isNE);
  }

  protected void disambiguate(Mentions mentions, EnsembleMentionEntitySimilarity mes, Map<ResultMention, List<ResultEntity>> solutions) throws Exception {
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        List<ResultEntity> entities = new LinkedList<>();
    
        // Compute all scores.
        Map<KBIdentifiedEntity, Double> entityScores = new HashMap<>();
        for (Entity entity : mention.getCandidateEntities()) {
          double sim = mes.calcSimilarity(mention, input_.getContext(), entity);
          entityScores.put(entity.getKbIdentifiedEntity(), sim);
        }
    
        if (computeConfidence) {
          // Normalize similarities so that they sum up to one. The mass of the
          // score that the best entity accumulates will also be a measure of the
          // confidence that the mapping is correct.
          entityScores = CollectionUtils.normalizeValuesToSum(entityScores);
        }
    
        // Create ResultEntities.
        for (Entry<KBIdentifiedEntity, Double> e : entityScores.entrySet()) {
          entities.add(new ResultEntity(e.getKey().getIdentifier(), e.getKey().getKnowledgebase(), e.getValue()));
        }
    
        // Distinguish a the cases of empty, unambiguous, and ambiguous mentions.
        if (entities.isEmpty()) {
          // Assume a 95% confidence, as the coverage of names of the dictionary
          // is quite good.
          ResultEntity re = ResultEntity.getNoMatchingEntity();
          if (computeConfidence) {
            re.setScore(0.95);
          }
          entities.add(re);
        } else if (entities.size() == 1 && computeConfidence) {
          // Do not give full confidence to unambiguous mentions, as there might
          // be meanings missing.
          entities.get(0).setScore(0.95);
        }
    
        // Sort the candidates by their score.
        Collections.sort(entities);
    
        // Fill solutions.
        ResultMention rm = new ResultMention(mention.getMention(), mention.getCharOffset(), mention.getCharLength());
        solutions.put(rm, entities);
      }
    }
  }
}
