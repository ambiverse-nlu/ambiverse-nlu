package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PostgresUtil {
  public static String getPostgresEscapedString(String input) {
    return input.replace("'", "''").replace("\\", "\\\\");
  }

  public static String getPostgresEscapedConcatenatedQuery(Collection<String> entities) {
    List<String> queryTerms = new LinkedList<String>();

    for (String term : entities) {
      StringBuilder sb = new StringBuilder();
      sb.append("E'").append(PostgresUtil.getPostgresEscapedString(term)).append("'");
      queryTerms.add(sb.toString());
    }

    return StringUtils.join(queryTerms, ",");
  }

  public static String getIdQuery(TIntHashSet ids) {
    int[] conv = ids.toArray();
    return getIdQuery(conv);
  }

  public static String getIdQuery(int[] ids) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ids.length; ++i) {
      sb.append(ids[i]);
      if (i < ids.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }
}
