package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions for Strings.
 */
public class StringUtils {

  public static final String BOUNDARY_CHAR = "_";

  /**
   * Returns all char ngrams in the string of the specified length. Contains boundary ngrams.
   *
   * For the string "a" and a length of 2, the ngrams "_a" and "a_" would be returned.
   *
   * @param s String to exrract ngrams from.
   * @param length  Length of the ngrams.
   * @return Set of all ngrams in the string, including boundary ngrams.
   */
  public static Set<String> getNgrams(String s, int length) {
    Set<String> ngrams = new HashSet<>();
    StringBuilder sb = new StringBuilder(3);
    for (int i = -length + 1; i < s.length(); ++i) {
      int j = i;
      // Fill up the beginning of the string with placeholder chars.
      while (j < 0) {
        sb.append(BOUNDARY_CHAR);
        ++j;
      }
      while (sb.length() < length && j < s.length()) {
        sb.append(s.charAt(j));
        ++j;
      }
      // Fill up the end of the string with placeholder chars.
      while (sb.length() < length) {
        sb.append(BOUNDARY_CHAR);
        ++j;
      }
      ngrams.add(sb.toString());
      sb.setLength(0);
    }
    return ngrams;
  }

  /**
   * Returns all token ngrams in the string array of the specified length.
   * If the input array is smaller than the requested length, the array itself will be returned
   *
   *
   * @param s String array to exrract tngrams from.
   * @param length  Length of the ngrams.
   * @return Set of all ngrams in the string, or the input elements themselves if the input is smaller than the requested length.
   */
  public static Set<String[]> getNgrams(String[] s, int length) {
    Set<String[]> ngrams = new HashSet<>();

    // Trivial case if the input is too short.
    if (s.length <= length) {
      ngrams.add(s);
      return ngrams;
    }

    // Generate the ngrams.
    for (int i = 0; i <= s.length - length; ++i) {
      String[] ngram = new String[length];

      // Start in input.
      int j = i;

      // Start in new ngram.
      int k = 0;

      while (k < length && j < s.length) {
        ngram[k] = s[j];
        ++j;
        ++k;
      }

      ngrams.add(ngram);
    }
    return ngrams;
  }


  public static String URLify(String str, String prefix) {
    char[] char_str = str.toCharArray();
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < char_str.length; i++) {
      if(char_str[i] == ' ') {
        sb.append("%20");
      } else {
        sb.append(char_str[i]);
      }
    }
    return prefix + sb.toString();
  }
}
