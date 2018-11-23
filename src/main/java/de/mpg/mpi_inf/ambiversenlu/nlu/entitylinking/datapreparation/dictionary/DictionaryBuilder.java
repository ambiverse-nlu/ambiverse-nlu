package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MaterializedPriorProbability;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.DBUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class materializes the dictionary used for entity candidate lookups.
 * It creates a new table:
 * dictionary(mention TEXT, entity INTEGER, prior FLOAT)
 * which can then be used for candidate lookups and by
 * {@link MaterializedPriorProbability}.
 *  *
 * The class hold two lists of data providers.
 * One to get the entries themselves and the other is used to compute the prior probability
 * For example, in original AIDA, we different sources for means entries, but we use
 * only anchor links to compute the prior probabilities
 *
 *
 * As a side-output it creates the entity-id table which maps entities and
 * mention strings to int-ids:
 * entity_id(entity TEXT, id INTEGER)
 */
public class DictionaryBuilder {

  private static final Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

  private static final String dictionary_table = DataAccessSQL.DICTIONARY;
  private static final String entities_table = DataAccessSQL.ENTITIES;

  private List<DictionaryEntriesDataProvider> dictionaryEntriesProviders;

  private List<DictionaryEntriesDataProvider> dictionaryEntriesProvidersToComputePrior;

  /**
   *
   * @param dictionaryEntriesProviders the providers that will be use to fill in the entries
   * @param dictionaryEntriesProvidersToComputePrior the providers the will be used to computer the prior probability
   */
  public DictionaryBuilder(List<DictionaryEntriesDataProvider> dictionaryEntriesProviders,
      List<DictionaryEntriesDataProvider> dictionaryEntriesProvidersToComputePrior) {
    super();
    this.dictionaryEntriesProviders = dictionaryEntriesProviders;
    this.dictionaryEntriesProvidersToComputePrior = dictionaryEntriesProvidersToComputePrior;
  }

  public void run() throws SQLException, EntityLinkingDataAccessException {
    Map<String, TObjectIntHashMap<DictionaryEntity>> entries = new HashMap<>();
    fillDictionaryEntries(entries);
    computeCounts(entries);
    writeToDatabase(entries);
  }

  /**
   * Reads the entries from all data providers, and merges them together in one big dictionary
   */
  private void fillDictionaryEntries(Map<String, TObjectIntHashMap<DictionaryEntity>> entries) {
    for (DictionaryEntriesDataProvider provider : dictionaryEntriesProviders) {
      for (Entry<String, List<DictionaryEntity>> entry : provider) {
        String originalMentionString = entry.getKey();
        for (DictionaryEntity candidateEntity : entry.getValue()) {
          List<String> mentionStrings = generateMentionStrings(originalMentionString, candidateEntity.isNamedEntity);
          for (String mentionString : mentionStrings) {
            addDictionaryEntry(entries, mentionString, candidateEntity);
          }
        }
      }
    }
  }

  /**
   * Generates the mention strings for the appropriate entity type with respect to the case conflation.
   *
   * @param originalMentionString Original mention string.
   * @param entityType  Type of the entity.
   * @return  Properly conflated mention strings.
   */
  private List<String> generateMentionStrings(String originalMentionString, EntityType entityType) {
    List<String> mentionStrings = new ArrayList<>();

    switch (entityType) {
      case NAMED_ENTITY:
        mentionStrings.add(EntityLinkingManager.conflateToken(originalMentionString, true));
        break;
      case CONCEPT:
        mentionStrings.add(EntityLinkingManager.conflateToken(originalMentionString, false));
        break;
      default:
        mentionStrings.add(EntityLinkingManager.conflateToken(originalMentionString, true));
        mentionStrings.add(EntityLinkingManager.conflateToken(originalMentionString, false));
        break;
    }

    return mentionStrings;
  }

  private void addDictionaryEntry(Map<String, TObjectIntHashMap<DictionaryEntity>> dictionaryEntries,
      String mentionString, DictionaryEntity candidateEntity) {
    TObjectIntHashMap<DictionaryEntity> mentionCandidates = dictionaryEntries.get(mentionString);
    if (mentionCandidates == null) {
      mentionCandidates = new TObjectIntHashMap<>();
      dictionaryEntries.put(mentionString, mentionCandidates);
    }
    mentionCandidates.adjustOrPutValue(candidateEntity, 0, 0);
  }

