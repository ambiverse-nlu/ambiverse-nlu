package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConf;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf.DataPrepConfName;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.ConceptCategoryDicionatriesBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesDicionatriesBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportance.EntityImportanceBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent.EntityImportanceComponentsBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityoccurrence.EntityOccurrenceCountsBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence.EntityKeyphraseCooccurrenceBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyvaluestore.KeyValueStorePreparator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.KnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql.KeyphraseCountCollector;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql.MaterializeMIWeights;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.sql.UnitsStatCollector;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.unitcooccurrence.EntityUnitCooccurrenceBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfigurator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.training.NerTrainer;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

/**
 * Creates the AIDA database tables from the YAGO2 facts table.
 *
 *  Put all the methods that prepare databases or gather
 *  data from some input here, together with
 *  information on how the input must look like, and what
 *  the output is used for!
 */
public class PrepareData {

  private static final Logger logger = LoggerFactory.getLogger(PrepareData.class);

  private DataPrepConf conf;

  private File workingDirectory;

  public enum STAGE {
    ALL, MINIMAL, DICTIONARY, ENTITY_METADATA, META,
    INLINKS, KEYPHRASES, KEYPHRASE_WEIGHTS, UNITS, SUPERDOC, RANK,
    IMPORTANCE_COMPONENTS, TAXONOMY, TYPE, DICTIONARY_TERNARY_TREE,
    DMAP_CREATION, ELASTICSEARCH_CREATION, CASSANDRA_CREATION, CONCEPT_CATEGORIES, KNOWNER_PREPARE_RESOURCES, KNOWNER_TRAIN_MODEL
  }


  private Set<STAGE> stages = new HashSet<STAGE>();

  private STAGE[] minimalStages = {
      STAGE.DICTIONARY,
      STAGE.ENTITY_METADATA,
      STAGE.META,
      STAGE.INLINKS,
      STAGE.SUPERDOC,
      STAGE.KEYPHRASES,
      STAGE.UNITS,
      STAGE.RANK,
      STAGE.TAXONOMY,
      STAGE.TYPE};

  private Set<STAGE> minimalStageConfig = new HashSet<>();

  public PrepareData(DataPrepConf conf, String workingDirectoryPath, String stage) throws SQLException {
    this.conf = conf;
    this.workingDirectory = new File(workingDirectoryPath);

    if (!workingDirectory.exists()) {
      throw new IllegalArgumentException("'" + workingDirectoryPath + "' does not exist, please create.");
    }
    if (!workingDirectory.canWrite()) {
      throw new IllegalArgumentException("'" + workingDirectoryPath + "' is not writable.");
    }
    for (STAGE lightStage : minimalStages) {
      minimalStageConfig.add(lightStage);
    }
    stages = getStagesFromInput(stage);
  }

  private Set<STAGE> getStagesFromInput(String stageString) {
    Set<STAGE> temp = new HashSet<PrepareData.STAGE>();
    String[] stageStrings = stageString.split(",");
    for (String s : stageStrings) {
      STAGE stage = STAGE.valueOf(s);
      if (stage.equals(STAGE.MINIMAL)) {
        temp.addAll(minimalStageConfig);
      } else {
        temp.add(stage);
      }
    }
    return temp;
  }

  private boolean shouldDoStage(STAGE stage) {
    if (!conf.needsStage(stage)) {
      return false;
    }
    if (stages.contains(STAGE.ALL)) {
      logger.debug("All stages should be done, running " + stage);
      return true;
    } else if (stages.contains(stage)) {
      return true;
    } else {
      return false;
    }
  }

