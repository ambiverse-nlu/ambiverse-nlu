package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider.Source;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider.SourceProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.Spot;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TextSpotter;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TrieBuilder;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.fst.FST;
import org.apache.uima.UIMAException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**This is a callable which returns a wiki corpus
  */
class WikiCorpusTask implements Callable<List<String>> {

	private static final Pattern pattern = Pattern.compile("wgWikibaseItemId\":\"(Q\\d+?)\"");
	private static final int EXTENSION_DEPTH = 1;
	private static final String ENTITY_OUTLINK_CACHE_JSON = "outlinksCache.json";
	private static final ConcurrentHashMap<Integer, Map<String, MentionObject>> entityMentionLabelsMapCache = new ConcurrentHashMap<>();
	private static final Set<String> stopwords;
	private static final Set<String> languagesList;
	private static final Set<String> titlesList;
	private static final Map<Integer, Set<Integer>> entityOutlinks;
	private static final Set<String> knownCountries;

	private static final Logger logger = LoggerFactory.getLogger(WikiCorpusTask.class);
	
	private final String mainEntityRepresentation;
	private final CountDownLatch countDownLatch;
	private final double entitiesThreshold;
	private final Source source;
	private final WikiCorpusGenerator wikiCorpusGenerator;
	private final Integer mainEntityID;

