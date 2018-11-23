package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading.TsvEntries;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.TsvReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Reads YAGO3 file from disk. Splits into separate relations on load - this should
 * save a lot of memory.
 */
public class YAGO3SplittingTsvFileReader implements YAGO3Reader {

  private Logger logger = LoggerFactory.getLogger(YAGO3SplittingTsvFileReader.class);

  private final File yago3FileLocation;

  private static final String SPLIT_PREFIX = "aidaFacts_split_YAGO3_";

  private static final String SPLIT_COUNTS = "aidaFacts_split_counts_YAGO3_.tsv";

  private static DecimalFormat percent = new DecimalFormat("###.##");
  private static DecimalFormat numbers = new DecimalFormat("###,###,###,###");

  private boolean loaded = false;

  private Map<String, List<Fact>> cachedRelations = new HashMap<>();

  /** The maximum size of a relation that can be cached. */
  private static final int MAX_FACT_CACHE_COUNT = 10_000_000;

  /** The maximum number of times a relation should be read before checking for caching. */
  private static final int MAX_READ_COUNT = 3;

  private TObjectIntHashMap<String> relationReadCount = new TObjectIntHashMap<>();

  public YAGO3SplittingTsvFileReader(File yago3FileLocation) {
    this.yago3FileLocation = yago3FileLocation;
  }

  public void splitYago3File(File yago3FileLocation) throws IOException {
    File splitCountsFile = getFactCountsFile();
    if (splitCountsFile.exists()) {
      logger.info("'" + splitCountsFile + "' already exists, assuming splitting is finished.");
      return;
    }

    Map<String, BufferedWriter> writers = new HashMap<>();

    logger.info("Splitting YAGO3 file to single-relation files.");
    int read = 0;

    TObjectIntHashMap<String> factCounts = new TObjectIntHashMap<>();

    // Load original file line by line, write into separate files split by relation.
    BufferedReader reader = FileUtils.getBufferedUTF8Reader(yago3FileLocation);
    for (Fact f : new TsvReader(reader)) {
      factCounts.adjustOrPutValue(f.getRelation(), 1, 1);

      File relationFile = getFileForRelation(f.getRelation());

      BufferedWriter writer = writers.get(f.getRelation());
      if (writer == null) {

        if (relationFile.exists()) {
          logger.warn("Relation file '" + relationFile + "' already exists, overwriting unfinished splitting.");
          relationFile.delete();
        }

        writer = FileUtils.getBufferedUTF8Writer(relationFile);
        writers.put(f.getRelation(), writer);
      }

      StringBuilder sb = new StringBuilder();
      if (f.getId() != null) {
        sb.append(f.getId());
      }
      sb.append("\t").append(f.getSubject()).append("\t").append(f.getRelation()).append("\t").append(f.getObject());
      writer.write(sb.toString());
      writer.newLine();

      if (++read % 10_000_000 == 0) {
        logger.info("Split " + read + " facts. Last read relation was '" + f.getRelation() + "'.");
      }
    }

    // Close all writers.
    for (BufferedWriter writer : writers.values()) {
      writer.flush();
      writer.close();
    }

    // Write split counts.
    BufferedWriter writer = FileUtils.getBufferedUTF8Writer(splitCountsFile);

    for (TObjectIntIterator<String> itr = factCounts.iterator(); itr.hasNext(); ) {
      itr.advance();
      writer.write(itr.key() + "\t" + itr.value());
      writer.newLine();
    }
    writer.flush();
    writer.close();
  }

  private File getFileForRelation(String relation) throws UnsupportedEncodingException {
    String fileName = URLEncoder.encode(relation, StandardCharsets.UTF_8.displayName());
    File yagoFileDir = yago3FileLocation.getParentFile();
    File relationFile = new File(yagoFileDir, SPLIT_PREFIX + fileName);
    return relationFile;
  }

  private int getFactCount(String relation) throws FileNotFoundException {
    for (String[] line : new TsvEntries(getFactCountsFile())) {
      if (line[0].equals(relation)) {
        return Integer.parseInt(line[1]);
      }
    }

    return 0;
  }

  private File getFactCountsFile() {
    return new File(yago3FileLocation.getParent(), SPLIT_COUNTS);
  }

  @Override public synchronized List<Fact> getFacts(String relation) throws IOException {
    if (!loaded) {
      splitYago3File(yago3FileLocation);
      loaded = true;
    }

    List<Fact> facts;

    if (cachedRelations.containsKey(relation)) {
      facts = cachedRelations.get(relation);
    } else {
      facts = readFacts(relation);
    }

    relationReadCount.adjustOrPutValue(relation, 1, 1);

    logger.info(relation + " read " + relationReadCount.get(relation) + " times.");

    if (relationReadCount.get(relation) >= MAX_READ_COUNT && facts.size() < MAX_FACT_CACHE_COUNT && !cachedRelations.containsKey(relation)) {
      logger.info("Caching relation '" + relation + "' with " + facts.size() + " facts.");
      cachedRelations.put(relation, facts);
    }

    return facts;
  }

  private List<Fact> readFacts(String relation) throws IOException {
    int factCount = getFactCount(relation);
    logger.info("Reading " + numbers.format(factCount) + " facts for " + relation);
    List<Fact> facts = new ArrayList<>(factCount);
    File relationFile = getFileForRelation(relation);
    if(loaded && !relationFile.exists()) {
      return Collections.emptyList();
    }
    int read = 0;
    try (BufferedReader reader = FileUtils.getBufferedUTF8Reader(relationFile)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] data = line.split("\t");
        Fact f = new Fact(data[0], data[1], data[2], data[3], false);
        facts.add(f);
        if (++read % 1_000_000 == 0) {
          double progress = (double) read / (double) factCount * 100;
          logger.info("Read " + percent.format(progress) + "% of facts for '" + relation + "'.");
        }
      }
    }
    return facts;
  }
}
