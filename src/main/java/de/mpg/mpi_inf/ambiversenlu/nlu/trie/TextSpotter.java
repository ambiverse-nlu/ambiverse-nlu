package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.fst.FST;

import java.io.IOException;
import java.util.*;

public class TextSpotter {
  public static Set<Spot> spotTrieEntriesInTextIgnoreCase(FST<Long> trie, String text, Set<Integer> tokenBeginningPositionss,
                                                          Set<Integer> tokenEndingPositionss, double fuzzyMatchingRatio) throws IOException {
    return spotTrieEntriesInTextIgnoreCase(trie, text, tokenBeginningPositionss, tokenEndingPositionss, fuzzyMatchingRatio, false);
  }

  /**
   * The method search for items from the dictionary, inside the text.
   * @param trie The dictionary in a trie structure
   * @param text The text to be evaluated
   * @param tokenBeginningPositionss The begining positions of the tokens in the text
   * @param tokenEndingPositionss Ending character offset of the tokens
   * @param fuzzyMatchingRatio The matching tolerance (e.g., 1 means exact matching)
   * @return A set of spots containing, the end and begging position of the match plus the dictionary item
   * @throws IOException
   */
  public static Set<Spot> spotTrieEntriesInTextIgnoreCase(FST<Long> trie, String text, Set<Integer> tokenBeginningPositionss,
      Set<Integer> tokenEndingPositionss, double fuzzyMatchingRatio, boolean caseSensitive) throws IOException {

    String textToMatch = text;
    if(!caseSensitive) {
      textToMatch = text.toLowerCase();
    }

    final byte[] bytes = new BytesRef(textToMatch).bytes;
    List<FSTCursor> activeCursors = new ArrayList<>(1000);
    List<FSTCursor> finalCursors = new ArrayList<>(1000);
    List<FSTCursor> inactiveCursors = new ArrayList<>(1000);

    int character = -1;
    int skip = 0;
    for (int b = 0; b < bytes.length; b++) {
      int currentByte = bytes[b] & 0xFF;
      if (currentByte == 0) {
        break;
      }
      if (skip == 0) {
        character++;
        if (tokenBeginningPositionss.contains(character)) {
          FSTCursor newCursor = new FSTCursor(trie, character); //Starts a new cursor for every character
          activeCursors.add(newCursor);
        }
        skip = additionalBytesForCharacter(currentByte); //calculates how many bytes has the current character
      } else {
        skip--;
      }
      Iterator<FSTCursor> it = activeCursors.iterator();
      while (it.hasNext()) {
        FSTCursor cursor = it.next();
        boolean match = cursor.move(currentByte, character);
        if (match && tokenEndingPositionss.contains(character + 1) && cursor.isFinal()) {
          cursor.addSpot(character + 1); //If it is an exact match wrt tokens add.
          finalCursors.add(cursor);
        } else if (!match) {
//        } else if (!match && cursor.dicardContinueOrfuzzyMatching(character, fuzzyMatchingRatio) <= 0) {
          it.remove();
          inactiveCursors.add(cursor);
        }
      }
      if (tokenEndingPositionss.contains(character + 1)) { //If at the end of a token check for fuzzy matching or discard cursor
        it = inactiveCursors.iterator();
        while (it.hasNext()) {
          FSTCursor cursor = it.next();
//          if cursor already hit final, add it to final cursors
            if (cursor.hitFinal()) {
              finalCursors.add(cursor);

          } else {
            int nextStep = cursor.dicardContinueOrfuzzyMatching(character, fuzzyMatchingRatio);
            if (nextStep > 0) {
            cursor.addSpot(character + 1);

            finalCursors.add(cursor);
          }
        }

          it.remove();
        }
      }
    }
    for (FSTCursor cursor : activeCursors) { //Check if the active cursors have past hits
      if (cursor.hitFinal()) {
        finalCursors.add(cursor);
      }
    }
    Set<Spot> result = new HashSet<>();
    for (FSTCursor finalCursor : finalCursors) {
      result.addAll(finalCursor.getSpots());
    }
    return result;
  }

  private static int additionalBytesForCharacter(int currentByte) {
    if (currentByte < 192) {
      return 0;
    } else if (currentByte < 224) {
      return 1;
    } else if (currentByte < 240) {
      return 2;
    } else {
      return 3;
    }
  }
}