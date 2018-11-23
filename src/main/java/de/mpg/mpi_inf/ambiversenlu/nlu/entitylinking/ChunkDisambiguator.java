package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.disambiguationtechnique.LocalDisambiguation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms.CocktailParty;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms.CocktailPartySizeConstrained;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms.DisambiguationAlgorithm;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms.SimpleGreedy;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.lookup.EntityLookupManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * Main class for running the disambiguation. Is thread-safe and can be
 * run in parallel.
 *
 */
public class ChunkDisambiguator implements Callable<ChunkDisambiguationResults> {

  private static final Logger logger_ = LoggerFactory.getLogger(ChunkDisambiguator.class);

  private final PreparedInputChunk input_;

  private final ExternalEntitiesContext externalContext_;

  private final DisambiguationSettings settings_;

  private final Tracer tracer_;

  private final EntityLookupManager entityLookupMgr;

  public ChunkDisambiguator(PreparedInputChunk input, 
      ExternalEntitiesContext eec, DisambiguationSettings settings,
      Tracer tracer) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    this.input_ = input;
    this.externalContext_ = eec;
    this.settings_ = settings;
    this.tracer_ = tracer;
    this.entityLookupMgr = new EntityLookupManager(settings.getLanguageSettings());
  }

  public ChunkDisambiguationResults disambiguate() throws Exception {
    Integer timerId = RunningTimer.recordStartTime("ChunkDisambiguator");
    entityLookupMgr.fillInCandidateEntities(
        input_.getConceptMentions(),
        input_.getNamedEntityMentions(),
        externalContext_.getDictionary(),
        externalContext_.getBlacklistedEntities(),
        settings_.isIncludeNullAsEntityCandidate(),
        settings_.isIncludeContextMentions(),
        settings_.getMaxEntityRank(),
        settings_.getMaxCandidatesPerEntityByPrior(),
        settings_.isMentionLookupPrefix());

    Map<ResultMention, List<ResultEntity>> mentionMappings = null;
    DisambiguationAlgorithm da = null;
    switch (settings_.getDisambiguationTechnique()) {
      case LOCAL:
        da = new LocalDisambiguation(input_, externalContext_, settings_, tracer_);
        break;
      case GRAPH:
        switch (settings_.getDisambiguationAlgorithm()) {
          case COCKTAIL_PARTY:
            da = new CocktailParty(input_, settings_, tracer_);
            break;
          case COCKTAIL_PARTY_SIZE_CONSTRAINED:
            logger_.debug("Creating CocktailPartySizeConstrained algorithm.");
            da = new CocktailPartySizeConstrained(input_, settings_, tracer_);
            break;
          case SIMPLE_GREEDY:
            da = new SimpleGreedy(input_, settings_, tracer_);
            break;
//          case RANDOM_WALK:
//            da = new RandomWalk(input_, settings_, tracer_);
//            break;
          default:
            throw new IllegalArgumentException("Unsupported graph algorithm.");
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported disambiguation technique.");
    }
    mentionMappings = da.disambiguate();
    RunningTimer.recordEndTime("ChunkDisambiguator", timerId);

    if (mentionMappings == null) {
      mentionMappings = new HashMap<>();
    }
    if (settings_.isIncludeNullAsEntityCandidate()) {
      adjustOokbeEntityNames(mentionMappings);
    }

    // do the tracing
    String tracerHtml = null;  //tracer.getHtmlOutput();
    GraphTracer.TracingTarget target = settings_.getTracingTarget();

    if (GraphTracer.gTracer.canGenerateHtmlFor(input_.getChunkId())) {
      tracerHtml = GraphTracer.gTracer.generateHtml(input_.getChunkId(), target);
      GraphTracer.gTracer.removeDocId(input_.getChunkId());
    } else if (GraphTracer.gTracer.canGenerateHtmlFor(Integer.toString(input_.getChunkId().hashCode()))) {
      tracerHtml = GraphTracer.gTracer.generateHtml(Integer.toString(input_.getChunkId().hashCode()), target);
      GraphTracer.gTracer.removeDocId(Integer.toString(input_.getChunkId().hashCode()));
    }

    ChunkDisambiguationResults disambiguationResults = new ChunkDisambiguationResults(mentionMappings, tracerHtml);

    if (settings_.getNullMappingThreshold() >= 0.0) {
      double threshold = settings_.getNullMappingThreshold();
      logger_.debug("Dropping all entities below the score threshold of " + threshold);

      // drop anything below the threshold
      for (ResultMention rm : disambiguationResults.getResultMentions()) {
        double score = rm.getBestEntity().getScore();

        if (score < threshold) {
          logger_.debug("Dropping entity:" + rm.getBestEntity() + " for mention:" + rm);
          List<ResultEntity> nme = new ArrayList<ResultEntity>(1);
          nme.add(ResultEntity.getNoMatchingEntity());
          rm.setResultEntities(nme);
        }
      }
    }

    return disambiguationResults;
  }

  private void adjustOokbeEntityNames(Map<ResultMention, List<ResultEntity>> solutions) {
    // Replace name-OOKBE placeholders by plain OOKBE placeholders.
    Map<ResultMention, List<ResultEntity>> nmeCleanedResults = new HashMap<ResultMention, List<ResultEntity>>();

    for (Entry<ResultMention, List<ResultEntity>> e : solutions.entrySet()) {
      if (Entities.isOokbeName(e.getValue().get(0).getEntity())) {
        List<ResultEntity> nme = new ArrayList<ResultEntity>(1);
        nme.add(ResultEntity.getNoMatchingEntity());
        nmeCleanedResults.put(e.getKey(), nme);
      } else {
        nmeCleanedResults.put(e.getKey(), e.getValue());
      }
    }
  }

  @Override public ChunkDisambiguationResults call() throws Exception {
    ChunkDisambiguationResults result = disambiguate();
    return result;
  }
}