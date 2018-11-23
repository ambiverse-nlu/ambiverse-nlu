package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the input document as context representation.
 */
public class Context implements Serializable {

  private static Logger logger = LoggerFactory.getLogger(Context.class);

  public static final int UNKNOWN_TOKEN_ID = 0;

  private List<String> tokenStrings_;

  private int[] tokenIds_;

  public Context(Tokens tokens) throws EntityLinkingDataAccessException {
    logger.debug("Creating new Context.");
    List<String> ts = new ArrayList<>(tokens.size());
    for (Token token : tokens) {
      ts.add(token.getOriginal());
    }
    tokenStrings_ = new ArrayList<>(ts);
    logger.debug("Get Ids for Words.");
    TObjectIntHashMap<String> token2ids = DataAccess.getIdsForWords(tokenStrings_);
    tokenIds_ = new int[tokens.size()];
    for (int i = 0; i < tokens.size(); ++i) {
      String token = tokenStrings_.get(i);
      int tokenId = token2ids.get(token);
      if (tokenId == token2ids.getNoEntryValue()) {
        tokenId = tokens.getIdForTransientToken(token);
      }
      tokenIds_[i] = tokenId;
    }
  }

  public List<String> getTokens() {
    return tokenStrings_;
  }

  public int[] getTokenIds() {
    return tokenIds_;
  }

  public int getTokenCount() {
    return tokenIds_.length;
  }
}
