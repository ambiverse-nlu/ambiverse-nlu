package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public class PreparedInputChunk implements Serializable {

  private static Logger logger = LoggerFactory.getLogger(PreparedInputChunk.class);

  private String chunkId_;

  private Tokens tokens_;

  /** Used by the local similarity methods in the disambiguation. It holds
   * the document tokens both as strings and converted to word ids. */
  private Context context_;
  
  private Mentions namedEntityMentions_;
  private Mentions conceptMentions_;
      
  private Map<Integer, Map<Integer, ResultMention>> resultMentions_;
  
      
  public PreparedInputChunk(String chunkId) {
    chunkId_ = chunkId;
  }

  public PreparedInputChunk(String chunkId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions) throws EntityLinkingDataAccessException {
    this(chunkId, tokens, conceptMentions, namedEntityMentions, null);
  }
  
  public PreparedInputChunk(String chunkId, Tokens tokens, Mentions conceptMentions, Mentions namedEntityMentions, Map<Integer, Map<Integer, ResultMention>> currentResultMentions) throws EntityLinkingDataAccessException {
    logger.debug("Creating prepared input chunk for chunkId {}.", chunkId);
    chunkId_ = chunkId;
    tokens_ = tokens;
    conceptMentions_ = conceptMentions;
    namedEntityMentions_ = namedEntityMentions;
    context_ = createContextFromTokens(tokens);
    resultMentions_ = currentResultMentions;
  }
  public Tokens getTokens() {
    return tokens_;
  }

  public Mentions getNamedEntityMentions() {
    return namedEntityMentions_;
  }
  
  public Mentions getConceptMentions() {
    return conceptMentions_;
  }

  
  public Context getContext() {
    return context_;
  }

  private Context createContextFromTokens(Tokens t) throws EntityLinkingDataAccessException {
    logger.debug("Creating context form tokens.");
    return new Context(t);
  }

  public String getChunkId() {
    return chunkId_;
  }
  
  public Map<Integer, Map<Integer, ResultMention>> getResultMentions() {
    return resultMentions_;
  }
  
  public String[] getMentionContext(Mention m, int windowSize) {
    int start = Math.max(0, m.getStartToken() - windowSize);
    int end = Math.min(tokens_.size(), m.getEndToken() + windowSize);
    StringBuilder before = new StringBuilder();
    for (int i = start; i < m.getStartToken(); ++i) {
      before.append(tokens_.getToken(i).getOriginal()).append(" ");
    }
    StringBuilder after = new StringBuilder();
    for (int i = m.getEndToken() + 1; i < end; ++i) {
      after.append(tokens_.getToken(i).getOriginal()).append(" ");
    }
    return new String[] { before.toString(), after.toString() };
  }




}