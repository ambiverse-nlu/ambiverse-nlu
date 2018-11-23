package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleChunkDocumentChunker extends DocumentChunker {

  private static Logger logger = LoggerFactory.getLogger(SingleChunkDocumentChunker.class);

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions) throws EntityLinkingDataAccessException {
    return process(docId, tokens, conceptMentions, namedEntityMentions, null);
  }

  @Override
  public PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions, Map<Integer, Map<Integer, ResultMention>> resultMentions)
      throws EntityLinkingDataAccessException {

    logger.debug("Processing single chunk document for document {}.", docId);

    String chunkId = docId + "_singlechunk";
    if (resultMentions == null) {
      resultMentions = new HashMap<>();
    }
    PreparedInputChunk singleChunk = 
        new PreparedInputChunk(chunkId, tokens, conceptMentions, namedEntityMentions,resultMentions);
         
    List<PreparedInputChunk> chunks = new ArrayList<PreparedInputChunk>(1);
    chunks.add(singleChunk);
    PreparedInput preparedInput = new PreparedInput(docId, chunks);
    return preparedInput;
  }

  @Override
  public String toString() {
    return "SingleChunk";
  }
}