  /**
   * Reads the entries from all data providers, and uses them to compute the counts of
   * each entry.  No new entries are added at this stage, only counting
   */
  private void computeCounts(Map<String, TObjectIntHashMap<DictionaryEntity>> dictionaryEntries) {
    Map<String, TObjectIntHashMap<String>> mentionCandidatesForCounting = new HashMap<>();
    for (DictionaryEntriesDataProvider provider : dictionaryEntriesProvidersToComputePrior) {
      for (Entry<String, List<DictionaryEntity>> entry : provider) {
        String originalMentionString = entry.getKey();
        // if the mentionString is longer than 3 characters, the assumptions is that
        // the case does not matter - make all UPPER CASE/LOWER CASEA
        for (DictionaryEntity candidateEntity : entry.getValue()) {
          List<String> mentionStrings = generateMentionStrings(originalMentionString, candidateEntity.isNamedEntity);
          for (String mentionString : mentionStrings) {
            String entity = candidateEntity.entity;
            TObjectIntHashMap<String> mentionCandidates = mentionCandidatesForCounting.get(mentionString);
            if (mentionCandidates == null) {
              mentionCandidates = new TObjectIntHashMap<String>();
              mentionCandidatesForCounting.put(mentionString, mentionCandidates);
            }
            mentionCandidates.adjustOrPutValue(entity, 1, 1);
          }
        }
      }
    }

    for(Entry<String, TObjectIntHashMap<DictionaryEntity>> entry: dictionaryEntries.entrySet()) {
      String mentionString = entry.getKey();
      TObjectIntHashMap<String> entitiesForCounting = mentionCandidatesForCounting.get(mentionString);
      if (entitiesForCounting == null) {
        continue;
      }
      TObjectIntHashMap<DictionaryEntity> mentionCandidates = entry.getValue();
      for (DictionaryEntity candidate : mentionCandidates.keySet()) {
        if (entitiesForCounting.containsKey(candidate.entity)) {
          mentionCandidates.adjustValue(candidate, entitiesForCounting.get(candidate.entity));
        }
      }
    }
  }

