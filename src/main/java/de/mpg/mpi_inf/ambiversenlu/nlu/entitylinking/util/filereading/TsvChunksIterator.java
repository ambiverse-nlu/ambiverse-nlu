package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.TsvUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class TsvChunksIterator implements Iterator<Pair<String[], List<String[]>>> {

  private FileEntriesIterator internalIterator_;

  /**
   * Range to group by, as [start,end) (end is excluded).
   */
  private int[] range_;

  private boolean useRangeForData_;

  private String[] currentGroupingElement_;

  private List<String[]> currentChunkData_;

  public TsvChunksIterator(File tsvFile, int[] groupingRange, boolean useRangeForData, int progressAfterLines)
      throws UnsupportedEncodingException, FileNotFoundException {
    internalIterator_ = new FileEntriesIterator(tsvFile, progressAfterLines, 100000);
    range_ = groupingRange;
    useRangeForData_ = useRangeForData;

    // Initialize with first line content.
    if (hasNext()) {
      currentChunkData_ = new LinkedList<String[]>();
      String[] tmpLine = internalIterator_.peek().split("\t");
      currentGroupingElement_ = getGroupingElement(tmpLine, range_);
    }
  }

  @Override public boolean hasNext() {
    return internalIterator_.hasNext();
  }

  @Override public Pair<String[], List<String[]>> next() {
    // Iterate over all single entries, return as chunk.
    while (internalIterator_.hasNext()) {
      String[] data = internalIterator_.peek().split("\t");
      String[] groupingElement = getGroupingElement(data, range_);
      if (checkGroupingElementChanged(currentGroupingElement_, groupingElement)) {
        String[] key = Arrays.copyOf(currentGroupingElement_, currentGroupingElement_.length);
        List<String[]> value = new ArrayList<String[]>(currentChunkData_.size());
        for (String[] v : currentChunkData_) {
          value.add(v);
        }
        Pair<String[], List<String[]>> chunk = new Pair<String[], List<String[]>>(key, value);
        currentGroupingElement_ = groupingElement;
        currentChunkData_ = new LinkedList<String[]>();
        return chunk;
      } else {
        currentChunkData_.add(getDataElements(data, range_));
        internalIterator_.next().split("\t");
      }
    }

    // Return the last chunk.
    return new Pair<String[], List<String[]>>(currentGroupingElement_, currentChunkData_);
  }

  private String[] getGroupingElement(String[] entries, int[] r) {
    if (useRangeForData_) {
      return TsvUtils.getElementsNotInRange(entries, r);
    } else {
      return TsvUtils.getElementsInRange(entries, r);
    }
  }

  private String[] getDataElements(String[] entries, int[] r) {
    if (useRangeForData_) {
      return TsvUtils.getElementsInRange(entries, r);
    } else {
      return TsvUtils.getElementsNotInRange(entries, r);
    }
  }

  private boolean checkGroupingElementChanged(String[] current, String[] next) {
    return !Arrays.equals(current, next);
  }

  @Override public void remove() {
    throw new NoSuchElementException();
  }
}
