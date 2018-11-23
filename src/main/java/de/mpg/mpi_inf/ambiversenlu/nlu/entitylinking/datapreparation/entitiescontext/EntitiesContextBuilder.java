package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext;

import com.google.common.primitives.Ints;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.UnitUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.UnitBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.uima.UIMAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

/**
 * Collects all keyphrases from different sources and stores
 * them in the entity_keyphrases table. It creates a word_id table
 * storing the string to int mappings as side-output, was well as a
 * word_expansion table containing regular-to-ALLCAPS mappings for terms.
 */
public class EntitiesContextBuilder {

  private static final Logger logger = LoggerFactory.getLogger(EntitiesContextBuilder.class);

  private PreparedStatement statementKeyphrases;

  private TIntObjectHashMap<Set<KeyphraseRecord>> entitiesKeyphrases;

  private TObjectIntHashMap<KBIdentifiedEntity> entity2id;

  private TObjectIntHashMap<String> word2id;

  private TObjectIntHashMap<String> keyphraseSources;

  private TIntObjectHashMap<Integer[]> keyphraseTokenIds;

  private int keyphraseCounter = 0;

  private int keyphraseSourceCounter = 0;

  private List<EntitiesContextEntriesDataProvider> providers;

  public EntitiesContextBuilder(List<EntitiesContextEntriesDataProvider> providers) {
    this.providers = providers;
  }

  public void run() throws SQLException, IOException, EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, NoSuchMethodException,
      MissingSettingException, UnprocessableDocumentException {
    logger.info("Reading entity ids.");
    entity2id = DataAccess.getAllEntityIds();
    logger.info("Reading entity ids DONE.");

    //initialize variables
    entitiesKeyphrases = new TIntObjectHashMap<Set<KeyphraseRecord>>(2500000);
    keyphraseSources = new TObjectIntHashMap<String>();
    keyphraseTokenIds = new TIntObjectHashMap<Integer[]>(2500000);
    word2id = new TObjectIntHashMap<String>(10000000);
    aggregateContextsFromAllProviders();
    storeContexts();
  }

  /**
   * @apiNote Generates the ids of keyphrases and ngrams according to their entity-cooccurences so that data can be compressed
   * It goes over all entities and counts each occurence of the nGram or keyphrases, then it sorts in desc order and assigns ids
   * @param ngrams contains the n-grams to be generated (e.g., 1, 2, 3 ...)
   */
  private void generateNgramsId(int[] ngrams)
      throws IOException, EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, NoSuchMethodException, MissingSettingException,
      UnprocessableDocumentException {
    Map<String, Integer> nGramsCount = new HashMap<>();
    for (EntitiesContextEntriesDataProvider provider : providers) {
      for (Entry<String, Set<EntityContextEntry>> entry : provider) {
        KBIdentifiedEntity entity = KBIdentifiedEntity.getKBIdentifiedEntity(entry.getKey(), provider.getKnowledgebaseName());
        if (!includeEntity(entity2id.get(entity))) {
          continue;
        }
        Set<String> nGramsEntity = new HashSet<String>();
        Set<EntityContextEntry> entityContext = entry.getValue();
        for (EntityContextEntry entityContextEntry : entityContext) {
          String keyphrase = entityContextEntry.context;
          if(keyphrase.trim().isEmpty()) {
            continue;
          }
          nGramsEntity.add(keyphrase);
          if (ngrams == null || ngrams.length == 0) continue;
          Tokens tokens = getTokenizedKeyphrase(keyphrase, entityContextEntry.language);
          if (tokens == null || tokens.size() == 0) {
            continue;
          }
          if(!Utils.shouldKeepTokens(tokens)) {
            continue;
          }
          int[] tokensTmpIds = new int[tokens.size()];
          TIntObjectHashMap<String> id2wordTmp = new TIntObjectHashMap<String>();
          for (int i = 1; i < tokens.size(); i++) {
            tokensTmpIds[i] = i;
            id2wordTmp.put(i, tokens.getToken(i - 1).getOriginal());
          }
          for (int ngram : ngrams) {
            Set<TIntArrayList> result = new HashSet<>();
            UnitUtil.getNgram(ngram, tokensTmpIds, null, result);
            for (TIntArrayList r : result) {
              nGramsEntity.add(UnitBuilder.buildUnit(r, id2wordTmp));
            }
          }
        }
        for (String ng : nGramsEntity) {
          if (nGramsCount.containsKey(ng)) {
            nGramsCount.put(ng, nGramsCount.get(ng) + 1);
          } else {
            nGramsCount.put(ng, 1);
          }
        }
      }
    }

    nGramsCount = CollectionUtils.sortMapByValue(nGramsCount, true);
    int index = 1;
    for (Iterator<Entry<String, Integer>> it = nGramsCount.entrySet().iterator(); it.hasNext(); ) {
      Entry<String, Integer> e = it.next();
      word2id.put(e.getKey(), index);
      index++;
    }
  }