  private void writeToDatabase(Map<String, TObjectIntHashMap<DictionaryEntity>> entries) throws SQLException, EntityLinkingDataAccessException {
    // Get int ids for entities and mentions, starting with 1.
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = new TObjectIntHashMap<KBIdentifiedEntity>();
    computeEntityIds(entries, entityIds);

    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    con.setAutoCommit(false);
    Statement stmt = con.createStatement();

    // Write the dictionary to the database.
    stmt.execute("CREATE TABLE " + DataAccessSQL.ENTITY_LANGUAGES + " " + "(entity INTEGER NOT NULL, mention TEXT NOT NULL, language INTEGER NOT NULL)");
    stmt.close();
    PreparedStatement pStmt = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.ENTITY_LANGUAGES + "(entity, mention, language) VALUES(?, ?, ?)", 10000);
    int inserts = 0;
    for (String mention : entries.keySet()) {
      TObjectIntHashMap<DictionaryEntity> dictEntityForMention = entries.get(mention);
      Set<DictionaryEntityBaseWithLanguage> dictEntityForMentionWithoutSource = new HashSet<>(dictEntityForMention.size()); // Transform all dictionary entities to the base class with language to ignore source
      for (TObjectIntIterator<DictionaryEntity> it = dictEntityForMention.iterator(); it.hasNext();) {
        it.advance();
        DictionaryEntityBaseWithLanguage debl = DictionaryEntityBaseWithLanguage.getDictionaryEntityBaseWithLanguage(it.key().entity, it.key().knowledgebase, it.key().language, it.key().isNamedEntity);
        dictEntityForMentionWithoutSource.add(debl);
      }
      for(DictionaryEntityBaseWithLanguage entry: dictEntityForMentionWithoutSource) {
        int id = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(entry.entity, entry.knowledgebase));
        pStmt.setInt(1, id);
        pStmt.setString(2, mention);
        pStmt.setInt(3, entry.language.getID());
        if (++inserts % 1000000 == 0) {
          logger.info(inserts + " entitities added");
        }
        DBUtil.addBatch(pStmt);
      }

    }
    DBUtil.executeBatch(pStmt);
    con.commit();
    pStmt.close();

    stmt = con.createStatement();
    // Write the dictionary to the database.
    stmt.execute("CREATE TABLE " + dictionary_table + " " + "(mention TEXT NOT NULL, entity INTEGER NOT NULL, prior FLOAT DEFAULT 0.0, entitytype INTEGER NOT NULL)");
    stmt.close();
    logger.info("Writing to DB");

    Map<Integer, EntityType> isNamedEntity = new HashMap<>();
    addEntriesToDB(entries, isNamedEntity, con, entityIds);

    stmt = con.createStatement();
    stmt.execute("CREATE TABLE " + entities_table + " " + "(entity INTEGER NOT NULL, namedentity INTEGER NOT NULL)");
    stmt.close();

    pStmt = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + entities_table + "(entity, namedentity) VALUES(?, ?)", 10000);
    inserts = 0;
    for(Entry<Integer, EntityType> entity: isNamedEntity.entrySet()) {
      pStmt.setInt(1, entity.getKey());
      pStmt.setInt(2, entity.getValue().getDBId());
      if (++inserts % 1000000 == 0) {
        logger.info(inserts + " entitities added");
      }
      DBUtil.addBatch(pStmt);
    }
    DBUtil.executeBatch(pStmt);
    con.commit();
    pStmt.close();


    logger.info("Dictionary done");
    String sql = null;
    stmt = con.createStatement();
    logger.info("Started storing entity-ids in the DB");
    sql = "CREATE TABLE " + DataAccessSQL.ENTITY_IDS + "(entity TEXT, knowledgebase TEXT, id INTEGER)";
    stmt.execute(sql);
    stmt.close();
    pStmt = DBUtil
        .getAutoExecutingPeparedStatement(con, "INSERT INTO " + DataAccessSQL.ENTITY_IDS + "(entity, knowledgebase, id)  VALUES(?, ?, ?)", 10000);
    inserts = 0;

    for (TObjectIntIterator<KBIdentifiedEntity> itr = entityIds.iterator(); itr.hasNext();) {
      itr.advance();
      pStmt.setString(1, itr.key().getIdentifier());
      pStmt.setString(2, itr.key().getKnowledgebase());
      pStmt.setInt(3, itr.value());
      DBUtil.addBatch(pStmt);

      if (++inserts % 1000000 == 0) {
        logger.info(inserts + " entity-ids added");
      }
    }
    DBUtil.executeBatch(pStmt);
    con.commit();
    pStmt.close();

    logger.info("Gathering stats");
    stmt = con.createStatement();
    stmt.execute("ALTER TABLE " + dictionary_table + " ALTER mention SET STATISTICS 1000");
    stmt.execute("ALTER TABLE " + dictionary_table + " ALTER entity SET STATISTICS 1000");

    logger.info("Creating Indexes");
    stmt.execute("CREATE INDEX dictionary_language_e_idx ON " + DataAccessSQL.ENTITY_LANGUAGES + " USING btree (entity)");
    stmt.execute("CREATE INDEX dictionary_language_l_idx ON " + DataAccessSQL.ENTITY_LANGUAGES + " USING btree (language)");
    stmt.execute("CREATE INDEX dictionary_idx ON " + dictionary_table + " USING btree (mention, entity, prior, entitytype)");
    stmt.execute("CREATE INDEX dictionary_eidx ON " + dictionary_table + " USING btree (entity)");
    stmt.execute("CREATE INDEX entities_idx ON " + entities_table + " USING btree (entity)");
    sql = "CREATE INDEX entity_id_eindex ON " + DataAccessSQL.ENTITY_IDS + " USING btree (entity, knowledgebase);";
    stmt.execute(sql);
    sql = "CREATE INDEX entity_id_iindex ON " + DataAccessSQL.ENTITY_IDS + " using btree (id);";
    stmt.execute(sql);
    con.commit();
    stmt.close();

    try {
      stmt = con.createStatement();
      sql = "CREATE EXTENSION pg_trgm";
      stmt.execute(sql);
      stmt.close();
      stmt = con.createStatement();
      sql = "CREATE INDEX " + dictionary_table + "_trgmidx " + "ON " + dictionary_table + " USING gist (mention gist_trgm_ops)";
      stmt.execute(sql);
      stmt.close();
    } catch (SQLException e) {
      logger.error(
          "Could not create trgm index, infix queries will be slow (this does not affect the regular AIDA performance). " + "SQL was: '" + sql + "', "
              + "error was: " + e.getMessage());
    }



    stmt.close();
    con.commit();
    con.setAutoCommit(true);
    EntityLinkingManager.releaseConnection(con);
  }

  private void addEntriesToDB(
          Map<String, TObjectIntHashMap<DictionaryEntity>> dictionaryEntries,
          Map<Integer, EntityType> isNamedEntity, Connection con,
          TObjectIntHashMap<KBIdentifiedEntity> entityIds) throws SQLException {
    PreparedStatement pStmt = DBUtil.getAutoExecutingPeparedStatement(con, "INSERT INTO " + dictionary_table + " (mention, entity, prior, entitytype) VALUES (?,?,?,?)", 1_000_000);
    int inserts = 0;
    for (String mention : dictionaryEntries.keySet()) {
      TObjectIntHashMap<DictionaryEntity> priors = dictionaryEntries.get(mention);
      TObjectIntHashMap<DictionaryEntityBase> priorsWithoutSourceAndLanguage = new TObjectIntHashMap<>(priors.size()); // Transform all dictionary entities to the base class to ignore language and source
      for (TObjectIntIterator<DictionaryEntity> it = priors.iterator(); it.hasNext();) {
        it.advance();
        DictionaryEntityBase deb = DictionaryEntityBase.getDictionaryEntityBase(it.key().entity, it.key().knowledgebase, it.key().isNamedEntity);
        priorsWithoutSourceAndLanguage.put(deb, it.value());
      }

      int totalTargetsConcept = 0;
      int totalTargetsNamedEntity = 0;
      TObjectIntHashMap<DictionaryEntityBase> entity2CountsMap = new TObjectIntHashMap<>();

      // Get total targets for normalization.

      //first extract the counts per entity not per DictionaryEntity
      //entities should have the same prior regardless of their source
      for (TObjectIntIterator<DictionaryEntityBase> it = priorsWithoutSourceAndLanguage.iterator(); it.hasNext();) {
        it.advance();
        entity2CountsMap.put(it.key(), it.value());
      }

      for (TObjectIntIterator<DictionaryEntityBase> it = entity2CountsMap.iterator(); it.hasNext();) {
        it.advance();
        if(it.key().isNamedEntity.equals(EntityType.NAMED_ENTITY)) {
          totalTargetsNamedEntity += it.value();
        } else if (it.key().isNamedEntity.equals(EntityType.CONCEPT)) {
          totalTargetsConcept += it.value();
        } else {
          totalTargetsNamedEntity += it.value();
          totalTargetsConcept += it.value();
        }
      }


      // Add normalized prior to db.
      for (TObjectIntIterator<DictionaryEntityBase> it = priorsWithoutSourceAndLanguage.iterator(); it.hasNext();) {
        it.advance();
        DictionaryEntityBase targetEntity = it.key();
        float priorNE = 0.0f;
        float priorC = 0.0f;

        if (!it.key().isNamedEntity.equals(EntityType.CONCEPT) && totalTargetsNamedEntity > 0) {
          priorNE = (float) it.value() / (float) totalTargetsNamedEntity;
        }
        if (!it.key().isNamedEntity.equals(EntityType.NAMED_ENTITY) && totalTargetsConcept > 0) {
          priorC = (float) it.value() / (float) totalTargetsConcept;
        }

        //This is to avoid having repeated rows, the source is droped while constructing the ids so this makes sure that entities are unique
        int id = entityIds.get(KBIdentifiedEntity.getKBIdentifiedEntity(targetEntity.entity, targetEntity.knowledgebase));
        isNamedEntity.put(id, targetEntity.isNamedEntity);
        if(targetEntity.isNamedEntity.equals(EntityType.NAMED_ENTITY)) {
          addDictionaryEntry(pStmt, mention, id, priorNE, targetEntity.isNamedEntity);
        } else if(targetEntity.isNamedEntity.equals(EntityType.CONCEPT)) {
          addDictionaryEntry(pStmt, mention, id, priorC, targetEntity.isNamedEntity);
        } else {
          addDictionaryEntry(pStmt, mention, id, priorNE, EntityType.NAMED_ENTITY);
          addDictionaryEntry(pStmt, mention, id, priorC, EntityType.CONCEPT);
        }

        if (++inserts % 1000000 == 0) {
          logger.info(inserts + " entities added");
        }
      }
    }

    // execute last batch
    DBUtil.executeBatch(pStmt);
    con.commit();
    pStmt.close();
  }

  private void computeEntityIds(Map<String, TObjectIntHashMap<DictionaryEntity>> dictionaryEntries, TObjectIntHashMap<KBIdentifiedEntity> entityIds) {
    int entityId = entityIds.size() + 1;
    // Get int ids for entities and mentions, starting with 1.
    for (Entry<String, TObjectIntHashMap<DictionaryEntity>> e : dictionaryEntries.entrySet()) {
      // All entities are added as-is.
      for (DictionaryEntity entity : e.getValue().keySet()) {
        if (!entityIds.containsKey(KBIdentifiedEntity.getKBIdentifiedEntity(entity.entity, entity.knowledgebase))) {
          entityIds.put(KBIdentifiedEntity.getKBIdentifiedEntity(entity.entity, entity.knowledgebase), entityId++);
        }
      }
    }
  }

  private void addDictionaryEntry(PreparedStatement pStmt, String mention, int id, float prior, EntityType type) throws SQLException {
    pStmt.setString(1, mention);
    pStmt.setInt(2, id);
    pStmt.setFloat(3, prior);
    pStmt.setInt(4, type.getDBId());
    DBUtil.addBatch(pStmt);
  }
}