	static {
		try {
			Set<String> stopwords_ = new HashSet<>();
			stopwords_.addAll(Files.readAllLines(Paths.get("src/main/resources/tokens/stopwords-multi.txt"))
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.toSet()));
			stopwords = Collections.unmodifiableSet(stopwords_);
			Set<String> languagesList_ = new HashSet<>();
			languagesList_.addAll(Files.readAllLines(Paths.get("src/main/resources/ner/wikicorpusgenerator/languagesList.txt"))
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.toSet()));
			languagesList_.addAll(Files.readAllLines(Paths.get("src/main/resources/ner/wikicorpusgenerator/languages.txt"))
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.toSet()));
			languagesList = Collections.unmodifiableSet(languagesList_);
			Set<String> titles_ = new HashSet<>();
			titles_.addAll(Files.readAllLines(Paths.get("src/main/resources/ner/wikicorpusgenerator/titles.txt"))
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.toSet()));
			titlesList = Collections.unmodifiableSet(titles_);
			Set<String> knownCountry_ = new HashSet<>();
			knownCountry_.addAll(Files.readAllLines(Paths.get("src/main/resources/ner/generated_configurations/en/gazeteers/known_country.csv"))
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.toSet()));
			knownCountries = Collections.unmodifiableSet(knownCountry_);
			entityOutlinks = Collections.unmodifiableMap(computeEntityOutlinks());

		} catch (IOException e) {
			throw new RuntimeException("IOException during initialization", e);
		} catch (EntityLinkingDataAccessException e) {
			throw new RuntimeException("EntityLinkingDataAccessException during initialization", e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<Integer, Set<Integer>> computeEntityOutlinks() throws EntityLinkingDataAccessException, IOException, SQLException {
		Map<Integer, Set<Integer>> inLinkOutLinkMap = new HashMap<>();
		String file_name = EntityLinkingManager.getAidaDbIdentifierLight() + "_" + ENTITY_OUTLINK_CACHE_JSON;
		if (Files.exists(Paths.get(file_name))) {
			logger.info("Loading " + file_name + " from cache");
			Map<Integer, Set<Integer>> cache = new GsonBuilder().enableComplexMapKeySerialization().create()
					.fromJson(new JsonReader(new FileReader(file_name)),
							new TypeToken<Map<Integer, Set<Integer>>>() {
							}.getType());

			if (cache != null) {
				inLinkOutLinkMap.putAll(cache);
				return inLinkOutLinkMap;
			}
		}
		logger.info("Computing " + ENTITY_OUTLINK_CACHE_JSON);
		TIntObjectHashMap<int[]> inlinkNeighbors = DataAccess.getInlinkNeighbors(DataAccess.getAllEntities());
		TIntObjectIterator<int[]> iterator = inlinkNeighbors.iterator();

		while (iterator.hasNext()) {
			iterator.advance();
			int outEntity = iterator.key();
			int[] inEntities = iterator.value();
			for (int inEntity : inEntities) {
				if (!inLinkOutLinkMap.containsKey(inEntity)) {
					inLinkOutLinkMap.put(inEntity, new HashSet<>());
				}
				Set<Integer> outLinks = inLinkOutLinkMap.get(inEntity);
				outLinks.add(outEntity);

			}
		}

		if (!inLinkOutLinkMap.isEmpty()) {
			Gson gson = new Gson();
			Files.write(Paths.get(file_name), gson.toJson(inLinkOutLinkMap).getBytes());
		}

		return inLinkOutLinkMap;
	}

	public WikiCorpusTask(WikiCorpusGenerator wikiCorpusGenerator, Integer mainEntityID, String mainEntityRepresentation, CountDownLatch countDownLatch,
						  double entitiesThreshold, Source source) {
		this.wikiCorpusGenerator = wikiCorpusGenerator;
		this.mainEntityID = mainEntityID;
		this.mainEntityRepresentation = mainEntityRepresentation;
		this.countDownLatch = countDownLatch;
		this.entitiesThreshold = entitiesThreshold;
		if (source == null) {
			throw new IllegalArgumentException("Source cannot be null");
		}
		this.source = source;
	}

	@Override
	public List<String> call() throws Exception {
		try {
			String docText;
			String docTitle;
			String wikiId;
			Document document;
			String[] articleData = wikiCorpusGenerator.getEntityIDDocParagraphCache().get(mainEntityID);

			if (articleData != null) {
				docTitle = articleData[0];
				docText = articleData[1];
				wikiId = articleData[2];
				if (wikiCorpusGenerator.getSeenDocTitlesPerThreshold().contains(docTitle + Double.toString(entitiesThreshold))) {
					source.addMissingEntity(mainEntityID);
					return null;
				}

			} else {

				document = retrieveDocument(mainEntityRepresentation);
				if (document == null) {
					logger.info("Could not retrieve document for mainEntityRepresentation " + mainEntityRepresentation);
					source.addMissingEntity(mainEntityID);
					return null;
				}
				String docTitle0 = document.select("h1#firstHeading").first().text();
				if (source.getType() == SourceProvider.Type.MANUAL) {
					docTitle = docTitle0;
				} else {
					docTitle = mainEntityRepresentation.replace("<", "")
							.replace(">", "")
							.replace("_", " ");
				}
				if (wikiCorpusGenerator.getSeenDocTitlesPerThreshold().contains(docTitle + Double.toString(entitiesThreshold))) {
					source.addMissingEntity(mainEntityID);
					return null;
				}
				docText = extractFirstParagraphs(document);
				wikiId = extractWikiId(document);
				wikiCorpusGenerator.getEntityIDDocParagraphCache().putIfAbsent(mainEntityID, new String[]{docTitle, docText, wikiId});
			}

			wikiCorpusGenerator.getSeenDocTitlesPerThreshold().add(docTitle + Double.toString(entitiesThreshold));

			List<List<Token>> sentenceTokens = retrieveDocText(docText, wikiId);

			Entity mainEntity = new Entity(null, null, mainEntityID);
//                extracting entity tree
			Entities entities = extractEntities(mainEntityID, EXTENSION_DEPTH);

//                global thresholding
			Map<Entity, Double> entityMilneWittenSimilarityMap = getEntityMilneWittenSimilarityMap(mainEntity, entities);
			List<Entity> selectedEntities = sortMentionEntitiesByMilneWittenSimilarity(entityMilneWittenSimilarityMap);
			if (entitiesThreshold != 1) {
				selectedEntities = selectedEntities.subList(0, (int) Math.round(entitiesThreshold * entities.size()));
			}

			Collection<Entity> _selectedEntities = selectedEntities;
			selectedEntities = new ArrayList<>(IntStream
					.range(0, selectedEntities.size())
					.mapToObj(i -> (Entity) _selectedEntities.toArray()[_selectedEntities.size() - i - 1])
					.collect(Collectors.toList()));
			selectedEntities.add(mainEntity);

			Set<Entity> processingEntities = new HashSet<>();
			HashMap<Integer, Map<String, MentionObject>> entityMentionLabelsMap = new HashMap<>();

			for (Entity entity : selectedEntities) {
				Map<String, MentionObject> labelMap = entityMentionLabelsMapCache.get(entity.getId());
				if (labelMap != null) {
					entityMentionLabelsMap.put(entity.getId(), labelMap);
				} else {
					processingEntities.add(entity);
				}
			}

//        processing the entities which were not found in cache
			if (!processingEntities.isEmpty()) {

				computeNonprocessedEntityMentionLabels(processingEntities, entityMentionLabelsMap);
			}
//                adding the entity mentions in order from the furthest to the main entity to the closest, such that
//                the mentions added recently, the ones which are closer, have higher chance not to get rewritten
			Map<String, MentionObject> typedMentions = new HashMap<>();
			for (Entity entity : selectedEntities) {
				Map<String, MentionObject> mentionLabels = entityMentionLabelsMap.get(entity.getId());
				if (mentionLabels == null) {
					continue;
				}
//					Double milneSimilarity = entityMilneWittenSimilarityMap.get(entity);
//					mentionLabels.keySet().forEach(k -> mentionLabels.get(k).setMilneSimilarity(milneSimilarity));
				typedMentions.putAll(mentionLabels);
			}


//                Map<String, NerType.Label> typedMentions = extractTypedMentions(mainEntityID, mainEntityID, EXTENSION_DEPTH,
//                        entitiesThreshold, document, mainEntityRepresentation);
			if (typedMentions.isEmpty()) {
				logger.info("No typedMentions found for " + mainEntityRepresentation);
				source.addMissingEntity(mainEntityID);
				return null;
			}

			Set<String> sortedMentions = new TreeSet<>();
			sortedMentions.addAll(typedMentions.keySet());
			FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

//			todo temporal fix while we do not support lemmas in cs!
//			double ratio = LANGUAGE.getLanguageForString(wikiCorpusGenerator.getLanguages()).requiresLemma()? 0.7 : 1.0;
			double ratio;
			if (wikiCorpusGenerator.getLanguage().equals("cs")) {
				ratio = 0.7;
			} else {
				ratio = KnowNERLanguage.requiresLemma(wikiCorpusGenerator.getLanguage())? 0.7 : 1.0;
			}

			List<String> doc = new ArrayList<>();
			doc.add("-DOCSTART- (" + docTitle + ")\n");

			for (List<Token> sentence : sentenceTokens) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				int sentenceBegin = sentence.get(0).getBeginIndex();

				String sentenceText = docText.substring(sentenceBegin, sentence.get(sentence.size() - 1).getEndIndex());

				String conflatedSentenceText = conflateSentence(sentenceText, sentence, sentenceBegin);

				List<Integer> begins = sentence.stream().map(t -> t.getBeginIndex() - sentenceBegin).collect(Collectors.toList());
				List<Integer> ends = sentence.stream().map(t -> t.getEndIndex() - sentenceBegin).collect(Collectors.toList());

				Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, conflatedSentenceText,
						new HashSet<>(begins), new HashSet<>(ends), ratio, true);

//				filtering out spots which contain only digits or start with lowercase letter
				spots = filterSpots(sortedMentions, conflatedSentenceText, spots);

				List<Spot> nonOverlappingSpots = getNonOverlappingSpots(spots, conflatedSentenceText, typedMentions);
				logger.debug("Spots found " + spots.size() + "; non-overlapping: " + nonOverlappingSpots.size());
				Map<Integer, Object[]> typedSpots = typeSpots(nonOverlappingSpots, typedMentions, conflatedSentenceText);
//                    List<String> collect = typedSpots.keySet()
//                            .stream()
//                            .map(begin -> sentenceText.substring(begin, ((int) typedSpots.get(begin)[0])))
//                            .collect(Collectors.toList());
//                    logger.info("found spots:" + collect);
				String result = Util.produceAidaSentence(typedSpots, begins, ends, sentenceText);
				if ("".equals(result)) {
					continue;
				}
				doc.add(result);

//				upper case sentences were used for english
//				doc.add(result.toUpperCase());
			}
			if (doc.size() < 2) {
				logger.info("Document contains less than 2 sentences: " + doc.size());
				source.addMissingEntity(mainEntityID);
				return null;
			}
			int count = source.processedDocumentsCounterIncrement();
			logger.info("Document \"" + docTitle + "\" with threshold " + entitiesThreshold + " was processed " +
					"(" + count + "/" + source.getTotalNumber() + ")");
			return doc;

		} catch (Exception e) {
			logger.warn("Error during processing mainEntityRepresentation " + mainEntityRepresentation, e);
			source.addMissingEntity(mainEntityID);
			return null;
		} finally {
			countDownLatch.countDown();
		}
	}

	private String conflateSentence(String sentenceText, List<Token> sentence, int sentenceBegin) {
		int prev = sentenceBegin;
		StringBuilder result = new StringBuilder();
		for (Token t : sentence) {
			String token = EntityLinkingManager.conflateToken(sentenceText.substring(t.getBeginIndex() - sentenceBegin,
					t.getEndIndex() - sentenceBegin),
					Language.getLanguageForString(wikiCorpusGenerator.getLanguage()), true);
			result.append(StringUtils.repeat(" ", t.getBeginIndex() - prev));
			result.append(token);
			prev = t.getEndIndex();
		}
		return result.toString();
	}

	private Set<Spot> filterSpots(Set<String> sortedMentions, String sentenceText, Set<Spot> spots) {
		return spots
				.stream()
				.filter(s -> {
					String spotText = sentenceText.substring(s.getBegin(), s.getEnd());
					if (!sortedMentions.contains(spotText)) {
						return false;
					}
					return spotText.matches("[0-9\\p{L}][^0-9].*");
				})
				.collect(Collectors.toSet());
	}

	private static String extractFirstParagraphs(Document document) {
		Elements paragraphs = document.select("p");
		StringBuilder sb = new StringBuilder();
		for (Element p : paragraphs) {
			if (!p.parent().hasClass("mw-parser-output") || !p.children().isEmpty() && p.child(0).attr("style").equals("display:none")) {
				continue;
			}
			sb.append(p.text()).append("\n");

			if (p.nextElementSibling() != null && (p.nextElementSibling().className().startsWith("toc") ||
					!p.nextElementSibling().nodeName().equals("p") && !p.nextElementSibling().attr("style").equals("display:none"))) {
				break;
			}
//
//            if (p.className().startsWith("toc")) {
//                break;
//            }
		}
		return sb.toString();
	}
