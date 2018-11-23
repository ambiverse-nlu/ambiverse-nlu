package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over a file, line by line. It is not thread-safe. 
 */
public class FileEntriesIterator implements Iterator<String> {

  private Logger logger = LoggerFactory.getLogger(FileEntriesIterator.class);

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private BufferedReader reader_;

  private int maxLineLengthForMark_;

  private int progressAfterLines_ = 100000;

  private long currentByteLength_ = 0;

  private long currentFileLine_ = 0;

  private NumberFormat percentFormat = NumberFormat.getPercentInstance();

  private String lastReadLineInHasNext;

  private boolean done = false;

  public FileEntriesIterator(File tsvFile, int progressAfterLines) throws UnsupportedEncodingException, FileNotFoundException {
    this(tsvFile, progressAfterLines, 10000);
  }

  public FileEntriesIterator(File tsvFile, int progressAfterLines, int maxLineLength) throws UnsupportedEncodingException, FileNotFoundException {
    this(new FileInputStream(tsvFile), DEFAULT_CHARSET, progressAfterLines, maxLineLength);
  }

  public FileEntriesIterator(InputStream inputStream, int progressAfterLines) throws UnsupportedEncodingException {
    this(inputStream, DEFAULT_CHARSET, progressAfterLines, 10000);
  }

  public FileEntriesIterator(InputStream inputStream, Charset cs, int progressAfterLines, int maxLineLength) throws UnsupportedEncodingException {
    reader_ = new BufferedReader(new InputStreamReader(inputStream, cs));
    progressAfterLines_ = progressAfterLines;
    maxLineLengthForMark_ = maxLineLength;
  }

  @Override public boolean hasNext() {
    if (done) {
      return false;
    }
    try {
      reader_.mark(maxLineLengthForMark_);
      lastReadLineInHasNext = reader_.readLine();
      boolean hasNext = (lastReadLineInHasNext != null);
      if (hasNext) {
        reader_.reset();
      } else {
        // Close the reader, not used anymore.
        reader_.close();
        done = true;
      }
      return hasNext;
    } catch (IOException e) {
      logger.error("IOException: " + e.getLocalizedMessage() + ". " + "This might happen when lines are too long, exceeding " + maxLineLengthForMark_
          + " characters. Try increasing it.");
      try {
        reader_.close();
      } catch (IOException e2) {
        logger.error("Failed to close reader: " + e2.getLocalizedMessage());
      }
      return false;
    }
  }

  @Override public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    } else {
      String line;
      try {
        line = reader_.readLine();

        if (progressAfterLines_ != 0) {
          currentByteLength_ += line.length();

          // Post progress every few lines.
          if (++currentFileLine_ % progressAfterLines_ == 0) {
            float progress = currentByteLength_;
            logger.info("Read " + progress + " bytes (" + currentFileLine_ + " lines)");
          }
        }
        return line;
      } catch (IOException e) {
        throw new NoSuchElementException(e.getLocalizedMessage());
      }
    }
  }

  public String peek() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    } else {
      return lastReadLineInHasNext;
    }
  }

  @Override public void remove() {
    throw new UnsupportedOperationException();
  }
}