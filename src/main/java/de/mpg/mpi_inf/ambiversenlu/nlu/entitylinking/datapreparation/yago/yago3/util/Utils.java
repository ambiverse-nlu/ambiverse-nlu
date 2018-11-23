package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.textmanipulation.Whitespace;

import java.util.regex.Pattern;


public class Utils {

  public static Pattern cleaner = Pattern.compile("[\\x00-\\x1F]");


  /**
   * Determine if a token should be kept.
   *
   * @param tokenString
   * @return
   */
  public static boolean shouldKeepToken(String tokenString) {
    if (tokenString == null || tokenString.trim().isEmpty()) {
      return false;
    }
    if (Whitespace.isWhiteSpace(tokenString)) {
      return false;
    }
    if (cleaner.matcher(tokenString).find()) {
      return false;
    }
    if (tokenString.length() >= 100) {
      return false;
    }
    // Otherwise keep.
    return true;
  }

  public static boolean shouldKeepTokens(Tokens tokens) {
    for(Token token: tokens.getTokens()) {
      if(token.getOriginal().length() > 100) {
        return false;
      }
    }
    return true;
  }


}
