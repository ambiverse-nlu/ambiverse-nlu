package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider.Source;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider.SourceProvider;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class WikiCorpusGenerator {
	private static final Logger logger = LoggerFactory.getLogger(WikiCorpusGenerator.class);
	private static final String WIKI_TOKENS_CACHE_PREFIX = "wikiTokensCache";
	private static final String WIKI_PARAGRAPHS_CACHE_PREFIX = "wikiParagraphsCache";
	private static final Pattern DOC_TITLE_PATTERN = Pattern.compile("-DOCSTART- \\((.*)\\)");
	private static final int N_THREADS = 13;
	private static final List<Double> DEFAULT_THRESHOLDS = Collections.singletonList(1.0);

	private final Set<String> seenDocTitlesPerThreshold;
	private final Map<String, List<List<int[]>>> wikiIDDocTokensCache;
	private final Map<Integer, String[]> entityIDDocParagraphCache;
	private final String language;
	private final String base;
	private final boolean appendToBase;
	private final List<Double> thresholds;
	private final CountDownLatch countDownLatch;
	private final Source source;

	private final String generatedFileName;
	private AtomicBoolean cachesStored = new AtomicBoolean(false);
	private AtomicBoolean documentsWritten = new AtomicBoolean(false);
	private AtomicBoolean shutdownHook = new AtomicBoolean(false);

	public static void main(String[] args) throws Throwable {
		new WikiCorpusGenerator(args).run();
	}

	public WikiCorpusGenerator(String[] args) throws IOException, ParseException, EntityLinkingDataAccessException {
		CommandLine cmd = new WikiCorpusGeneratorCommandLineUtils().parseCommandLineArgs(args);
		generatedFileName = cmd.getOptionValue("p");
		base = cmd.getOptionValue("b");
		appendToBase = cmd.hasOption("a");
		Set<String> seenDocTitles_ = new HashSet<>();
		if (base != null) {
			seenDocTitles_.addAll(extractSeenDocTitlesPerThreshold(base));
		}
		seenDocTitlesPerThreshold = new ConcurrentSkipListSet<>(seenDocTitles_);
		thresholds = cmd.hasOption("t")? new GsonBuilder().enableComplexMapKeySerialization().create()
				.fromJson(cmd.getOptionValue("t"), new TypeToken<List<Double>>() {}.getType()) : DEFAULT_THRESHOLDS;
		language = cmd.getOptionValue("l");
		source = SourceProvider.getSource(cmd.getOptionValue("s"), cmd.hasOption("m")? Integer.valueOf(cmd.getOptionValue("m")) : null, language);
		logger.info("Generating wiki corpus '" + generatedFileName + "' for language " + language + " with thresholds " + thresholds +
				(source.getType() == SourceProvider.Type.MANUAL? " for manual evaluation" : " from " + source.getType() +
						(!cmd.hasOption("m")? "" : " with " + cmd.getOptionValue("m") + " articles")));

		wikiIDDocTokensCache = new ConcurrentHashMap<>();
		String wikiTokensCacheJson = WIKI_TOKENS_CACHE_PREFIX + "_" + language + ".json";
		if (Files.exists(Paths.get(wikiTokensCacheJson))) {
			Map<String, List<List<int[]>>> docsCache0 = new GsonBuilder().enableComplexMapKeySerialization().create()
					.fromJson(new JsonReader(new FileReader(wikiTokensCacheJson)),
							new TypeToken<Map<String, List<List<int[]>>>>() {
							}.getType());

			if (docsCache0 != null) {
				wikiIDDocTokensCache.putAll(docsCache0);
			}
		}
		entityIDDocParagraphCache = new ConcurrentHashMap<>();
		String wikiParagraphsCache = WIKI_PARAGRAPHS_CACHE_PREFIX + "_" + language + ".json";
		if (Files.exists(Paths.get(wikiParagraphsCache))) {
			Map<Integer, String[]> paragraphCache0 = new GsonBuilder().enableComplexMapKeySerialization().create()
					.fromJson(new JsonReader(new InputStreamReader(new FileInputStream(wikiParagraphsCache), "UTF8")),
							new TypeToken<Map<Integer, String[]>>() {
							}.getType());

			if (paragraphCache0 != null) {
				entityIDDocParagraphCache.putAll(paragraphCache0);
			}
		}
		countDownLatch = new CountDownLatch(source.getTotalNumber() * thresholds.size());
	}

	private Collection<? extends String> extractSeenDocTitlesPerThreshold(String s) throws IOException {
		return Files.readAllLines(Paths.get(s))
				.stream()
				.filter(l -> l.contains("DOCSTART"))
				.map(l -> {
					Matcher matcher = DOC_TITLE_PATTERN.matcher(l);
					if (!matcher.matches()) {
						throw new RuntimeException("wrong doctitle: " + l);
					}

					return thresholds.stream().map(t-> matcher.group(1) + Double.toString(t));
				})
				.flatMap(x-> x)
				.collect(Collectors.toSet());

	}

	public void run() throws Throwable {
		logger.info("Starting Wiki corpus generation for language {} from source {}", language, source.getType());
		ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
		Map<Double, List<Future<List<String>>>> allFutures = new HashMap<>();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook operations");
				shutdownHook.set(true);
				while (countDownLatch.getCount() > 0) {
					countDownLatch.countDown();
				}
				if (!executor.isShutdown()) {
					executor.shutdown();
					try {
						if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
							executor.shutdownNow();
						}
					} catch (InterruptedException e) {
						executor.shutdownNow();
					}
				}
//				todo blocks!
				storeCaches();
				writeDocuments(allFutures);

			}
		});

		while (source.notEnoughEntities() && !shutdownHook.get()) {
			LinkedHashMap<Integer, String> originalNamedEntityIDs = source.retrieveOriginalNamedEntityIDs();

			for (double threshold : thresholds) {
				List<Future<List<String>>> futures = new ArrayList<>();
				for (Integer entityID : originalNamedEntityIDs.keySet()) {

					futures.add(executor.submit(new WikiCorpusTask(WikiCorpusGenerator.this, entityID,
							originalNamedEntityIDs.get(entityID), countDownLatch, threshold, source)));
				}
				if (!allFutures.containsKey(threshold)) {
					allFutures.put(threshold, futures);
				} else {
					allFutures.get(threshold).addAll(futures);
				}
			}

			countDownLatch.await();

		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}

		storeCaches();
		writeDocuments(allFutures);

		logger.info("WikiCorpusGenerator has finished the job");
	}

	synchronized private void writeDocuments(Map<Double, List<Future<List<String>>>> allFutures) {
		if (documentsWritten.get()) {
			return;
		}
		documentsWritten.set(true);
		logger.info("Writing documents");
		try {
			List<String> baseDocument = appendToBase? Files.readAllLines(Paths.get(base)) : Collections.EMPTY_LIST;
			for (Double milneWittenThreshold : allFutures.keySet()) {
				List<String> fullDocument = new ArrayList<>(baseDocument);
				for (Future<List<String>> future : allFutures.get(milneWittenThreshold)) {
					List<String> docText;
					if (future.isCancelled()) {
						docText = null;
					} else {
						try {
						docText = future.get(800, TimeUnit.MILLISECONDS);
						} catch (TimeoutException e) {
							docText = null;
						}
					}

					if (docText != null) {
						fullDocument.addAll(docText);
					}
				}
				String fileName = generatedFileName + (thresholds.size() > 1? milneWittenThreshold + ".tsv" : "");

				Files.write(Paths.get(fileName), fullDocument);
				logger.info("Document " + fileName + " has been written");
			}
		} catch (Exception e) {
			logger.error("Could not write documents!", e);
		}
	}

	synchronized private void storeCaches() {
		if (cachesStored.get()) {
			return;
		}
		cachesStored.set(true);
		logger.info("Storing the documents structure into docs cache");
		try {
			Gson gson = new Gson();
			if (!wikiIDDocTokensCache.isEmpty()) {
				Files.write(Paths.get(WIKI_TOKENS_CACHE_PREFIX + "_" + language + ".json"),
						gson.toJson(wikiIDDocTokensCache).getBytes());
			}
			if (!entityIDDocParagraphCache.isEmpty()) {
				Files.write(Paths.get(WIKI_PARAGRAPHS_CACHE_PREFIX + "_" + language + ".json"),
						gson.toJson(entityIDDocParagraphCache).getBytes("UTF8"));
			}
		} catch (IOException e) {
			logger.error("Could not store caches");
		}
	}

	Set<String> getSeenDocTitlesPerThreshold() {
		return seenDocTitlesPerThreshold;
	}

	Map<String, List<List<int[]>>> getWikiIDDocTokensCache() {
		return wikiIDDocTokensCache;
	}

	public Map<Integer, String[]> getEntityIDDocParagraphCache() {
		return entityIDDocParagraphCache;
	}

	public String getLanguage() {
		return language;
	}

	public Source getSource() {
		return source;
	}

}
