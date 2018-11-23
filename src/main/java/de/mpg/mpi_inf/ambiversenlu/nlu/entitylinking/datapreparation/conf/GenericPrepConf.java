package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData.STAGE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance.EntityImportanceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent.EntityImportanceComponentEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence.EntityOccurrenceCountsEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence.EntityKeyphraseCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.kgmapping.KGMapping;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.DummyKnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.KnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityKeyphraseCooccurrenceDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityOccurrenceCountsDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityUnitCooccurrenceDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoInlinkBasedEntityImportanceCountsDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3PrepConf;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util.WeightComputation.MI_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class GenericPrepConf extends DataPrepConf {
  private static final String FACTS_TABLENAME = "yagofacts";

  private Logger logger = LoggerFactory.getLogger(GenericPrepConf.class);

  private GenericReader genericReader;
  private Yago3PrepConf yago3PrepConf;

  private static KGMapping kgmapping;

  public GenericPrepConf() throws IOException {
    String fileLocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.GENERIC_FILE);

    if (fileLocation != null) {
      logger.info("READING FROM FILE:" + fileLocation);
      genericReader = new GenericSplittingTsvFileReader(new File(fileLocation));
    } else {
      logger.info("DATA WILL BE READ FROM DATABASE");
      genericReader = new GenericDBReader(FACTS_TABLENAME);
    }

    String yago3FileLocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.YAGO3_FILE);

    if (yago3FileLocation != null) {
      yago3PrepConf = new Yago3PrepConf();
    }

    if(kgmapping == null) {
      kgmapping = new KGMapping();
      kgmapping.addKGMapping(genericReader);
    }
  }

  @Override
  public List<DictionaryEntriesDataProvider> getDictionaryEntriesProvider() throws IOException {
    List<DictionaryEntriesDataProvider> dictionaryEntriesProviders = new LinkedList<DictionaryEntriesDataProvider>();
    dictionaryEntriesProviders.add(new GenericDictionaryEntriesDataProvider(genericReader, AIDASchemaPreparationConfig.getKBName()));
    if(yago3PrepConf != null) {
      dictionaryEntriesProviders.addAll(yago3PrepConf.getDictionaryEntriesProvider());
    }
    return dictionaryEntriesProviders;
  }


  @Override
  public List<DictionaryEntriesDataProvider> getDictionaryEntriesProviderToComputePrior() throws IOException {
    return Collections.EMPTY_LIST;
  }

  @Override
  public List<EntitiesContextEntriesDataProvider> getEntitiesContextProviders() throws IOException {
    List<EntitiesContextEntriesDataProvider> contextEntriesProviders = new LinkedList<EntitiesContextEntriesDataProvider>();

    EntitiesContextEntriesDataProvider descriptionDataProvider = new GenericRelationBasedEntitiesContextDataProvider(
        genericReader, Relations.DESCRIPTION.getRelation(), "description", AIDASchemaPreparationConfig.getKBName());
    EntitiesContextEntriesDataProvider contextDataProvider = new GenericRelationBasedEntitiesContextDataProvider(
        genericReader, Relations.CONTEXT.getRelation(), "generic", AIDASchemaPreparationConfig.getKBName());
    EntitiesContextEntriesDataProvider keyPhrasecontextDataProvider = new GenericRelationBasedEntitiesContextDataProvider(
        genericReader, Relations.HAS_KEYPHRASE.getRelation(), "generic", AIDASchemaPreparationConfig.getKBName());

    contextEntriesProviders.add(descriptionDataProvider);
    contextEntriesProviders.add(contextDataProvider);
    contextEntriesProviders.add(keyPhrasecontextDataProvider);
    if(yago3PrepConf != null) {
      contextEntriesProviders.addAll(yago3PrepConf.getEntitiesContextProviders());
    }

    return contextEntriesProviders;
  }

  @Override
  public List<EntityOccurrenceCountsEntriesDataProvider> getEntitiesOccurrenceCountsProviders() {
    List<EntityOccurrenceCountsEntriesDataProvider> providers = new LinkedList<EntityOccurrenceCountsEntriesDataProvider>();
    providers.add(new YagoEntityOccurrenceCountsDataProvider());
    return providers;
  }

  @Override
  public List<InlinksEntriesDataProvider> getInlinksEntriesProviders() {
    List<InlinksEntriesDataProvider> providers = new LinkedList<InlinksEntriesDataProvider>();
    providers.add(new GenericInlinksDataProvider(genericReader, AIDASchemaPreparationConfig.getKBName()));
    if(yago3PrepConf != null) {
      providers.addAll(yago3PrepConf.getInlinksEntriesProviders());
    }
    return providers;
  }

  @Override
  public List<EntityKeyphraseCooccurrenceEntriesDataProvider> getEntityKeyphrasesOccurrenceEntriesProviders() {
    List<EntityKeyphraseCooccurrenceEntriesDataProvider> providers = new LinkedList<EntityKeyphraseCooccurrenceEntriesDataProvider>();
    providers.add(new YagoEntityKeyphraseCooccurrenceDataProvider());
    return providers;
  }

  @Override
  public List<EntitiesTypesEntriesDataProvider> getEntitiesTypesEntriesProviders() {
    List<EntitiesTypesEntriesDataProvider> providers = new LinkedList<EntitiesTypesEntriesDataProvider>();
    providers.add(new GenericEntitiesTypesDataProvider(genericReader, AIDASchemaPreparationConfig.getKBName()));
    if(yago3PrepConf != null) {
      providers.addAll(yago3PrepConf.getEntitiesTypesEntriesProviders());
    }
    return providers;
  }

  @Override public List<EntitiesTypesEntriesDataProvider> getConceptCategoriesEntriesProviders() {
    List<EntitiesTypesEntriesDataProvider> providers = new LinkedList<EntitiesTypesEntriesDataProvider>();
    providers.add(new GenericEntitiesTypesDataProvider(genericReader, AIDASchemaPreparationConfig.getKBName()));
    if(yago3PrepConf != null) {
      providers.addAll(yago3PrepConf.getConceptCategoriesEntriesProviders());
    }
    return providers;
  }

  @Override
  public List<TypeTaxonomyEntriesDataProvider> getTypeTaxonomyEntriesProviders() {
    List<TypeTaxonomyEntriesDataProvider> providers = new LinkedList<>();
    if(yago3PrepConf != null) {
      providers.addAll(yago3PrepConf.getTypeTaxonomyEntriesProviders());
    }
    return providers;
  }

  @Override
  public List<EntityImportanceEntriesDataProvider> getEntitiesImportanceEntriesProviders() {
    List<EntityImportanceEntriesDataProvider> providers = new LinkedList<EntityImportanceEntriesDataProvider>();
    providers.add(new YagoInlinkBasedEntityImportanceCountsDataProvider(AIDASchemaPreparationConfig.getKBName()));
    return providers;
  }

  @Override
  public List<EntitiesMetaDataEntriesDataProvider> getEntitiesMetaDataEntriesProvider() {
    List<EntitiesMetaDataEntriesDataProvider> providers = new LinkedList<EntitiesMetaDataEntriesDataProvider>();
    providers.add(new GenericEntitiesMetaDataDataProvider(genericReader, AIDASchemaPreparationConfig.getKBName()));
    if(yago3PrepConf != null) {
      providers.addAll(yago3PrepConf.getEntitiesMetaDataEntriesProvider());
    }
    return providers;
  }

  @Override
  public List<WikiLinkLikelihoodProvider> getWikiLinkProbabilitiesProviders() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public KnowledgeBaseMetaDataProvider getKnowledgeBaseMetaDataProvider() {
    return new DummyKnowledgeBaseMetaDataProvider();
  }

  @Override
  public MI_TYPE getUnitMIType(UnitType unitType) {
    return MI_TYPE.NORMALIZED_POINTWISE_MUTUAL_INFORMATION;
  }

  @Override
  public MI_TYPE getKeyphraseMIType() {
    return MI_TYPE.NORMALIZED_POINTWISE_MUTUAL_INFORMATION;
  }

  @Override
  public List<EntityImportanceComponentEntriesDataProvider> getEntitiesImportanceComponentsEntriesProviders() {
    return new LinkedList<EntityImportanceComponentEntriesDataProvider>();
  }

  @Override
  public boolean needsStage(STAGE stage) {
    return true;
  }

  @Override
  public Map<String, String> getDataSources() {
    Map<String, String> sources = new HashMap<>();
    try {
      String genericfilelocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.GENERIC_FILE);
      if (genericfilelocation != null) {
        sources.put("GENERIC", genericfilelocation);
      } else {
        sources.put("GENERIC", EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_YAGO)
            .getMetaData().getURL());
      }
      String yago3FileLocation = AIDASchemaPreparationConfig.get(AIDASchemaPreparationConfig.YAGO3_FILE);
      if (yago3PrepConf != null && yago3FileLocation != null) {
        sources.put("YAGO", yago3FileLocation);
      } else if(yago3PrepConf != null) {
        sources.put("YAGO", EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_YAGO)
            .getMetaData().getURL());
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return sources;
  }

  @Override
  public List<EntityUnitCooccurrenceEntriesDataProvider> getEntityUnitsOccurrenceEntriesProviders() {
    List<EntityUnitCooccurrenceEntriesDataProvider> result = new LinkedList<>();
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS))
      result.add(new YagoEntityUnitCooccurrenceDataProvider(UnitType.BIGRAM));
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_KEYWORDS))
      result.add(new YagoEntityUnitCooccurrenceDataProvider(UnitType.KEYWORD));
    return result;
  }

}
