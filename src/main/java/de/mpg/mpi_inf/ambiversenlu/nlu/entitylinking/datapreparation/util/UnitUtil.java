package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.UnitBuilder;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Some method so Units are built everywhere in the same way.
 */
public final class UnitUtil {

  private static final Logger logger = LoggerFactory.getLogger(UnitUtil.class);

  private UnitUtil() {

  }

  public static int getUnitId(TIntList unitTokens, TIntObjectHashMap<String> id2word, TObjectIntHashMap<String> word2id) {
    if (unitTokens == null || unitTokens.size() == 0) return 0;
    if (unitTokens.size() == 1) return unitTokens.get(0);
    return word2id.get(UnitBuilder.buildUnit(unitTokens, id2word));
  }

  public static TIntObjectHashMap<TIntHashSet> loadEntityUnits(int unitSize) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<Set<TIntArrayList>> entitiesUnits = new TIntObjectHashMap<>();
    TIntSet usedTokens = new TIntHashSet();
    Connection con = null;
    Statement statement;

    ResultSet r;

    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);

      con.setAutoCommit(false);
      statement = con.createStatement();
      statement.setFetchSize(10_000_000);

      TIntObjectHashMap<int[]> keyphrase_tokens = DataAccess.getAllKeyphraseTokens();
      logger.info("Size of Preloaded keyphrase_tokens : " + keyphrase_tokens.size());
      double minWeight = AIDASchemaPreparationConfig.getDouble(AIDASchemaPreparationConfig.DATABASE_UNIT_CREATION_THRESHOLD_MIN_WEIGHT);
      int topk = AIDASchemaPreparationConfig.getInteger(AIDASchemaPreparationConfig.DATABASE_UNIT_CREATION_THRESHOLD_TOPK);
      String sql;
      if (topk > 0) {
        sql = "SELECT entity, keyphrase FROM ("
            + "SELECT entity, keyphrase, weight, row_number() OVER (PARTITION BY entity ORDER BY weight DESC) AS rank " + "FROM "
            + DataAccessSQL.ENTITY_KEYPHRASES + ") as ekr WHERE rank<=" + topk;
        if (minWeight > -1.0) sql += " AND weight>=" + minWeight;
      } else {
        sql = "SELECT entity, keyphrase FROM " + DataAccessSQL.ENTITY_KEYPHRASES;
        if (minWeight > -1.0) sql += " WHERE weight>=" + minWeight;
      }

      r = statement.executeQuery(sql);
      int count = 0;
      while (r.next()) {
        ++count;
        int entity = r.getInt("entity");
        int keyphrase = r.getInt("keyphrase");
        int[] tokens = keyphrase_tokens.get(keyphrase);
        Set<TIntArrayList> unitSet = entitiesUnits.get(entity);
        if (unitSet == null) entitiesUnits.put(entity, (unitSet = new HashSet<>()));
        getNgram(unitSize, tokens, usedTokens, unitSet);
      }
      logger.info("Finished Reading " + count + " keyphrases!");
      logger.info("entitiesUnits size: " + entitiesUnits.size());
      logger.info("usedTokens size: " + usedTokens.size());
      r.close();
      con.setAutoCommit(true);
    } catch (SQLException e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    TIntObjectHashMap<TIntHashSet> entitiesUnitsIds = new TIntObjectHashMap<>();
    TObjectIntHashMap<String> wordIds = DataAccess.getAllWordIds();
    TIntObjectHashMap<String> idsWords = new TIntObjectHashMap<>(wordIds.size());
    for (TObjectIntIterator<String> itr = wordIds.iterator(); itr.hasNext(); ) {
      itr.advance();
      idsWords.put(itr.value(), itr.key());
    }
    logger.info("Finished idsWords " + idsWords.size() + " ids!");

    TIntObjectHashMap<String> usedWords = getUsedWords(usedTokens, idsWords);
    logger.info("Used words: " + usedWords.size());
    TIntObjectIterator<Set<TIntArrayList>> entitiesUnitTokensIterator = entitiesUnits.iterator();
    while (entitiesUnitTokensIterator.hasNext()) {
      entitiesUnitTokensIterator.advance();
      TIntHashSet units = new TIntHashSet();
      entitiesUnitsIds.put(entitiesUnitTokensIterator.key(), units);
      for (TIntArrayList unit : entitiesUnitTokensIterator.value()) {
        units.add(getUnitId(unit, usedWords, wordIds));
      }
    }
    return entitiesUnitsIds;
  }

  private static TIntObjectHashMap<String> getUsedWords(TIntSet usedTokens, TIntObjectHashMap<String> idsWords) {
    TIntObjectHashMap<String> usedWords = new TIntObjectHashMap<>();
    for (TIntIterator itr = usedTokens.iterator(); itr.hasNext(); ) {
      int usedToken = itr.next();
      usedWords.put(usedToken, idsWords.get(usedToken));
    }
    return usedWords;
  }

  public static void getNgram(int ngram, int[] tokens, TIntSet usedTokens, Set<TIntArrayList> unitSet) {
    if (tokens == null) {
      return;
    }
    int[] unitArray = new int[ngram];
    int unitSizeMinusOne = ngram - 1;
    if (ngram > 1) {
      unitArray[0] = 0;
      // we skip the first element of the firstUnitArray because it should stay 0
      System.arraycopy(tokens, 0, unitArray, 1, unitSizeMinusOne);
      unitSet.add(new TIntArrayList(unitArray));

      unitArray[ngram - 1] = 0;
      // we skip the last element of the lastUnitArray because it should stay 0
      System.arraycopy(tokens, tokens.length - unitSizeMinusOne, unitArray, 0, unitSizeMinusOne);
      unitSet.add(new TIntArrayList(unitArray));
    }
    if (tokens.length >= ngram - 1) {
      for (int i = 0; i < tokens.length - unitSizeMinusOne; i++) {
        System.arraycopy(tokens, i, unitArray, 0, ngram);
        unitSet.add(new TIntArrayList(unitArray));
      }
      if (usedTokens != null) {
        usedTokens.addAll(tokens);
      }
    }
  }
}