//
////		checks if all the capitalized words in the sentence are named entities
//		private boolean checkAllCapitalizedEntities(Map<Integer, Object[]> typedSpots, List<Integer> begins, String sentenceText) {
//			for (int b : begins) {
//				if (sentenceText.substring(b).matches("[A-Z].*") && !typedSpots.containsKey(b)) {
//					return false;
//				}
//			}
//			return true;
//		}

	private void computeNonprocessedEntityMentionLabels(Set<Entity> processingEntities,
														HashMap<Integer, Map<String, MentionObject>> entityMentionLabelsMap)
			throws EntityLinkingDataAccessException, InterruptedException {
		TIntObjectHashMap<int[]> typesIdsForEntitiesIds =
				DataAccess.getTypesIdsForEntitiesIds(processingEntities
						.stream()
						.mapToInt(Entity::getId)
						.toArray());

		TIntObjectHashMap<List<MentionObject>> mentionsForEntities = DataAccess.getMentionsForEntities(new Entities(processingEntities));

		TIntObjectIterator<List<MentionObject>> entityMentionsIterator = mentionsForEntities.iterator();
		while (entityMentionsIterator.hasNext()) {
			Map<String, MentionObject> entityResult = new HashMap<>();
			entityMentionsIterator.advance();

			int eid = entityMentionsIterator.key();

			int[] typeIDs = typesIdsForEntitiesIds.get(eid);
			NerType.Label nerTypeForTypeIds = NerType.getNerTypeForTypeIds(typeIDs);

			for (MentionObject mentionObject : entityMentionsIterator.value()) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				String entityMention = mentionObject.getMention();
				NerType.Label nerTypeForTypeIds_ = nerTypeForTypeIds;

//              getting rid of the junk from the aida database
				if (stopwords.contains(entityMention.toLowerCase()) ||
						entityMention.contains("<SPAN") ||
						entityMention.contains("=") ||
						entityMention.contains("<!--") ||
						entityMention.contains("(") && entityMention.contains(")") ||
						isDate(entityMention.trim()) ||
						entityMention.matches("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()]") ||
						entityMention.endsWith("'S")
						) {
					continue;
				}

//                i.e. United States is a location, not an organization
				if (knownCountries.contains(entityMention.toLowerCase())) {
					nerTypeForTypeIds_ = NerType.Label.LOCATION;
				}
				if (languagesList.contains(entityMention.toLowerCase())) {
					nerTypeForTypeIds_ = NerType.Label.MISC;
				}
				MentionObject copy = mentionObject.copy();
				copy.setLabel(nerTypeForTypeIds_);
				entityResult.put(entityMention, copy);
			}
			entityMentionLabelsMapCache.put(eid, entityResult);
			entityMentionLabelsMap.put(eid, entityResult);
		}
	}

	private Entities extractEntities(int articleEntityID, int expansionLevel) throws IOException, EntityLinkingDataAccessException, InterruptedException {
		Entity articleEntity = new Entity(null, null, articleEntityID);

		Entities neighborEntities = retrieveNeighborEntities(articleEntity);
		Entities result = new Entities(new HashSet<>(neighborEntities.getEntities()));

		if (expansionLevel > 0) {
			neighborEntities
					.getEntities()
					.stream()
					.map(e -> {
						try {
							return extractEntities(e.getId(), expansionLevel - 1);
						} catch (InterruptedException e0) {
								Thread.currentThread().interrupt();
							return null;
						} catch (Exception e1) {
							logger.warn("error during extracting neighboring entities " + e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.forEach(result::addAll);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		return result;
	}

	@NotNull
	private List<List<Token>> retrieveDocText(String docText, String wikiId) throws UIMAException,
			IOException, EntityLinkingDataAccessException,
			NoSuchMethodException, ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
		List<List<Token>> sentenceTokens = null;
		Map<String, List<List<int[]>>> wikiIDDocTokensCache = wikiCorpusGenerator.getWikiIDDocTokensCache();
		if (wikiIDDocTokensCache.containsKey(wikiId)) {
			try {

				sentenceTokens = wikiIDDocTokensCache.get(wikiId)
						.stream()
						.map(l -> l.stream()
								.map(a -> new Token(-1, docText.substring(a[0], a[1]), a[0], a[1], -1))
								.collect(Collectors.toList()))
						.collect(Collectors.toList());
			} catch (Exception e) {
				wikiIDDocTokensCache.remove(wikiId);
			}
		}

		if (sentenceTokens == null) {
			Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString(wikiCorpusGenerator.getLanguage()), docText);
			sentenceTokens = tokens.getSentenceTokens();
			List<List<int[]>> collect = sentenceTokens
					.stream()
					.map(l -> l.stream()
							.map(t -> new int[]{t.getBeginIndex(), t.getEndIndex()})
							.collect(Collectors.toList()))
					.collect(Collectors.toList());
			wikiIDDocTokensCache.put(wikiId, collect);
		}
		return sentenceTokens;
	}

	private static boolean isDate(String mention) {
		return Pattern.matches("\\d+ (January|February|March|April|May|June|July|August|September|October|November|December)( \\d{4})*", mention) ||
				Pattern.matches("(January|February|March|April|May|June|July|August|September|October|November|December) \\d+", mention);
	}

	@Nullable
	private Document retrieveDocument(String articleEntityRepresentation) throws IOException, InterruptedException, EntityLinkingDataAccessException {

		String language = wikiCorpusGenerator.getLanguage();
		if (wikiCorpusGenerator.getSource().getType() == SourceProvider.Type.MANUAL && language.equals("de")) {
//			todo trick to delete later
			logger.info("Retrieving document for german, manual evaluation of the algorithm");
			Map<String, String> urls = new HashMap<>();
			urls.put("<ABBA>", "https://de.wikipedia.org/wiki/ABBA");
			urls.put("<Philadelphia_Phillies>", "https://de.wikipedia.org/wiki/Philadelphia_Phillies");
			urls.put("<Amazon_River>", "https://de.wikipedia.org/wiki/Amazonas");
			urls.put("<Sagrada_Família>", "https://de.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
			urls.put("<Russia>", "https://de.wikipedia.org/wiki/Russland");
			urls.put("<Chicago>", "https://de.wikipedia.org/wiki/Chicago");
			urls.put("<Leonardo_da_Vinci>", "https://de.wikipedia.org/wiki/Leonardo_da_Vinci");
			urls.put("<Delhi>", "https://de.wikipedia.org/wiki/Delhi");
			urls.put("<William_Shakespeare>", "https://de.wikipedia.org/wiki/William_Shakespeare");
			urls.put("<Albert_Einstein>", "https://de.wikipedia.org/wiki/Albert_Einstein");
			urls.put("<Mao_Zedong>", "https://de.wikipedia.org/wiki/Mao_Zedong");
			urls.put("<Plato>", "https://de.wikipedia.org/wiki/Platon");
			urls.put("<Napoleon>", "https://de.wikipedia.org/wiki/Napoleon_Bonaparte");
			String link = urls.get(articleEntityRepresentation);

			return retrieveDocumentFromLink(link);

		}
		Set<String> entityURLs = DataAccess.getEntityURLs(articleEntityRepresentation);
		if (entityURLs.isEmpty()) {
			logger.debug("No urls found for articleEntityRepresentation " + articleEntityRepresentation);
			return null;
		}


		for (String entityURL : entityURLs) {
			if (entityURL.contains(language + ".wikipedia.org")) {
				return retrieveDocumentFromLink(entityURL);
			}
		}

//		logger.info("No urls found for articleEntityRepresentation " + articleEntityRepresentation + " in language " + language);
//		todo search for the link in the language through the existing links
		String otherLangURL = entityURLs.iterator().next();
		Document other = retrieveDocumentFromLink(otherLangURL);
		if (other == null) {
			logger.debug("Could not retrieve document for articleEntityRepresentation " + articleEntityRepresentation +
					" from link in other language: " + otherLangURL);
			return null;
		}
		String link = retrieveLinkInLanguage(other, language);
		if (link == null) {
			logger.debug("No links to article in the current language from another language for articleEntityRepresentation " + articleEntityRepresentation);
			return null;
		}
		return retrieveDocumentFromLink(link);
	}

	private Document retrieveDocumentFromLink(String link) throws InterruptedException, IOException {
		Document document = null;
		int tryCounter = 5;
		while (document == null && tryCounter > 0) {

			try {
				document = Jsoup.parse(new URL(link), 30000);
			} catch (org.jsoup.HttpStatusException httpe) {
//                    logger.info(href + " try " + counter);
				if (httpe.getStatusCode() == 429) {
					Thread.sleep(new Random().nextInt(2000));
					logger.info("Try " + (7 - tryCounter) + ": " + link);
				}
				tryCounter--;
			}
		}
		return document;
	}

	private String retrieveLinkInLanguage(Document document, String language) {
		Elements elementsByClass = document.getElementsByClass("interwiki-" + language);
		if (elementsByClass == null || elementsByClass.isEmpty()) {
//		logger.info("link in " + language + " was not found");
			return null;
		}
		return elementsByClass.first().child(0).attr("href");
	}

	private String extractWikiId(Document d) {
		Elements scriptTags = d.select("script");
		for (Element s : scriptTags) {
			Matcher matcher = pattern.matcher(s.data());
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	private Entities retrieveNeighborEntities(Entity articleEntity) throws EntityLinkingDataAccessException, IOException, InterruptedException {
		Entities neighborEntities = new Entities();
		if (entityOutlinks.containsKey(articleEntity.getId())) {
			int[] neighborEntityIDs = entityOutlinks.get(articleEntity.getId())
					.stream()
					.mapToInt(Integer::intValue)
					.toArray();

			neighborEntities.add(articleEntity);
			for (int eid : neighborEntityIDs) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				neighborEntities.add(new Entity(null, null, eid));
			}
		}
		return neighborEntities;
	}

	private Map<Entity, Double> getEntityMilneWittenSimilarityMap(Entity mainEntity, Entities neighborEntities) throws Exception {
		Entities entities = new Entities(neighborEntities);
		entities.add(mainEntity);
		EntityEntitySimilarity mwSim = EntityEntitySimilarity.getMilneWittenSimilarity(entities, new NullTracer());
		Map<Entity, Double> entitySimilarities = new HashMap<>();
		for (Entity e : neighborEntities.getEntities()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			entitySimilarities.put(e, mwSim.calcSimilarity(e, mainEntity));
		}

		return entitySimilarities;
	}

	@NotNull
	private List<Entity> sortMentionEntitiesByMilneWittenSimilarity(Map<Entity, Double> entitySimilarities) {

		List<Entity> selectedEntities = new ArrayList<>(entitySimilarities.keySet());
		selectedEntities.sort((o1, o2) -> {
			try {
				synchronized (this) {
					double o1Sim = entitySimilarities.get(o1);
					double o2Sim = entitySimilarities.get(o2);
					return o1Sim == o2Sim ? 0 : o1Sim > o2Sim ? -1 : 1;
				}
			} catch (Exception e) {
//                logger.error(mainEntity + ": could not compute similarity of entities " + o1 + ", " + o2);
				e.printStackTrace();
				throw new RuntimeException("could not compute similarity of entities " + o1 + ", " + o2);
			}
		});
		return selectedEntities;
	}

	private Map<Integer, Object[]> typeSpots(List<Spot> spots, Map<String, MentionObject> typedMentions, String conflatedText) {
		Map<Integer, Object[]> typedSpots = new HashMap<>();
		for (Spot s : spots) {
			String spotText = conflatedText.substring(s.getBegin(), s.getEnd());
			MentionObject mentionObject = typedMentions.get(spotText);
			NerType.Label label = mentionObject.getLabel();

			if (label == NerType.Label.LOCATION && spotText.contains(",")) {
				String[] split = spotText.split(", ", 2);
				if (split.length == 2) {
					typedSpots.put(s.getBegin(), new Object[]{s.getBegin() + split[0].length(), label/*, mentionObject.getPrior(), mentionObject.getMilneSimilarity()*/});
					typedSpots.put(s.getBegin(), new Object[]{s.getBegin() + split[0].length(), label/*, mentionObject.getPrior(), mentionObject.getMilneSimilarity()*/});
					typedSpots.put(s.getBegin() + spotText.indexOf(split[1]), new Object[]{s.getEnd(), label/*, mentionObject.getPrior(), mentionObject.getMilneSimilarity()*/});
				} else {
					typedSpots.put(s.getBegin(), new Object[]{s.getEnd(), label/*, mentionObject.getPrior(), mentionObject.getMilneSimilarity()*/});
				}
			} else

				typedSpots.put(s.getBegin(), new Object[]{s.getEnd(), label/*, mentionObject.getPrior(), mentionObject.getMilneSimilarity()*/});
		}
		return typedSpots;
	}

	private List<Spot> getNonOverlappingSpots(Set<Spot> spots, String conflatedText, Map<String, MentionObject> typedMentions) {
//		with equal begin, the longer mention goes first
		Set<Spot> sortedSpots = new TreeSet<>((o1, o2) -> o1.getBegin() != o2.getBegin() ? o1.getBegin() - o2.getBegin() : o2.getEnd() - o1.getEnd());
		sortedSpots.addAll(spots);
		Iterator<Spot> it = sortedSpots.iterator();
		List<Spot> spotList = new ArrayList<>();
		while (it.hasNext()) {
			Spot spot = it.next();
			Spot last = !spotList.isEmpty() ? spotList.get(spotList.size() - 1) : null;
			String spotText = conflatedText.substring(spot.getBegin(), spot.getEnd());

			String lastSpotText = last == null? null : conflatedText.substring(last.getBegin(), last.getEnd());
			if (last == null) {
				spotList.add(spot);

			} else if (last.getBegin() != spot.getBegin()) {
				if (spot.getBegin() < last.getEnd()) {
//                    condition of priority for overlapping spots
					if (

							spotText.split(" ").length > lastSpotText.split(" ").length ||

//							priority to complex location (e.g. "London, UK")
							complexLocationCheck(spotText, lastSpotText, typedMentions)) {
						spotList.remove(last);
						spotList.add(spot);
					}
				} else {
					spotList.add(spot);
				}
			} else {
				if (complexLocationCheck(spotText, lastSpotText, typedMentions)) {
					spotList.remove(last);
					spotList.add(spot);
				}
			}
		}
		return spotList;
	}
	//	if true - spot, false - last; initial priority for last because it is longer
	private boolean complexLocationCheck(String spotText, String lastSpotText, Map<String, MentionObject> typedMentions) {
		return !(typedMentions.get(lastSpotText).getLabel() == NerType.Label.LOCATION && lastSpotText.contains(",")) &&
				typedMentions.get(spotText).getLabel() == NerType.Label.LOCATION && spotText.contains(",");

	}

	//	if true - spot, false - last; initial priority for last because it is longer
	private boolean titleCheck(Spot last, Spot spot, String lastSpotText, String spotText) {
		if (last.getEnd() != spot.getEnd()) {
			return false;
		}

		String prefix = lastSpotText.substring(0, lastSpotText.indexOf(spotText));
		return titlesList.contains(prefix.trim().toLowerCase());
	}
}
