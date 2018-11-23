package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.LanguageModelContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.KLDivergenceCalculator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Context;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.UnitMeasureTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.InputTextWrapper;
import gnu.trove.map.TIntIntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LanguageModelMentionEntitySimilarityMeasure extends MentionEntitySimilarityMeasure {

  private static final Logger logger = LoggerFactory.getLogger(LanguageModelMentionEntitySimilarityMeasure.class);

  protected InputTextWrapper originalInputText;

  private boolean removeStopwords = true;

  private UnitType unitType;

  private boolean normalized;

  private boolean isTracing = false;

  public LanguageModelMentionEntitySimilarityMeasure(Tracer tracer, UnitType unitType, boolean normalized) {
    super(tracer);

    this.unitType = unitType;
    this.normalized = normalized;

    isTracing = !(tracer instanceof NullTracer);
  }

  @Override public double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext)
      throws EntityLinkingDataAccessException {
    if (!(entitiesContext instanceof LanguageModelContext)) {
      logger.warn("LanguageModelMentionEntitySimilarityMeasure#calcSimilarity() "
          + "was invoked with an EntitiesContext that's not a LanguageModelContext => " + "returning 0.0 as similarity");
      return 0d;
    }
    LanguageModelContext languageModelContext = (LanguageModelContext) entitiesContext;

    if (originalInputText == null) {
      logger.debug("Calculating similarity, original input text is null.");
      originalInputText = new InputTextWrapper(context, unitType, removeStopwords);
    }
    if (languageModelContext.shouldIgnoreMention(unitType)) originalInputText.mentionToIgnore(mention);

    KLDivergenceCalculator klDivergenceCalculator = new KLDivergenceCalculator(normalized);
    TIntIntMap unitCountsForEntity = languageModelContext.getUnitCountsForEntity(entity, unitType);

    UnitMeasureTracer mt = null;
    if (isTracing) mt = new UnitMeasureTracer(getIdentifier(), 0.0, unitCountsForEntity.size());

    int entityUnitsSum = 0;
    for (int unitCount : unitCountsForEntity.values()) {
      entityUnitsSum += unitCount;
    }

    int unitGlobalCount, unitEntityCount;
    for (int unit : originalInputText.getUnits()) {
      if (unit == 0) continue;
      unitGlobalCount = languageModelContext.getUnitCount(unit, unitType);
      unitEntityCount = unitCountsForEntity.get(unit);
      // this check makes sure that the unit exist for this unitType
      if (unitGlobalCount == 0) continue;
      double summand = -klDivergenceCalculator
          .addSummand(originalInputText.getUnitCount(unit), originalInputText.getSize(), unitEntityCount, entityUnitsSum,
              languageModelContext.getUnitCount(unit, unitType), languageModelContext.getCollectionSize(),
              languageModelContext.getSmoothingParameter(unitType));
      if (mt != null) mt.addUnitTraceInfo(unit, summand, unitEntityCount != 0);
    }

    if (mt != null) {
      mt.setScore(-klDivergenceCalculator.getKLDivergence());
      tracer.addMeasureForMentionEntity(mention, entity.getId(), mt);
    }

    return -klDivergenceCalculator.getKLDivergence();
  }
}
