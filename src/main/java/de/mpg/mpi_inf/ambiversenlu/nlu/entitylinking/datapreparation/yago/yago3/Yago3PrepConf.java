package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData.STAGE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.aida.AIDAManualDictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConf;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance.EntityImportanceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent.EntityImportanceComponentEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence.EntityOccurrenceCountsEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence.EntityKeyphraseCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.KnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityKeyphraseCooccurrenceDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityOccurrenceCountsDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityUnitCooccurrenceDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoInlinkBasedEntityImportanceCountsDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3DBReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3SplittingTsvFileReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation.MI_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Yago3PrepConf extends DataPrepConf {

  public static final String YAGO3 = "YAGO3";

  private static final String FACTS_TABLENAME = "yagofacts";

  private Logger logger = LoggerFactory.getLogger(Yago3PrepConf.class);

  protected YAGO3Reader yago3Reader;

  public Yago3PrepConf() {
    String yago3FileLocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.YAGO3_FILE);

    if (yago3FileLocation != null) {
      logger.info("Reading YAGO3 FROM FILE:" + yago3FileLocation);
      yago3Reader = new YAGO3SplittingTsvFileReader(new File(yago3FileLocation));
    } else {
      logger.info("YAGO3 WILL BE READ FROM DATABASE");
      yago3Reader = new YAGO3DBReader(FACTS_TABLENAME);
    }
  }

  @Override public List<DictionaryEntriesDataProvider> getDictionaryEntriesProvider() throws IOException {
    List<DictionaryEntriesDataProvider> dictionaryEntriesProviders = new LinkedList<DictionaryEntriesDataProvider>();
    dictionaryEntriesProviders.add(new Yago3DictionaryEntriesDataProvider(yago3Reader));
    dictionaryEntriesProviders.add(new Yago3AnchorsDictionaryEntriesDataProvider(yago3Reader));
    dictionaryEntriesProviders.add(new AIDAManualDictionaryEntriesDataProvider());
    //dictionaryEntriesProviders.add(new FreebaseKnownAsDictionaryEntriesProviderForYago3(yago3Reader));
    return dictionaryEntriesProviders;
  }

  @Override public List<DictionaryEntriesDataProvider> getDictionaryEntriesProviderToComputePrior() throws IOException {
    List<DictionaryEntriesDataProvider> dictionaryEntriesProvidersToComputePrior = new LinkedList<DictionaryEntriesDataProvider>();
    dictionaryEntriesProvidersToComputePrior.add(new Yago3AnchorsDictionaryEntriesDataProvider(yago3Reader));
    return dictionaryEntriesProvidersToComputePrior;
  }

  @Override public List<EntitiesContextEntriesDataProvider> getEntitiesContextProviders() throws IOException {
    List<EntitiesContextEntriesDataProvider> contextEntriesProviders = new LinkedList<EntitiesContextEntriesDataProvider>();

    EntitiesContextEntriesDataProvider hasCitationTitleDataProvider = new Yago3RelationBasedEntitiesContextDataProvider(yago3Reader,
        YAGO3RelationNames.hasCitationTitle, DataAccess.KPSOURCE_CITATION);

    EntitiesContextEntriesDataProvider hasWikipediaCategoryDataProvider = new Yago3WikipediaCategoryEntitiesContextDataProvider(yago3Reader, DataAccess.KPSOURCE_CATEGORY);

    EntitiesContextEntriesDataProvider hasWikipediaAnchorTextDataProvider = new Yago3RelationBasedEntitiesContextDataProvider(yago3Reader,
        YAGO3RelationNames.hasWikipediaAnchorText, DataAccess.KPSOURCE_LINKANCHOR);

    EntitiesContextEntriesDataProvider hasInternalWikipediaLinkToDataProvider = new Yago3InlinkTitlesEntitiesContextDataProvider(yago3Reader,
        DataAccess.KPSOURCE_INLINKTITLE);

    contextEntriesProviders.add(hasCitationTitleDataProvider);
    contextEntriesProviders.add(hasWikipediaCategoryDataProvider);
    contextEntriesProviders.add(hasWikipediaAnchorTextDataProvider);
    contextEntriesProviders.add(hasInternalWikipediaLinkToDataProvider);

    return contextEntriesProviders;
  }

  @Override public List<EntityOccurrenceCountsEntriesDataProvider> getEntitiesOccurrenceCountsProviders() {
    List<EntityOccurrenceCountsEntriesDataProvider> providers = new LinkedList<EntityOccurrenceCountsEntriesDataProvider>();
    providers.add(new YagoEntityOccurrenceCountsDataProvider());
    return providers;
  }

  @Override public List<InlinksEntriesDataProvider> getInlinksEntriesProviders() {
    List<InlinksEntriesDataProvider> providers = new LinkedList<InlinksEntriesDataProvider>();
    providers.add(new Yago3InlinksDataProvider(yago3Reader));
    return providers;
  }

  @Override public List<EntityKeyphraseCooccurrenceEntriesDataProvider> getEntityKeyphrasesOccurrenceEntriesProviders() {
    List<EntityKeyphraseCooccurrenceEntriesDataProvider> providers = new LinkedList<EntityKeyphraseCooccurrenceEntriesDataProvider>();
    providers.add(new YagoEntityKeyphraseCooccurrenceDataProvider());
    return providers;
  }

  @Override public List<EntitiesTypesEntriesDataProvider> getEntitiesTypesEntriesProviders() {
    List<EntitiesTypesEntriesDataProvider> providers = new LinkedList<EntitiesTypesEntriesDataProvider>();
    providers.add(new Yago3EntitiesTypesDataProvider(yago3Reader));
    return providers;
  }
  
  @Override public List<EntitiesTypesEntriesDataProvider> getConceptCategoriesEntriesProviders() {
    List<EntitiesTypesEntriesDataProvider> providers = new LinkedList<EntitiesTypesEntriesDataProvider>();
    providers.add(new Yago3EntitiesWikidataCategoryDataProvider(yago3Reader));
    return providers;
  }

  @Override public List<TypeTaxonomyEntriesDataProvider> getTypeTaxonomyEntriesProviders() {
    List<TypeTaxonomyEntriesDataProvider> providers = new LinkedList<TypeTaxonomyEntriesDataProvider>();
    providers.add(new Yago3TypeTaxonomyDataProvider(yago3Reader));
    return providers;
  }

  @Override public List<EntityImportanceEntriesDataProvider> getEntitiesImportanceEntriesProviders() {
    List<EntityImportanceEntriesDataProvider> providers = new LinkedList<EntityImportanceEntriesDataProvider>();
    providers.add(new YagoInlinkBasedEntityImportanceCountsDataProvider(YAGO3));
    return providers;
  }

  @Override public List<EntitiesMetaDataEntriesDataProvider> getEntitiesMetaDataEntriesProvider() {
    List<EntitiesMetaDataEntriesDataProvider> providers = new LinkedList<EntitiesMetaDataEntriesDataProvider>();
    providers.add(new Yago3EntitiesMetaDataDataProvider(yago3Reader));
    return providers;
  }

  @Override public KnowledgeBaseMetaDataProvider getKnowledgeBaseMetaDataProvider() {
    return new Yago3KnowledgeBaseMetaDataProvider(yago3Reader);
  }

  @Override public MI_TYPE getUnitMIType(UnitType unitType) {
    return MI_TYPE.NORMALIZED_POINTWISE_MUTUAL_INFORMATION;
  }

  @Override public MI_TYPE getKeyphraseMIType() {
    return MI_TYPE.NORMALIZED_POINTWISE_MUTUAL_INFORMATION;
  }

  @Override public List<EntityImportanceComponentEntriesDataProvider> getEntitiesImportanceComponentsEntriesProviders() {
    return new LinkedList<EntityImportanceComponentEntriesDataProvider>();
  }

  @Override public boolean needsStage(STAGE stage) {
    //All stages are needed for YAGO
    return true;
  }

  @Override public Map<String, String> getDataSources() {
    Map<String, String> sources = new HashMap<>();
    try {
      String yago3FileLocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.YAGO3_FILE);
      if (yago3FileLocation != null) {
        sources.put("YAGO3", yago3FileLocation);
      } else {
        sources.put("YAGO3", EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_YAGO).getMetaData().getURL());
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return sources;
  }

  @Override
  public List<WikiLinkLikelihoodProvider> getWikiLinkProbabilitiesProviders() {
    List<WikiLinkLikelihoodProvider> providers = new LinkedList<>();
    providers.add(new Yago3WikiLinkProbabilitiesProvider(yago3Reader));
    return providers;
  }

  @Override public List<EntityUnitCooccurrenceEntriesDataProvider> getEntityUnitsOccurrenceEntriesProviders() {
    List<EntityUnitCooccurrenceEntriesDataProvider> result = new LinkedList<>();
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS))
      result.add(new YagoEntityUnitCooccurrenceDataProvider(UnitType.BIGRAM));
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_KEYWORDS))
      result.add(new YagoEntityUnitCooccurrenceDataProvider(UnitType.KEYWORD));
    return result;
  }
}
