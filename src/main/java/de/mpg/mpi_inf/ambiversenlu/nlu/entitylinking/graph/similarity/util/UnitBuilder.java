package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util;

import gnu.trove.list.TIntList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class UnitBuilder {

  public static final String TOKEN_SEPERATOR = " ";

  public static String buildUnit(String[] unitTokens) {
    StringBuilder unitStringBuilder = new StringBuilder();
    for (String unitToken : unitTokens) {
      if (unitToken == null) continue;
      if (unitStringBuilder.length() != 0) unitStringBuilder.append(TOKEN_SEPERATOR);
      unitStringBuilder.append(unitToken);
    }
    return unitStringBuilder.toString();
  }

  public static String buildUnit(TIntList unitTokens, TIntObjectHashMap<String> id2word) {
    String[] tokenStrings = new String[unitTokens.size()];
    for (int i = 0; i < unitTokens.size(); i++) {
      tokenStrings[i] = id2word.get(unitTokens.get(i));
    }
    return buildUnit(tokenStrings);
  }
}
