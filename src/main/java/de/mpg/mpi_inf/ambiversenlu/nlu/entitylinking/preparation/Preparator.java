package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.DocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Preparator {
  private Logger logger_ = LoggerFactory.getLogger(Preparator.class);

  public PreparedInput prepareInputData(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions,
      DisambiguationSettings settings, DocumentChunker.DOCUMENT_CHUNK_STRATEGY chunkingStrategy) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    logger_.debug("Prepare input data.");
    return prepareInputData(docId, tokens, conceptMentions, namedEntityMentions, null, settings, chunkingStrategy);
  }
  
  public PreparedInput prepareInputData(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions,
      Map<Integer, Map<Integer, ResultMention>> resultMentionMap, DisambiguationSettings settings, 
      DocumentChunker.DOCUMENT_CHUNK_STRATEGY chunkingStrategy) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    
    logger_.debug("Preparing '" + docId + "'"); 
    
    Integer runningId = RunningTimer.recordStartTime("Preparator");
    long startTime = System.currentTimeMillis();
    
    // Drop mentions below min occurrence count.
    if (settings.getMinMentionOccurrenceCount() > 1) {
      dropMentionsBelowOccurrenceCount(conceptMentions, namedEntityMentions, settings.getMinMentionOccurrenceCount());
    }
    
    logger_.debug("Creating document chunker: " + chunkingStrategy);
    DocumentChunker chunker = DocumentChunker.getDocumentChunker(chunkingStrategy);

    logger_.debug("Creating prepared input.");
    PreparedInput preparedInput =
        chunker.process(docId, tokens, conceptMentions, namedEntityMentions, resultMentionMap);

    Type[] types = settings.getFilteringTypes();
    if (types != null) {
      Set<Type> filteringTypes = new HashSet<Type>(Arrays.asList(settings.getFilteringTypes()));
      preparedInput.setMentionEntitiesTypes(filteringTypes);
    }
    
    RunningTimer.recordEndTime("Preparator", runningId);
    double runTime = System.currentTimeMillis() - startTime;
    logger_.info("Document '" + docId + "' prepared in " + 
                runTime + "ms.");

    return preparedInput;
  }

  private static void dropMentionsBelowOccurrenceCount(Mentions conceptMentions, Mentions namedEntityMentions, int minMentionOccurrenceCount) {
    dropMentionsBelowOccurrenceCount(conceptMentions, minMentionOccurrenceCount);
    dropMentionsBelowOccurrenceCount(namedEntityMentions, minMentionOccurrenceCount);
  }

  public static void dropMentionsBelowOccurrenceCount(Mentions docMentions, int minMentionOccurrenceCount) {
    TObjectIntHashMap<String> mentionCounts = new TObjectIntHashMap<String>();
    for (Map<Integer, Mention> innerMap : docMentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        mentionCounts.adjustOrPutValue(m.getMention(), 1, 1);
      }
    }
    List<Mention> mentionsToRemove = new ArrayList<Mention>();
    for (Map<Integer, Mention> innerMap : docMentions.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if (mentionCounts.get(m.getMention()) < minMentionOccurrenceCount) {
          mentionsToRemove.add(m);
        }
      }
    }
    for (Mention m : mentionsToRemove) {
      docMentions.remove(m);
    }
  }
}
