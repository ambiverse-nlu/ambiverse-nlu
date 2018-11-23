package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData.STAGE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance.EntityImportanceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent.EntityImportanceComponentEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence.EntityOccurrenceCountsEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence.EntityKeyphraseCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.DummyKnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.KnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation.MI_TYPE;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class DataPrepConf {

  public abstract List<DictionaryEntriesDataProvider> getDictionaryEntriesProvider() throws IOException, EntityLinkingDataAccessException;

  public abstract List<DictionaryEntriesDataProvider> getDictionaryEntriesProviderToComputePrior() throws IOException;

  public abstract List<EntitiesContextEntriesDataProvider> getEntitiesContextProviders() throws IOException, EntityLinkingDataAccessException;

  public abstract List<EntityOccurrenceCountsEntriesDataProvider> getEntitiesOccurrenceCountsProviders();

  public abstract List<InlinksEntriesDataProvider> getInlinksEntriesProviders() throws EntityLinkingDataAccessException;

  public abstract List<EntityKeyphraseCooccurrenceEntriesDataProvider> getEntityKeyphrasesOccurrenceEntriesProviders();

  public abstract List<EntityUnitCooccurrenceEntriesDataProvider> getEntityUnitsOccurrenceEntriesProviders();

  public abstract List<EntitiesTypesEntriesDataProvider> getEntitiesTypesEntriesProviders();
  
  public abstract List<EntitiesTypesEntriesDataProvider> getConceptCategoriesEntriesProviders();

  public abstract List<TypeTaxonomyEntriesDataProvider> getTypeTaxonomyEntriesProviders();

  public abstract List<EntityImportanceEntriesDataProvider> getEntitiesImportanceEntriesProviders();

  public abstract List<EntityImportanceComponentEntriesDataProvider> getEntitiesImportanceComponentsEntriesProviders();

  public abstract List<EntitiesMetaDataEntriesDataProvider> getEntitiesMetaDataEntriesProvider();

  public KnowledgeBaseMetaDataProvider getKnowledgeBaseMetaDataProvider() {
    return new DummyKnowledgeBaseMetaDataProvider();
  }

  public abstract MI_TYPE getUnitMIType(UnitType unitType);

  public abstract MI_TYPE getKeyphraseMIType();

  public abstract boolean needsStage(STAGE stage);

  public abstract Map<String, String> getDataSources();

  public abstract List<WikiLinkLikelihoodProvider> getWikiLinkProbabilitiesProviders();

  public boolean shouldCreateDb() {
    return true;
  }
}
