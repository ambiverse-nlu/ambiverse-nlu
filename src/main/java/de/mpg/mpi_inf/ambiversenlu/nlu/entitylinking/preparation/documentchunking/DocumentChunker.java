package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.PreparedInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public abstract class DocumentChunker {
  public abstract PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions, Map<Integer, Map<Integer, ResultMention>> resultMentionMap) throws EntityLinkingDataAccessException;
  public abstract PreparedInput process(String docId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions) throws EntityLinkingDataAccessException;


  private static Logger logger = LoggerFactory.getLogger(DocumentChunker.class);

  public String toString() {
    return this.getClass().getSimpleName();
  }

  public enum DOCUMENT_CHUNK_STRATEGY {
    SINGLE, PAGEBASED, MULTIPLE_FIXEDLENGTH
  }

  public static DocumentChunker getDocumentChunker(DOCUMENT_CHUNK_STRATEGY docChunkStrategy) {

    DocumentChunker chunker = null;
    switch (docChunkStrategy) {
      case SINGLE:
        chunker = new SingleChunkDocumentChunker();
        break;
      case PAGEBASED:
        chunker = new PageBasedDocumentChunker();
        break;
      case MULTIPLE_FIXEDLENGTH:
        chunker = new FixedLengthDocumentChunker();
        break;
    }
    logger.debug("Getting document chunker {}.", chunker);
    return chunker;
  }


}
