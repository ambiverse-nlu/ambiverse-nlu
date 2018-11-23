package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class TrieBuilder {

  private static final Logger logger = LoggerFactory.getLogger(TrieBuilder.class);

  public static FST<Long> buildTrie(Set<String> sortedStrings) throws IOException {
    PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
    Builder<Long> builder = new Builder<Long>(FST.INPUT_TYPE.BYTE1, outputs);
    BytesRefBuilder scratchBytes = new BytesRefBuilder();
    IntsRefBuilder scratchInts = new IntsRefBuilder();
    long outputValue = 0;
    for (String mention : sortedStrings) {
      scratchBytes.copyChars(mention);
      try {
        builder.add(Util.toIntsRef(scratchBytes.get(), scratchInts), outputValue++);
      } catch (java.lang.AssertionError ae) {
        logger.debug("Assertion error for mention " + mention);
      }
    }
    return builder.finish();
  }
}
