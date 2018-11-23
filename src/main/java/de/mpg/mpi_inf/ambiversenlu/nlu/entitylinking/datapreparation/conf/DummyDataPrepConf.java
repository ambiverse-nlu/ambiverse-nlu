package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf;

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
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation.MI_TYPE;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DummyDataPrepConf extends DataPrepConf {

  @Override public List<DictionaryEntriesDataProvider> getDictionaryEntriesProvider() {
    return new LinkedList<DictionaryEntriesDataProvider>();
  }

  @Override public List<DictionaryEntriesDataProvider> getDictionaryEntriesProviderToComputePrior() {
    return new LinkedList<DictionaryEntriesDataProvider>();
  }

  @Override public List<EntitiesContextEntriesDataProvider> getEntitiesContextProviders() {
    return new LinkedList<EntitiesContextEntriesDataProvider>();
  }

  @Override public List<EntityOccurrenceCountsEntriesDataProvider> getEntitiesOccurrenceCountsProviders() {
    return new LinkedList<EntityOccurrenceCountsEntriesDataProvider>();
  }

  @Override public List<InlinksEntriesDataProvider> getInlinksEntriesProviders() {
    return new LinkedList<InlinksEntriesDataProvider>();
  }

  @Override public List<EntityKeyphraseCooccurrenceEntriesDataProvider> getEntityKeyphrasesOccurrenceEntriesProviders() {
    return new LinkedList<EntityKeyphraseCooccurrenceEntriesDataProvider>();
  }

  @Override public List<EntitiesTypesEntriesDataProvider> getEntitiesTypesEntriesProviders() {
    return new LinkedList<EntitiesTypesEntriesDataProvider>();
  }

  @Override public List<EntityImportanceEntriesDataProvider> getEntitiesImportanceEntriesProviders() {
    // Auto-generated method stub
    return null;
  }

  @Override public List<EntitiesMetaDataEntriesDataProvider> getEntitiesMetaDataEntriesProvider() {
    // Auto-generated method stub
    return null;
  }

  @Override public MI_TYPE getUnitMIType(UnitType unitType) {
    return null;
  }

  @Override public MI_TYPE getKeyphraseMIType() {
    return null;
  }

  @Override public List<EntityImportanceComponentEntriesDataProvider> getEntitiesImportanceComponentsEntriesProviders() {
    // Auto-generated method stub
    return null;
  }

  @Override public boolean needsStage(STAGE stage) {
    return true;
  }

  @Override public Map<String, String> getDataSources() {
    // Auto-generated method stub
    return null;
  }

	@Override
	public List<WikiLinkLikelihoodProvider> getWikiLinkProbabilitiesProviders() {
		return new LinkedList<>();
	}

	@Override public List<TypeTaxonomyEntriesDataProvider> getTypeTaxonomyEntriesProviders() {
    // Auto-generated method stub
    return null;
  }

  @Override public List<EntityUnitCooccurrenceEntriesDataProvider> getEntityUnitsOccurrenceEntriesProviders() {
    return new LinkedList<>();
  }

  @Override
  public List<EntitiesTypesEntriesDataProvider> getConceptCategoriesEntriesProviders() {
    // Auto-generated method stub
    return null;
  }
}
