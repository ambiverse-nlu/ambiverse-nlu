package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import edu.stanford.nlp.ling.IndexedWord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**A dictionary stores a set of strings.
 *
 * @date $LastChangedDate: 2013-04-23 12:03:16 +0200 (Tue, 23 Apr 2013) $
 * @version $LastChangedRevision: 735 $ */
public class Dictionary {

  /** Stores the strings */
  public Set<String> words = new HashSet<String>();

  public Dictionary() {
  }

  public int size() {
    return words.size();
  }

  public boolean contains(String word) {
    return words.contains(word);
  }

  public boolean contains(IndexedWord word) {
    return words.contains(word.lemma());
  }

  /** Loads the dictionary out of an {@link InputStream}. Each line
   * of the original file should contain an entry to the dictionary */
  public void load(File file) throws IOException {
    Scanner in = new Scanner(file);
    while (in.hasNextLine()) {
      String line = in.nextLine().trim();
      if (line.length() > 0) { // treat everything else as comments
        if (Character.isLetter(line.charAt(0))) {
          words.add(line);
        }
      }
    }
  }

  public Set<String> words() {
    return words;
  }

}
