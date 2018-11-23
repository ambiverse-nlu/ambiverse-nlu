package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageBasedDocumentChunker extends DocumentChunker {

  private static Logger logger = LoggerFactory.getLogger(PageBasedDocumentChunker.class);

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions) throws EntityLinkingDataAccessException {
    return process(docId, tokens, conceptMentions, namedEntityMentions, null);
  }

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions, 
      Map<Integer, Map<Integer, ResultMention>> resultMentionMap) throws EntityLinkingDataAccessException {

    logger.debug("Processing Page based document chunker for document {}.", docId);

    Integer runId = RunningTimer.recordStartTime("PageBasedChuncker");
    if (resultMentionMap == null) {
      resultMentionMap = new HashMap<>();
    }
    List<PreparedInputChunk> chunks = new ArrayList<PreparedInputChunk>(1);
    Tokens pageTokens = null;
    Mentions pageMentionsNamedEntity = null;
    Mentions pageMentionsConcept = null;
    Map<Integer, Map<Integer, ResultMention>> pageResultMentions = null;
    int prevPageNum = -1;

    PreparedInputChunk singleChunk = null;
    String chunkId = null;
    for (Token token : tokens.getTokens()) {
      int pageNumber = token.getPageNumber();
      if (prevPageNum != pageNumber) {
        if (prevPageNum != -1) {
          chunkId = docId + "_" + prevPageNum;
          singleChunk = 
            new PreparedInputChunk(chunkId, pageTokens, conceptMentions, namedEntityMentions, pageResultMentions);
          chunks.add(singleChunk);
        }
        pageTokens = new Tokens();
        pageTokens.setPageNumber(pageNumber);
        pageMentionsConcept = new Mentions();
        pageMentionsNamedEntity = new Mentions();
        pageResultMentions = new HashMap<>();
      }

      pageTokens.addToken(token);
      if(conceptMentions.containsOffset(token.getBeginIndex())) {
        pageMentionsConcept.addMentions(conceptMentions.getMentionsForOffset(token.getBeginIndex()));
      }
      if(namedEntityMentions.containsOffset(token.getBeginIndex())) {
        pageMentionsNamedEntity.addMentions(namedEntityMentions.getMentionsForOffset(token.getBeginIndex()));
      }
      if (resultMentionMap.containsKey(token.getBeginIndex())) {
        pageResultMentions.put(token.getBeginIndex(), resultMentionMap.get(token.getBeginIndex()));
      }
      prevPageNum = pageNumber;
    }

    // Need to add the last page processed to chunk list
    chunkId = docId + "_" + prevPageNum;
    singleChunk = 
        new PreparedInputChunk(chunkId, pageTokens, pageMentionsConcept, pageMentionsNamedEntity, pageResultMentions);
      chunks.add(singleChunk);
    
    PreparedInput preparedInput = new PreparedInput(docId, chunks);
    RunningTimer.recordEndTime("PageBasedChuncker", runId);
    return preparedInput;
  }


}
