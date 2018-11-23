package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.UnitBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Context;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class InputTextWrapper {
  private static final Logger logger = LoggerFactory.getLogger(InputTextWrapper.class);

  int[] units;

  TIntIntMap unitCounts;

  int numOfUnits;

  Mention mentionToIgnore;

  UnitType unitType;

  public InputTextWrapper(Context context, UnitType unitType, boolean removeStopwords) throws EntityLinkingDataAccessException {
    logger.debug("Wrapping input text.");
    mentionToIgnore = null;
    this.unitType = unitType;
    int unitLength = unitType.getUnitSize();
    if (context.getTokenCount() < unitLength) return;
    List<String> unitStrings = new ArrayList<>(context.getTokenCount());
    Queue<String> curTokens = new ArrayDeque<>(unitLength);
    String[] curTokensArray = new String[unitLength];
    for (String token : context.getTokens()) {
      curTokens.add(token);
      if (curTokens.size() == unitLength || (!curTokens.isEmpty() && curTokens.size() - 1 == unitLength)) {
        unitStrings.add(UnitBuilder.buildUnit(curTokens.toArray(curTokensArray)));
        curTokens.remove();
      }
    }

    logger.debug("Get ids for words.");
    TObjectIntHashMap<String> wordIds = DataAccess.getIdsForWords(unitStrings);
    units = new int[unitStrings.size()];
    unitCounts = new TIntIntHashMap((int) (wordIds.size() / Constants.DEFAULT_LOAD_FACTOR), Constants.DEFAULT_LOAD_FACTOR);
    numOfUnits = 0;
    for (int i = 0; i < unitStrings.size(); i++) {
      int unitId = wordIds.get(unitStrings.get(i));
      if (unitId == 0) continue;

      logger.debug("Get contract term for unit id {}.", unitId);
      int contractedUnitId = DataAccess.contractTerm(unitId);
      if (contractedUnitId != 0) unitId = contractedUnitId;
      if (removeStopwords && StopWord.isStopwordOrSymbol(unitId, Language.getLanguageForString("en")))  continue;
      units[i] = unitId;
      unitCounts.adjustOrPutValue(unitId, 1, 1);
      numOfUnits++;
    }
  }

  public int getUnitCount(int unit) {
    return unitCounts.get(unit);
  }

  public int getSize() {
    if (mentionToIgnore == null) return numOfUnits;
    else {
      return numOfUnits - ((unitType.getUnitSize() == 1 ? 0 : 3 - unitType.getUnitSize()) + (mentionToIgnore.getEndToken() - mentionToIgnore
          .getStartToken()));
    }
  }

  public int[] getUnits() {
    // If there is no context, return empty array.
    if (unitCounts == null) {
      return new int[0];
    }

    if (mentionToIgnore == null) return unitCounts.keys();
    else {
      int[] result = unitCounts.keys();
      int start = mentionToIgnore.getStartToken() - (unitType.getUnitSize() == 1 ? 0 : unitType.getUnitSize() - 2);
      int end = start + unitType.getUnitSize() + (mentionToIgnore.getEndToken() - mentionToIgnore.getStartToken()) - 1;
      for (int i = start; i < end; i++) {
        for (int j = 0; j < result.length; j++) {
          if (result[j] == units[i]) {
            result[j] = 0;
            break;
          }
        }
      }
      return result;
    }
  }

  public int[] getUnitsInContext() {
    return units;
  }

  public void mentionToIgnore(Mention mention) {
    mentionToIgnore = mention;
  }
}
