package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ExternalEntitiesContext;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LanguageModelContext extends EntitiesContext implements Serializable {

  private Collection<UnitType> unitTypes;

  private int collectionSize_;

  private List<TIntObjectHashMap<TIntIntHashMap>> entityUnitCounts;

  public LanguageModelContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }

  public LanguageModelContext() throws Exception {
    //bean.
  }

  public LanguageModelContext(Entities entities, ExternalEntitiesContext externalContext, EntitiesContextSettings settings) throws Exception {
    super(entities, externalContext, settings);
  }

  @Override public int[] getContext(Entity entity) {
    return entityUnitCounts.get(entity.getId()).keys();
  }

  @Override protected void setupEntities(Entities entities) throws Exception {
    collectionSize_ = DataAccess.getCollectionSize();
    entityUnitCounts = new ArrayList<>(UnitType.values().length);
    for (UnitType unitType : UnitType.values()) {
      entityUnitCounts.add(unitType.ordinal(), DataAccess.getEntityUnitIntersectionCount(entities, unitType));
    }
  }

  public int getEntityUnitCount(Entity entity, int unit, UnitType unitType) {
    TIntObjectHashMap<TIntIntHashMap> curEntityUnitCounts = entityUnitCounts.get(unitType.ordinal());
    if (curEntityUnitCounts == null) return 0;
    TIntIntHashMap curUnitCounts = curEntityUnitCounts.get(entity.getId());
    if (curUnitCounts == null) return 0;
    return curUnitCounts.get(unit);
  }

  public int getUnitCount(int unit, UnitType unitType) {
    return DataAccess.getUnitDocumentFrequency(unit, unitType);
  }

  public TIntIntMap getUnitCountsForEntity(Entity entity, UnitType unitType) {
    TIntObjectHashMap<TIntIntHashMap> curEntityUnitCounts = entityUnitCounts.get(unitType.ordinal());
    if (curEntityUnitCounts == null) return new TIntIntHashMap();
    TIntIntHashMap curUnitCounts = curEntityUnitCounts.get(entity.getId());
    return curUnitCounts != null ? curUnitCounts : new TIntIntHashMap();
  }

  public int getCollectionSize() {
    return collectionSize_;
  }

  public double getSmoothingParameter(UnitType unitType) {
    return settings.getSmoothingParameter(unitType);
  }

  public int contractTerm(int wordId) throws EntityLinkingDataAccessException {
    return DataAccess.contractTerm(wordId);
  }

  public boolean shouldIgnoreMention(UnitType unitType) {
    return settings.shouldIgnoreMention(unitType);
  }

  public Collection<UnitType> getUnitTypes() {
    return unitTypes;
  }

  public void setUnitTypes(Collection<UnitType> unitTypes) {
    this.unitTypes = unitTypes;
  }

  public int getCollectionSize_() {
    return collectionSize_;
  }

  public void setCollectionSize_(int collectionSize_) {
    this.collectionSize_ = collectionSize_;
  }

  public List<TIntObjectHashMap<TIntIntHashMap>> getEntityUnitCounts() {
    return entityUnitCounts;
  }

  public void setEntityUnitCounts(List<TIntObjectHashMap<TIntIntHashMap>> entityUnitCounts) {
    this.entityUnitCounts = entityUnitCounts;
  }
}
