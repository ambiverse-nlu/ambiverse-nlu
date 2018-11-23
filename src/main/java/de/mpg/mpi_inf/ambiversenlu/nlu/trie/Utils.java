package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;

import java.io.IOException;

public class Utils {

  public static String getStringbyKey(Long output, FST trie) throws IOException {
    BytesRefBuilder scratchBytes = new BytesRefBuilder();
    IntsRef key = Util.getByOutput(trie, output);
    BytesRef bytes = Util.toBytesRef(key, scratchBytes);
    return bytes.utf8ToString();
  }

}
