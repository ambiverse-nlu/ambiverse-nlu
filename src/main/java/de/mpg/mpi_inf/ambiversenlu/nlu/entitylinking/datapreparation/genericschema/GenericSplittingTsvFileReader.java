package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.TsvReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Reads YAGO3 file from disk. Splits into separate relations on load - this
 * should save a lot of memory.
 */
public class GenericSplittingTsvFileReader implements GenericReader {

	private Logger logger = LoggerFactory.getLogger(GenericSplittingTsvFileReader.class);

	private final File fileLocation;

	private static final String SPLIT_PREFIX = "aidaFacts_split_GENERIC_";

	private boolean loaded = false;

	private Map<String, List<Fact>> cachedRelations = new HashMap<>();

	/** The maximum size of a relation that can be cached. */
	private static final int MAX_FACT_CACHE_COUNT = 10_000_000;

	/**
	 * The maximum number of times a relation should be read before checking for
	 * caching.
	 */
	private static final int MAX_READ_COUNT = 3;

	private TObjectIntHashMap<String> relationReadCount = new TObjectIntHashMap<>();

	public GenericSplittingTsvFileReader(File fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void splitYago3File(File yago3FileLocation) throws IOException {
		Map<String, BufferedWriter> writers = new HashMap<>();

		logger.info("Splitting file to single-relation files.");
		int read = 0;

		// Load original File line by line, write into separate files split by
		// relation.
		BufferedReader reader = FileUtils.getBufferedUTF8Reader(yago3FileLocation);
		for (Fact f : new TsvReader(reader)) {
			File relationFile = getFileForRelation(f.getRelation());

			BufferedWriter writer = writers.get(f.getRelation());
			if (writer == null) {
				// Check if file exists, if so, end the whole loop with a
				// message.
				if (relationFile.exists()) {
					logger.info("Relation file '" + relationFile + "' already exists, assuming splitting is finished.");
					break;
				}

				writer = FileUtils.getBufferedUTF8Writer(relationFile);
				writers.put(f.getRelation(), writer);
			}

			StringBuilder sb = new StringBuilder();
			if (f.getId() != null) {
				sb.append(f.getId());
			}
			sb.append("\t").append(f.getSubject()).append("\t").append(f.getRelation()).append("\t")
					.append(f.getObject());
			writer.write(sb.toString());
			writer.newLine();

			if (++read % 1_000_000 == 0) {
				logger.info("Split " + read + " facts. Last read relation was '" + f.getRelation() + "'.");
			}
		}

		// Close all writers.
		for (BufferedWriter writer : writers.values()) {
			writer.flush();
			writer.close();
		}
	}

	private File getFileForRelation(String relation) throws UnsupportedEncodingException {
		String fileName = URLEncoder.encode(relation, StandardCharsets.UTF_8.displayName());
		File yagoFileDir = fileLocation.getParentFile();
		File relationFile = new File(yagoFileDir, SPLIT_PREFIX + fileName);
		return relationFile;
	}

	@Override
	public synchronized List<Fact> getFacts(String relation) throws IOException {
		if (!loaded) {
			splitYago3File(fileLocation);
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

		if (relationReadCount.get(relation) >= MAX_READ_COUNT && facts.size() < MAX_FACT_CACHE_COUNT
				&& !cachedRelations.containsKey(relation)) {
			logger.info("Caching relation '" + relation + "' with " + facts.size() + " facts.");
			cachedRelations.put(relation, facts);
		}

		return facts;
	}

	@Override public Iterator<Fact> iterator(String relation) throws IOException {
		return getFacts(relation).iterator();
	}

	private List<Fact> readFacts(String relation) throws IOException {
		Relations relVal = Relations.names2Relations.get(relation);
		List<Fact> facts = new ArrayList<>(100_000);
		File relationFile = getFileForRelation(relation);
		if(!relationFile.exists()) {
			if(!relVal.optional) {
				throw new FileNotFoundException("Relation: " + relVal.name() + " is mandatory but was not present.");
			} else {
				return Collections.EMPTY_LIST;
			}
		}
		int read = 0;
		String s = "";
		try (BufferedReader reader = FileUtils.getBufferedUTF8Reader(relationFile)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split("\t");
				try {
					Fact f = new Fact(data[0], data[1], data[2], data[3], false);
					facts.add(f);
				} catch (Exception e) {
					logger.info(s);
				}
				if (++read % 1_000_000 == 0)
					logger.info("Read " + read + " facts for '" + relation + "'.");
				s = line;

			}
		}
		return facts;
	}
}