  private void aggregateContextsFromAllProviders()
      throws IOException, EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, NoSuchMethodException, MissingSettingException,
          UnprocessableDocumentException {
    PrintWriter write = new PrintWriter(new File("./keyphrases.txt"));
    try {
      List<Integer> nGrams = new ArrayList<>();
      if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_KEYWORDS)) {
        nGrams.add(1);
      }
      if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS)) {
        nGrams.add(2);
      }
      generateNgramsId(Ints.toArray(nGrams));

      for (EntitiesContextEntriesDataProvider provider : providers) {
        String knowledgebase = provider.getKnowledgebaseName();
        for (Entry<String, Set<EntityContextEntry>> entry : provider) {
          String entity = entry.getKey();
          KBIdentifiedEntity kbEntity = KBIdentifiedEntity.getKBIdentifiedEntity(entity, knowledgebase);
          Set<EntityContextEntry> entityContext = entry.getValue();
          for (EntityContextEntry entityContextEntry : entityContext) {
            String keyphrase = entityContextEntry.context;
            write.println(keyphrase);
            String source = entityContextEntry.source;
            addEntityKeyphrase(kbEntity, keyphrase, source, entityContextEntry.language);
            addKeyphraseSource(source);
          }
        }
      }
    } finally {
      write.close();
    }
  }

  private void storeContexts() throws SQLException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement schemaStatement = con.createStatement();
    // the sdf column will later be filled with the occurrence count of the keyphrase in its entity's superdoc
    // i.e. how often the keyphrase occurrs in the keyphrase sets of entities linking to this entity

    String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_KEYPHRASES + "(entity INTEGER, " + "keyphrase INTEGER, " + "source INTEGER, "
        + "weight DOUBLE PRECISION DEFAULT 0.0, " + "count INTEGER)";
    schemaStatement.execute(sql);

    con.setAutoCommit(false);

    // Storing in the DB
    logger.info("Started storing entity-keyphrases in the DB");
    int count = 0;
    statementKeyphrases = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.ENTITY_KEYPHRASES + "(entity, keyphrase, source) " + "VALUES(?, ?, ?)",
            1_000_000);
    for (int entity : entitiesKeyphrases.keys()) {
      for (KeyphraseRecord keyphraseRecord : entitiesKeyphrases.get(entity)) {
        statementKeyphrases.setInt(1, entity);
        statementKeyphrases.setInt(2, keyphraseRecord.keyphraseId);
        // Array tokenIds = con.createArrayOf("integer", keyphraseRecord.keyphraseTokenIds);
        // statementKeyphrases.setArray(3, tokenIds);
        // add token ids to the map
        keyphraseTokenIds.put(keyphraseRecord.keyphraseId, keyphraseRecord.keyphraseTokenIds);
        statementKeyphrases.setInt(3, keyphraseSources.get(keyphraseRecord.source));
        DBUtil.addBatch(statementKeyphrases);

        if (++count % 1_000_000 == 0) {
          logger.info("Inserted " + count + " keyphrases");
        }
      }
    }
    DBUtil.executeBatch(statementKeyphrases);
    con.commit();
    statementKeyphrases.close();
    logger.info("Started creating the index");
    sql = "CREATE INDEX entity_kws_kindex ON " + DataAccessSQL.ENTITY_KEYPHRASES + " USING btree (entity, keyphrase, weight, source)";
    schemaStatement.execute(sql);

    // Create keyphrase sources table
    logger.info("Started storing keyphrase sources in the DB");
    sql = "CREATE TABLE " + DataAccessSQL.KEYPHRASES_SOURCE + "(source TEXT, source_id INTEGER)";
    schemaStatement.execute(sql);
    PreparedStatement statementKeyphraseSources = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.KEYPHRASES_SOURCE + "(source, source_id) " + "VALUES(?, ?)", 1_000_000);
    for (String source : keyphraseSources.keySet()) {
      statementKeyphraseSources.setString(1, source);
      statementKeyphraseSources.setInt(2, keyphraseSources.get(source));
      DBUtil.addBatch(statementKeyphraseSources);
    }
    DBUtil.executeBatch(statementKeyphraseSources);
    con.commit();
    statementKeyphraseSources.close();

    // Create keyphrase tokenid table
    logger.info("Started storing keyphrase token ids in the DB");
    sql = "CREATE TABLE " + DataAccessSQL.KEYPHRASES_TOKENS + "(keyphrase INTEGER, token INTEGER, position INTEGER)";
    schemaStatement.execute(sql);
    PreparedStatement statementKeyphraseTokens = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.KEYPHRASES_TOKENS + "(keyphrase, token, position) " + "VALUES(?, ?, ?)",
            1_000_000);
    int kpCount = 0;
    for (int keyphrase : keyphraseTokenIds.keys()) {
      int posCount = 0;
      for (int tokId : keyphraseTokenIds.get(keyphrase)) {
        statementKeyphraseTokens.setInt(1, keyphrase);
        statementKeyphraseTokens.setInt(2, tokId);
        statementKeyphraseTokens.setInt(3, posCount++);
        DBUtil.addBatch(statementKeyphraseTokens);

        if (++kpCount % 1000000 == 0) {
          logger.info("Inserted " + kpCount + " keyphrase tokens");
        }
      }

    }
    DBUtil.executeBatch(statementKeyphraseTokens);
    con.commit();
    statementKeyphraseTokens.close();
    // Create the upper case mappings for term expansion.
    logger.info("Started storing word-expansions in the DB");
    sql = "CREATE TABLE " + DataAccessSQL.WORD_EXPANSION + "(word INTEGER, expansion INTEGER)";
    schemaStatement.execute(sql);
    PreparedStatement stmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.WORD_EXPANSION + "(word, expansion)  VALUES(?, ?)", 100000);

    // Need to iterate over a copy of the words as new ids are added
    // during the iteration, but only for upperIds.
    Set<String> words = new HashSet<String>(word2id.keySet());
    for (String word : words) {
      int id = word2id.get(word);
      String upperWord = word.toUpperCase(Locale.ENGLISH);
      int upperId = getWordId(upperWord);
      stmt.setInt(1, id);
      stmt.setInt(2, upperId);
      DBUtil.addBatch(stmt);
    }
    DBUtil.executeBatch(stmt);
    con.commit();
    stmt.close();
    logger.info("Started creating the word-expansion index");
    sql = "CREATE INDEX word_expansion_weindex ON " + DataAccessSQL.WORD_EXPANSION + " USING btree (word, expansion);";
    schemaStatement.execute(sql);
    sql = "CREATE INDEX word_expansion_ewindex ON " + DataAccessSQL.WORD_EXPANSION + " USING btree (expansion, word);";
    schemaStatement.execute(sql);

    // Store word-ids in DB.
    logger.info("Started storing word-ids in the DB");
    sql = "CREATE TABLE " + DataAccessSQL.WORD_IDS + "(word TEXT, id INTEGER)";
    schemaStatement.execute(sql);
    schemaStatement.close();

    stmt = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.WORD_IDS + "(word, id) VALUES(?, ?)", 100000);
    for (TObjectIntIterator<String> itr = word2id.iterator(); itr.hasNext(); ) {
      itr.advance();
      stmt.setString(1, itr.key());
      stmt.setInt(2, itr.value());
      DBUtil.addBatch(stmt);
    }
    DBUtil.executeBatch(stmt);
    con.commit();
    stmt.close();
    con.setAutoCommit(true);
    logger.info("Started creating the word-id index");
    schemaStatement = con.createStatement();
    schemaStatement.execute("ALTER TABLE " + DataAccessSQL.WORD_IDS + " ALTER word SET STATISTICS 1000");
    schemaStatement.execute("ALTER TABLE " + DataAccessSQL.WORD_IDS + " ALTER id SET STATISTICS 1000");
    sql = "CREATE INDEX word_id_wiindex ON " + DataAccessSQL.WORD_IDS + " using btree (word);";
    schemaStatement.execute(sql);
    sql = "CREATE INDEX word_id_iwindex ON " + DataAccessSQL.WORD_IDS + " using btree (id);";
    schemaStatement.execute(sql);

    //initializing the keyphrases sources weights table
    logger.info("Initializing the keyphrases sources weights table");
    sql = "CREATE TABLE " + DataAccessSQL.KEYPHRASES_SOURCES_WEIGHTS + "(source integer, weight double precision)";
    schemaStatement.execute(sql);

    sql =
        "insert INTO " + DataAccessSQL.KEYPHRASES_SOURCES_WEIGHTS + "(source, weight) select sources.source_id,1.0 from " + "( SELECT source_id FROM "
            + DataAccessSQL.KEYPHRASES_SOURCE + ") as sources";
    schemaStatement.execute(sql);
    schemaStatement.close();

    logger.info("Everything is done !! :-) ");

    EntityLinkingManager.releaseConnection(con);
  }

  private void addKeyphraseSource(String source) {
    if (!keyphraseSources.contains(source)) {
      keyphraseSourceCounter++;
      keyphraseSources.put(source, keyphraseSourceCounter);
    }
  }

  private Tokens getTokenizedKeyphrase(String keyphrase, Language language)
      throws UIMAException, IOException, ClassNotFoundException, EntityLinkingDataAccessException,
      MissingSettingException, NoSuchMethodException, UnprocessableDocumentException {
    if (keyphrase.contains("http://") || keyphrase.contains(":\\")) {
      return null;
    }
    return UimaTokenizer.tokenize(language, keyphrase);
  }

  private boolean includeEntity(int entityId) {
    //assert entityId > 0;
    // The dictionary building phase discards some entities. Ignore them here.
    if (entityId <= 0) {
      return false;
    } else {
      return true;
    }
  }

  private void addEntityKeyphrase(KBIdentifiedEntity entity, String keyphrase, String source, Language language)
      throws UIMAException, MissingSettingException, IOException, ClassNotFoundException,
      EntityLinkingDataAccessException, NoSuchMethodException, UnprocessableDocumentException {
    int entityId = entity2id.get(entity);
    if (!includeEntity(entityId)) {
      return;
    }
    if(keyphrase.trim().isEmpty()) {
      return;
    }

    // Tokenize keyphrase and store tokens.
    Tokens tokens = getTokenizedKeyphrase(keyphrase, language);
    if (tokens == null || tokens.size() == 0) {
      logger.debug("Could not tokenize '" + keyphrase + "', skipping.");
      return;
    }
    if(!Utils.shouldKeepTokens(tokens)) {
      return;
    }

    Set<KeyphraseRecord> keyphrases = entitiesKeyphrases.get(entityId);
    if (keyphrases == null) entitiesKeyphrases.put(entityId, (keyphrases = new HashSet<>()));
    keyphrases.add(new KeyphraseRecord(getWordId(keyphrase), extractTokens(tokens), source));

    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS)) {
      extractUnits(tokens, UnitType.BIGRAM.getUnitSize());
    }

    if (++keyphraseCounter % 1_000_000 == 0) {
      logger.info("Read " + keyphraseCounter + " keyphrases");
    }
  }

  private Integer[] extractTokens(Tokens tokens) {
    Integer[] tokenIds = new Integer[tokens.size()];
    int i = 0;
    for (Token token : tokens) {
      String tokenString = token.getOriginal();
      tokenIds[i] = getWordId(tokenString);
      ++i;
    }
    return tokenIds;
  }

  private int[] extractUnits(Tokens tokens, int unitSize) {
    int unitSizeMinusOne = unitSize - 1;
    if (tokens.size() < unitSizeMinusOne) return new int[0];
    String[] tokenStrings = new String[tokens.size()];
    for (int i = 0; i < tokens.size(); i++) {
      tokenStrings[i] = tokens.getToken(i).getOriginal();
    }
    // TODO: This will break for unitSize > 4 if tokens.size() == 1
    int[] units = new int[unitSize == 1 ? tokens.size() : tokens.size() + 3 - unitSize];
    if (unitSize > 1) {
      String[] firstUnitArray = new String[unitSize];
      // we skip the first element of the firstUnitArray because it should stay 0
      System.arraycopy(tokenStrings, 0, firstUnitArray, 1, unitSizeMinusOne);
      units[0] = getUnitId(firstUnitArray);
      String[] lastUnitArray = new String[unitSize];
      // we skip the last element of the lastUnitArray because it should stay 0
      System.arraycopy(tokenStrings, tokenStrings.length - unitSizeMinusOne, lastUnitArray, 0, unitSizeMinusOne);
      units[units.length - 1] = getUnitId(lastUnitArray);
    }
    if (tokenStrings.length >= unitSize) {
      for (int i = 0; i < tokenStrings.length - unitSizeMinusOne; i++) {
        units[i + 1] = getUnitId(Arrays.copyOfRange(tokenStrings, i, i + unitSize));
      }
    }
    return units;
  }

  /**
   * Retrieves the id for the word, creates a new id for unseen words.
   */
  private int getWordId(String word) {
    assert word2id.containsKey(word) : word + " does not exist in word2id.";

    // Do not use 0 index, it has a special meaning in SQL.
    int wordId = word2id.size() + 1;
    if (word2id.contains(word)) {
      wordId = word2id.get(word);
    } else {
      word2id.put(word, wordId);
    }

    return wordId;
  }

  private int getUnitId(String[] tokens) {
    return getWordId(UnitBuilder.buildUnit(tokens));
  }

  private class KeyphraseRecord {

    private int keyphraseId;

    private Integer[] keyphraseTokenIds;

    private String source;

    public KeyphraseRecord(int keyphraseId, Integer[] keyphraseTokenIds, String source) {
      this.keyphraseId = keyphraseId;
      this.keyphraseTokenIds = keyphraseTokenIds;
      this.source = source;
    }

    @Override public int hashCode() {
      return new Integer(keyphraseId).hashCode();
    }

    @Override public boolean equals(Object obj) {
      return keyphraseId == ((KeyphraseRecord) obj).keyphraseId;
    }
  }
}
