package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class FixedLengthDocumentChunker extends DocumentChunker {

  private static Logger logger = LoggerFactory.getLogger(FixedLengthDocumentChunker.class);

  private int maxSentencesPerChunk;
  
  public FixedLengthDocumentChunker() {
    maxSentencesPerChunk = EntityLinkingConfig.getFixedChunkSize();
  }

  /**
   *
   * @param fixedLength Maximal number of sentences per chunk.
   */
  public FixedLengthDocumentChunker(int fixedLength) {
    maxSentencesPerChunk = fixedLength;
  }

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions) throws EntityLinkingDataAccessException {
    return process(docId, tokens, conceptMentions, namedEntityMentions, null);
  }

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions, Map<Integer, Map<Integer, ResultMention>> resultMentionMap) throws EntityLinkingDataAccessException {
    logger.debug("Processing Fixed Length Document Chunker for document {}.", docId);

    Integer runId = RunningTimer.recordStartTime("FixedLengthChunker");
    if (resultMentionMap == null) {
      resultMentionMap = new HashMap<>();
    }
    List<PreparedInputChunk> chunks = new ArrayList<PreparedInputChunk>(1);
    // 0-based sentence id returned by Stanford NLP
    int processedSentence = -1;
    int totalProcessedChunk = 0;
    PreparedInputChunk singleChunk = null;
    String chunkId = null;
    Tokens currentChunkTokens = new Tokens();
    Mentions currentChunkMentionsNamedEntity = new Mentions();
    Mentions currentChunkMentionsConcept = new Mentions();
    Map<Integer, Map<Integer, ResultMention>> currentResultMentions = new HashMap<>();
    
    for(Token token : tokens.getTokens()) {
      int currentTokenSentence = token.getSentence();
      if (currentTokenSentence != processedSentence) {
        if (processedSentence != -1 && currentTokenSentence != 0 && (currentTokenSentence % maxSentencesPerChunk) == 0) {
          chunkId = docId + "_" + (totalProcessedChunk++);
          singleChunk = new PreparedInputChunk(chunkId, currentChunkTokens, currentChunkMentionsConcept, currentChunkMentionsNamedEntity, currentResultMentions);
          chunks.add(singleChunk);
          currentChunkTokens = new Tokens();
          currentChunkMentionsConcept = new Mentions();
          currentChunkMentionsNamedEntity = new Mentions();
          currentResultMentions = new HashMap<>();
        }
        processedSentence = currentTokenSentence;
      }

      currentChunkTokens.addToken(token);
      if(conceptMentions.containsOffset(token.getBeginIndex())) {
        currentChunkMentionsConcept.addMentions(conceptMentions.getMentionsForOffset(token.getBeginIndex()));
      }
      if(namedEntityMentions.containsOffset(token.getBeginIndex())) {
        currentChunkMentionsNamedEntity.addMentions(namedEntityMentions.getMentionsForOffset(token.getBeginIndex()));
      }
      if (resultMentionMap.containsKey(token.getBeginIndex())) {
        currentResultMentions.put(token.getBeginIndex(), resultMentionMap.get(token.getBeginIndex()));
      }
    }

 // Need to add the last page processed to chunk list
    chunkId = docId + "_" + totalProcessedChunk;
    singleChunk = 
        new PreparedInputChunk(chunkId, currentChunkTokens, currentChunkMentionsConcept, currentChunkMentionsNamedEntity, currentResultMentions);
      chunks.add(singleChunk);
    
    PreparedInput preparedInput = new PreparedInput(docId, chunks);
    RunningTimer.recordEndTime("FixedLengthChunker", runId);
    return preparedInput;
  }
}
