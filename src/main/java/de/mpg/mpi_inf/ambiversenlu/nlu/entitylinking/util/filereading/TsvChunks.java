package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

/**
 * Iterates over a tsv file, returning chunks grouped by a column range. The 
 * default grouping is by the first column only.
 */
public class TsvChunks implements Iterable<Pair<String[], List<String[]>>> {

  private Logger logger = LoggerFactory.getLogger(TsvChunks.class);

  private File file_;

  private int[] groupingRange_;

  boolean useRangeForData_;

  private int progressAfterLines_;

  public TsvChunks(String filename) {
    init(new File(filename), new int[] { 0, 0 }, false, 1000000);
  }

  public TsvChunks(File file) {
    init(file, new int[] { 0, 0 }, false, 1000000);
  }

  /**
   * Read file chunk by chunk posting progress every progressAfterLines
   * lines. Default is after 100000. groupingRange is the start and end
   * indexes of the columns by which the tsv should be chunked. A new chunk
   * is returned whenever the column contents change (any of the columns). 
   *
   * @param file  File to read.
   * @param groupingRange Range of columns in TSV to group by, [start, end].
   * The end is included. Negative values will be interpreted as offsets
   * from the end of the line, -1 refers to the last element.
   * @param useRangeForData Set to true to use the range for the data instead
   * of the grouping columns. Default is false.
   * @param progressAfterLines  Gap between progress posting.
   * Set to 0 to disable.
   */
  public TsvChunks(File file, int[] groupingRange, boolean useRangeForData, int progressAfterLines) {
    init(file, groupingRange, useRangeForData, progressAfterLines);
  }

  public void init(File file, int[] groupingRange, boolean useRangeForData, int progressAfterLines) {
    file_ = file;
    groupingRange_ = groupingRange;
    useRangeForData_ = useRangeForData;
    progressAfterLines_ = progressAfterLines;
  }

  @Override public Iterator<Pair<String[], List<String[]>>> iterator() {
    try {
      return new TsvChunksIterator(file_, groupingRange_, useRangeForData_, progressAfterLines_);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported Encoding: " + e.getLocalizedMessage());
      return null;
    } catch (FileNotFoundException e) {
      logger.error("File not found: " + e.getLocalizedMessage());
      return null;
    }
  }
}