  public void prepare(String confName) throws Exception {
    if (shouldDoStage(STAGE.DICTIONARY)) {
      // Create the database.
      if (conf.shouldCreateDb() &&
              !EntityLinkingManager.createDatabaseIfNeeded(EntityLinkingManager.DB_AIDA)) {
        System.out.println("Database already exists, aborting as not to overwrite anything.");
        System.exit(2);
      }

      logger.info("Materializing dicitionay with prior probabilities");
      new DictionaryBuilder(conf.getDictionaryEntriesProvider(), conf.getDictionaryEntriesProviderToComputePrior()).run();
      logger.info("Materializing dicitionay with prior probabilities DONE");
    }

    if (shouldDoStage(STAGE.ENTITY_METADATA)) {
      logger.info("Materializing entities metadata (human readable representation and URL)");
      new EntitiesMetaDataBuilder(conf.getEntitiesMetaDataEntriesProvider()).run();
      logger.info("Materializing entities metadata DONE");
    }

    if (shouldDoStage(STAGE.META)) {
      logger.info("Storing Metadata");
      storeMetadata(confName);
      logger.info("Storing Metadata DONE");
    }

    if (shouldDoStage(STAGE.INLINKS)) {
      logger.info("Materializing inlinks");
      new InlinksBuilder(conf.getInlinksEntriesProviders()).run();
      logger.info("Materializing inlinks DONE");
    }

    if (shouldDoStage(STAGE.RANK)) {
      logger.info("Ranking entities");
      new EntityImportanceBuilder(conf.getEntitiesImportanceEntriesProviders()).run();
      logger.info("Ranking entities DONE");
    }

    if (shouldDoStage(STAGE.IMPORTANCE_COMPONENTS)) {
      logger.info("Importance Component");
      new EntityImportanceComponentsBuilder(conf.getEntitiesImportanceComponentsEntriesProviders()).run();
      logger.info("Importance Component DONE");
    }

    if (shouldDoStage(STAGE.TAXONOMY)) {
      logger.info("Materializing Type Taxonomy");
      new TypeTaxonomyBuilder(conf.getTypeTaxonomyEntriesProviders()).run();
      logger.info("Materializing Type Taxonomy DONE");
    }

    if (shouldDoStage(STAGE.TYPE)) {
      logger.info("Materializing Entities Types");
      new EntitiesTypesDicionatriesBuilder(conf.getEntitiesTypesEntriesProviders()).run();
      logger.info("Materliazing Entities Types DONE");
    }

    // If YAGO is changed and has concept taxanomy we can remove this, we won't need it then probably.
    if (shouldDoStage(STAGE.CONCEPT_CATEGORIES)) {
      logger.info("Materializing Concept Categories");
      new ConceptCategoryDicionatriesBuilder(conf.getConceptCategoriesEntriesProviders()).run();
      logger.info("Materliazing Concept Categories DONE");
    }

    if (shouldDoStage(STAGE.KEYPHRASES)) {
      logger.info("Collecting keyphrases");
      new EntitiesContextBuilder(conf.getEntitiesContextProviders()).run();
      new KeyphraseCountCollector().run();
      logger.info("Collecting Keyphrases DONE");
    }

    if (shouldDoStage(STAGE.SUPERDOC)) {
      logger.info("Calculating entities cooccurrence statistics");
      new EntityOccurrenceCountsBuilder(conf.getEntitiesOccurrenceCountsProviders()).run();
      logger.info("Calculating entities cooccurrence statistics DONE");
      logger.info("Calculating superdoc keyphrase frequencies");
      new EntityKeyphraseCooccurrenceBuilder(conf.getEntityKeyphrasesOccurrenceEntriesProviders()).run();
      logger.info("Calculating superdoc keyphrase frequencies DONE");
    }

    if (shouldDoStage(STAGE.KEYPHRASE_WEIGHTS)) {
      logger.info("Pre-Computing Weights for keyphrases");
      new MaterializeMIWeights()
          .run(DataAccessSQL.KEYPHRASE_COUNTS, "keyphrase", DataAccessSQL.ENTITY_KEYPHRASES, "keyphrase", conf.getKeyphraseMIType());
      logger.info("Pre-Computing Weights for keyphrases DONE");
    }

    if (shouldDoStage(STAGE.UNITS)) {
      logger.info("Calculating unit frequencies");
      UnitsStatCollector unitsStatCollector = new UnitsStatCollector();
      if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS))
        unitsStatCollector.startCollectingStatsInMemory(UnitType.BIGRAM);
      if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_KEYWORDS))
        unitsStatCollector.startCollectingStatsInMemory(UnitType.KEYWORD);
      logger.info("Calculating unit frequencies DONE");
      logger.info("Calculating unit-entities cooccurrence statistics");
      new EntityUnitCooccurrenceBuilder(conf.getEntityUnitsOccurrenceEntriesProviders()).run();
      logger.info("Calculating unit-entities cooccurrence statistics DONE");
    }

    if (shouldDoStage(STAGE.CASSANDRA_CREATION)) {
      logger.info("Transforming Postgres to Cassandra");
      KeyValueStorePreparator.getDefault(DataAccess.type.cassandra).generateKeyValueStores();
      logger.info("Transforming Postgres to Cassandra DONE");
    }

    if(shouldDoStage(STAGE.KNOWNER_PREPARE_RESOURCES)) {
      logger.info("Starting preparing language resources for KnowNER");
        for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {

        KnowNERLanguageConfigurator knowNERLanguageConfigurator = new KnowNERLanguageConfiguratorBuilder()
                .setLanguage(language.name())
                .setMainDir(KnowNERSettings.getLangResourcesLocation())
                .setCreateNew(AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.KNOWNER_CREATENEW))
                .withDefaultResourceCheckers()
                .create();
        List<String> missingResources = knowNERLanguageConfigurator.run();
        if (!missingResources.isEmpty()) {
          throw new RuntimeException("Missing resources for KnowNER " + language + ": " + missingResources);
        }
        logger.info("NER configuration for language " + language + " was prepared");
      }
      logger.info("Preparing language configurations for KnowNER DONE");
    }

    if(shouldDoStage(STAGE.KNOWNER_TRAIN_MODEL)) {
      logger.info("Starting training models for KnowNER");
      for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {
        for (String type : new String[]{"KB", "NED"}) {
          NerTrainer nerTrainer = new NerTrainer(new String[] {
                  "-l", language.name(),
                  "-t", "BMEOW",
                  "-c", "default",
                  "-f", "ner/new/" + type + ".properties",
                  "-d", KnowNERSettings.getDumpName(),
                  "-m", "TEST"});

          String modelTitle = nerTrainer.run();
          logger.info("Ner model " + modelTitle + " has been trained");
        }
      }


      logger.info("Training models for KnowNER DONE");
    }

  }

  /* The inputs are:
   * - entity_keyphrases
   * - keyphrase_counts
   * - entity_counts
   * - entity_keywords
   * - keywords_counts
   * 
   * The method updates the weight columns in entity_keyphrases and
   * entity_keywords with the Normalized Pointwise MI weights.
   */
  private void precomputeKeytermWeights() throws SQLException, EntityLinkingDataAccessException {
    // Compute the NPMI weights.
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_KEYWORDS)) precomputeUnitWeights(UnitType.KEYWORD);
    if (AIDASchemaPreparationConfig.getBoolean(AIDASchemaPreparationConfig.DATABASE_CREATE_BIGRAMS)) precomputeUnitWeights(UnitType.BIGRAM);

    // Add the keyword weights to the entity_keyphrase table.
    // NOT USED ANYMORE IN THE NEW NORMALIZED SCHEMA
    //    new EntityKeyphraseTokenWeightImporter().run();
  }

  private void precomputeUnitWeights(UnitType unitType) throws SQLException, EntityLinkingDataAccessException {
    new MaterializeMIWeights()
        .run(unitType.getUnitCountsTableName(), unitType.getUnitName(), unitType.getEntityUnitCooccurrenceTableName(), unitType.getUnitName(),
            conf.getUnitMIType(unitType));
  }

  private void storeMetadata(String confName) throws SQLException, IOException, EntityLinkingDataAccessException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    String sql = "CREATE TABLE " + DataAccessSQL.METADATA + "(key TEXT, value TEXT)";
    stmt.execute(sql);

    // AIDA Metadata.
    String sizeString = String.valueOf(DataAccess.getAllEntityIds().size());
    sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('collection_size', " + sizeString + ")";
    stmt.execute(sql);

    sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('confName', '" + confName + "')";
    stmt.execute(sql);

    for (Entry<String, String> ds : conf.getDataSources().entrySet()) {
      sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('datasource:" + ds.getKey() + "', '" + ds.getValue() + "')";
      stmt.execute(sql);
    }

    sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('creationDate', '" + new Date() + "')";
    stmt.execute(sql);

    StringJoiner languages = new StringJoiner(",");
    List<String> languageList = new ArrayList();

    // KB metadata.
    KnowledgeBaseMetaDataProvider kbMetadataProvider = conf.getKnowledgeBaseMetaDataProvider();
    for (Entry<String, String> metadataPair : kbMetadataProvider.getMetaData().entrySet()) {
      String key = "KB:" + metadataPair.getKey();
      String value = metadataPair.getValue();
      if (metadataPair.getKey().contains("WikipediaSource")) {
        languages.add(value.substring(1, 3));
        languageList.add(value.substring(1, 3));
      }
      sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('" + key + "', '" + value + "')";
      stmt.execute(sql);
    }

    sql = "INSERT INTO " + DataAccessSQL.METADATA + " VALUES ('language', '" + languages.toString() + "')";
    stmt.execute(sql);

    stmt.close();
    EntityLinkingManager.releaseConnection(con);
    storeLanguages(languageList);
    storeEntityTypes();
  }

  private void storeLanguages(List<String> languages) throws SQLException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    String sql = "CREATE TABLE " + DataAccessSQL.LANGUAGES + "(key INT, value TEXT)";
    stmt.execute(sql);
    for (String language : languages) {
      sql = "INSERT INTO " + DataAccessSQL.LANGUAGES + " VALUES ('" + Language.getLanguageForString(language).getID() + "', '" + language
          + "')";
      stmt.execute(sql);
    }
    stmt.close();
    EntityLinkingManager.releaseConnection(con);
  }

  private void storeEntityTypes() throws SQLException {
    Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
    Statement stmt = con.createStatement();
    String sql = "CREATE TABLE " + DataAccessSQL.ENTITY_CLASSES + "(key INT, value TEXT)";
    stmt.execute(sql);
    for (EntityType et : EntityType.values()) {
      sql = "INSERT INTO " + DataAccessSQL.ENTITY_CLASSES + " VALUES ('" + et.ordinal() + "', '" + et.name()
              + "')";
      stmt.execute(sql);
    }
    stmt.close();
    EntityLinkingManager.releaseConnection(con);
  }

  public static void addBatch(PreparedStatement stmt) {
    try {
      stmt.addBatch();
    } catch (Exception e) {
      System.err.println("Error when adding batch: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

  public static void executeBatch(PreparedStatement stmt) {
    try {
      stmt.executeBatch();
    } catch (Exception e) {
      System.err.println("Error when executing batch: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Throwable {
    if (args.length < 1 || args.length > 2) {
      System.out.println("Usage: PrepareData <working_directory> [optional-stages comma-separted]");
      System.exit(1);
    }

    EntityLinkingManager.init();

    String confName = AIDASchemaPreparationConfig.getConfigurationName();
    String dir = args[0];

    String stage = "ALL";
    if (args.length == 2) {
      stage = args[1];
    }

    DataPrepConf conf = DataPrepConfFactory.getConf(DataPrepConfName.valueOf(confName));

    new PrepareData(conf, dir, stage).prepare(confName);
    try {
      EntityLinkingManager.shutDown();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }
}
