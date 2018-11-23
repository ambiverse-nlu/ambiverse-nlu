package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Iterates over a tsv file, returns each line as String[] split on <TAB>.
 */
public class TsvEntries implements Iterable<String[]> {

  private static final int DEFAULT_PROGRESS = 100_000;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private Logger logger = LoggerFactory.getLogger(TsvEntries.class);

  private InputStream inputStream_;

  private Charset cs_;

  private int progressAfterLines_;

  public TsvEntries(String filename) throws FileNotFoundException {
    this(new File(filename));
  }

  public TsvEntries(File file) throws FileNotFoundException {
    this(new FileInputStream(file), DEFAULT_CHARSET, DEFAULT_PROGRESS);
  }

  public TsvEntries(InputStream inputStream) throws FileNotFoundException {
    this(inputStream, DEFAULT_CHARSET, DEFAULT_PROGRESS);
  }

  /**
   * Read file line by line, posting progress every progressAfterLines
   * lines. Default is after 100000.
   *
   * @param file  File to read.
   * @param progressAfterLines  Gap between progress posting. 
   * Set to 0 to disable.
   */
  public TsvEntries(File file, int progressAfterLines) throws FileNotFoundException {
    this(new FileInputStream(file), DEFAULT_CHARSET, progressAfterLines);
  }

  /**
   * Read file line by line, posting progress every progressAfterLines
   * lines. Default is after 100000.
   *
   * @param file  File to read.
   * @param cs  Character set of the file to be read.
   * Set to 0 to disable.
   */
  public TsvEntries(File file, Charset cs) throws FileNotFoundException {
    this(new FileInputStream(file), cs, DEFAULT_PROGRESS);
  }

  /**
   * Read file line by line, posting progress every progressAfterLines
   * lines. Default is after 100000.
   *
   * @param file  File to read.
   * @param cs  Character set of the file to be read.
   * @param progressAfterLines  Gap between progress posting.
   * Set to 0 to disable.
   */
  public TsvEntries(File file, Charset cs, int progressAfterLines) throws FileNotFoundException {
    this(new FileInputStream(file), Charset.forName("UTF-8"), progressAfterLines);
  }

  /**
   * Read inputStream line by line, posting progress every progressAfterLines
   * lines. Default is after 100000.
   *
   * @param inputStream  InputStream to read.
   * @param progressAfterLines  Gap between progress posting.
   * Set to 0 to disable.
   */
  public TsvEntries(InputStream inputStream, Charset cs, int progressAfterLines) {
    inputStream_ = inputStream;
    cs_ = cs;
    progressAfterLines_ = progressAfterLines;
  }

  @Override public Iterator<String[]> iterator() {
    try {
      return new TsvEntriesIterator(inputStream_, cs_, progressAfterLines_);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported Encoding: " + e.getLocalizedMessage());
      return null;
    } catch (FileNotFoundException e) {
      logger.error("File not found: " + e.getLocalizedMessage());
      return null;
    }
  }
}
