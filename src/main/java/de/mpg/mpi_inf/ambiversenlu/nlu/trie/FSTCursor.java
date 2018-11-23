package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import com.google.common.math.DoubleMath;
import org.apache.lucene.util.fst.FST;

import java.io.IOException;
import java.util.*;

public class FSTCursor {

  private FST<Long> fst;
  private Long output;
  private FST.Arc<Long> arc;
  private int characterStart; //The first character of this cursor
  private FST.BytesReader fstReader;
  private int matches = 0; //The number of character matches
  private int lastMatchedCharacter = -1; //The last character matched
  private Map<Long, Spot> outputTospots = new HashMap<>();

  public FSTCursor(FST<Long> fst, int characterStart) {
    this.fst = fst;
    arc = fst.getFirstArc(new FST.Arc<Long>());
    output = fst.outputs.getNoOutput();
    fstReader = fst.getBytesReader();
    this.characterStart = characterStart;
  }

  public long finalOutput() {
    return fst.outputs.add(output, arc.nextFinalOutput);
  }

  public boolean isFinal() {
    return arc.isFinal();
  }

  public boolean move(int label, int lastMatchedCharacter) throws IOException {
    if (fst.findTargetArc(label, arc, arc, fstReader) == null) {
      return false;
    }
    output = fst.outputs.add(output, arc.output);
    if(this.lastMatchedCharacter != lastMatchedCharacter) {
      this.lastMatchedCharacter = lastMatchedCharacter;
      matches++;
    }
    return true;
  }


  //Main function to decide how to proceed with fuzzy matching
  public int dicardContinueOrfuzzyMatching(int currentCharacterPosition, double minMatchingRatio) {
    int explored = currentCharacterPosition - characterStart + 1; //The number of characters explored so far
    double current_ratio = matches / (double) explored; //The ratio of hits
    if(matches < 3 || explored - matches > 3) { //If there is less that 2 hits or more than 3 misses
      return -1; //Discard
    }
    if(DoubleMath.fuzzyCompare(current_ratio, minMatchingRatio, 0.0001) >= 0) {
      return 1; //Include
    } else if(DoubleMath.fuzzyCompare(minMatchingRatio, 1, 0.0001) < 1
        && explored - matches <= 2) {
      return 1; //Include
    } else {
      return 0; //continue
    }
  }

  public int getCharacterStartStart() {
    return characterStart;
  }

  public int getMatches() {
    return matches;
  }

  public boolean hitFinal() {return !outputTospots.isEmpty();}

  public void addSpot(int characterEnd) {
    if(!outputTospots.containsKey(output)) {
      outputTospots.put(output, new Spot(characterStart, characterEnd, output));
    }
  }


  public Set<Spot> getSpots() {return Collections.unmodifiableSet(new HashSet<>(outputTospots.values()));}


}