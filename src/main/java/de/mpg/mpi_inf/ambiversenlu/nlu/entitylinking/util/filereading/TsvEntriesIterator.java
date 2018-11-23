package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TsvEntriesIterator implements Iterator<String[]> {

  private FileEntriesIterator internalIterator_;

  public TsvEntriesIterator(InputStream inputStream, Charset cs_, int progressAfterLines) throws UnsupportedEncodingException, FileNotFoundException {
    internalIterator_ = new FileEntriesIterator(inputStream, cs_, progressAfterLines, 100000);
  }

  @Override public boolean hasNext() {
    return internalIterator_.hasNext();
  }

  @Override public String[] next() {
    return internalIterator_.next().split("\t");
  }

  @Override public void remove() {
    throw new NoSuchElementException();
  }
}
