package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class TrainingSettings implements Serializable {
    private static final long serialVersionUID = -4569336180879376473L;
    private static final Logger logger = LoggerFactory.getLogger(TrainingSettings.class);

    public static final String DEFAULT_FEATURE_EXTRACTOR = "features.extractor";
    public static final String DEFAULT_SALIENCE_ANNOTATOR = "salience.annotator";
    public static final String DEFAULT_CLASSIFICATION_METHOD = "classification.method";
    public static final String DEFAULT_CLASSIFICATION_STRATEGY = "classification.strategy";

    private static final String MAX_CATEGORIES="training.maxCategories";
    private static final String POSITIVE_INSTANCE_SCALING_FACTOR="training.positiveInstanceScalingFactor";

    public static final String SEED ="seed";
    public static final String TREE_MAX_DEPTH="tree.maxDepth";
    public static final String TREE_MAX_BINS ="tree.maxBins";
    public static final String TREE_NUM_TREES="tree.numTrees";
    public static final String TREE_SUBSAMPLING_RATE="tree.subsamplingRate";
    public static final String TREE_FEATURE_SUBSET_STRATEGY="tree.featureSubsetStrategy";
    public static final String TREE_IMPURITY="tree.impurity";
    public static final String TREE_NUM_TREES_X="tree.numTreesX";
    public static final String TREE_MAX_DEPTH_X="tree.maxDepthX";
    public static final String TREE_SUBSAMPLING_RATE_X="tree.subsamplingRateX";

    public static final String MAX_ITERATIONS="maxIterations";
    public static final String MAX_ITERATIONS_X="maxIterationsX";

    public static final String LR_REG_PARAM="lr.regParam";
    public static final String LR_ELASTIC_NET_PARAM="lr.elasticNetParam";
    public static final String LR_REG_PARAM_CV="lr.regParamCV";
    public static final String LR_ELASTIC_NET_PARAM_CV="lr.elasticNetParamCV";

    public static final String MPC_LAYERS="mpc.layers";
    public static final String MPC_BLOCK_SIZE="mpc.blockSize";

    public static final String EVALUATION_NUM_FOLDS ="evaluation.numFolds";
    public static final String EVALUATION_METRIC_NAME="evaluation.metricName";

    public static final String COHERENT_DOCUMENT="disambiguation.coherent";
    public static final String CONFIDENCE_TRESHOLD="disambiguation.confidenceThreshold";

    public static final String BIGRAM_COUNT_CACHE="aida.bigram_count";
    public static final String KEYWORD_COUNT_CACHE="aida.keyword_count";
    public static final String WORD_EXPANSIONS_CACHE="aida.word_expansions";
    public static final String WORD_CONTRACTIONS_CACHE="aida.word_contactions";
    public static final String CASSANDRA_CONFIG="aida.cassandra_config";
    public static final String DATABASE_AIDA="aida.db.database_aida";
    public static final String AIDA_CACHE_ROOT_HDFS="aida.cache_root_hdfs";
    public static final String AIDA_DEFAULT_CONF="aida.default_conf";

    private Properties properties;

    private static TrainingSettings trainingSettings;

    public TrainingSettings() {
        try {
            properties = ConfigUtils.loadProperties("salience.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TrainingSettings getInstance() {
        if (trainingSettings == null) {
            trainingSettings = new TrainingSettings();
        }
        return trainingSettings;
    }

    public enum FeatureExtractor {
        ENTITY_SALIENCE, ANNOTATE_AND_ENTITY_SALIENCE
    }

    public enum EntitySalienceEntityAnnotator {
        DISAMBIGUATION
    }

    public enum ClassificationMethod {
         LOG_REG, GBT, RANDOM_FOREST, DECISION_TREE, MPC, ONE_VS_ALL
    }

    public enum ClassificationStrategy {
        STANDARD, RANKED, SALIENCE
    }

    private Boolean isDocumentCoherent;
    private Double documentConfidenceThreshold;

    private FeatureExtractor featureExtractor;
    private EntitySalienceEntityAnnotator entitySalienceEntityAnnotator;     /** Which annotators to run [default: all] */
    private ClassificationMethod classificationMethod;
    private ClassificationStrategy classificationStrategy;

    //Random Forest settings
    private Long seed;
    private Integer maxBins;
    private Integer numTrees;
    private Integer maxDepth;
    private Double subsamplingRate;
    private String featureSubsetStrategy;
    private String impurity;

    //ParamMap for cross validation
    private int[] numTreesX;
    private int[] maxDepthX;
    private double[] subsamplingRateX;

    private Integer maxIterations;
    private int[] maxIterationsX;


    //Logistic Regression
    private Double lrRegParam;
    private Double lrElasticNetParam;
    //ParamMap for cross validation
    private double[] lrRegParamCV;
    private double[] lrElasticNetParamCV;

    private int[] mpcLayers;
    private Integer blockSize;

    private Integer numFolds;
    private String metricName;

    private Integer maxCategories;
    private Integer positiveInstanceScalingFactor;

    private String aidaDefaultConf;
    private String bigramCountCache;
    private String keywordCountCache;
    private String wordExpansionsCache;
    private String wordContractionsCache;
    private String cassandraConfig;
    private String databaseAida;
    private String aidaCacheRootHdfs;


    private String getValue(String key) {
        if(properties.getProperty(key) != null && !properties.getProperty(key).equals("")) {
            return properties.getProperty(key);
        }
        return null;
    }

    private void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

    public static String get(String key) {
        String value = TrainingSettings.getInstance().getValue(key);
        if (value == null) {
            logger.error("Missing key in properties file with no default value: " + key);
        }
        return value;
    }

    public static int getAsInt(String key) {
        String value = get(key);
        return Integer.parseInt(value);
    }

    public static Integer getAsInteger(String key) {
        String value = get(key);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    public static Long getAsLong(String key) {
        String value = get(key);
        if (value != null) {
            return Long.parseLong(value);
        }
        return null;
    }

    public static Double getAsDouble(String key) {
        String value = get(key);
        if(value != null) {
            return Double.parseDouble(value);
        }
        return null;
    }

    public static Boolean getAsBoolean(String key) {
        String value = get(key);
        if(value != null) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    public static int[] getAsIntArray(String key) {
        String value = get(key);
        if(value != null) {
            String[] values = value.split(",");
            int[] intValues = new int[values.length];
            for(int i=0; i<values.length; i++) {
                intValues[i] = Integer.parseInt(values[i]);
            }
            return intValues;
        }
        return null;
    }

    public static double[] getAsDoubleArray(String key) {
        String value = get(key);
        if(value != null) {
            String[] values = value.split(",");
            double[] doubleValues = new double[values.length];
            for(int i=0; i<values.length; i++) {
                doubleValues[i] = Double.parseDouble(values[i]);
            }
            return doubleValues;
        }
        return null;
    }

    public FeatureExtractor getFeatureExtractor() {
        if(featureExtractor == null) {
            featureExtractor = FeatureExtractor.valueOf(getValue(DEFAULT_FEATURE_EXTRACTOR));
        }
        return featureExtractor;
    }

    public void setFeatureExtractor(FeatureExtractor featureExtractor) {
        this.featureExtractor = featureExtractor;
    }

    public EntitySalienceEntityAnnotator getEntitySalienceEntityAnnotator() {
        if(entitySalienceEntityAnnotator == null) {
            entitySalienceEntityAnnotator = EntitySalienceEntityAnnotator.valueOf(getValue(DEFAULT_SALIENCE_ANNOTATOR));
        }
        return entitySalienceEntityAnnotator;
    }

    public void setEntitySalienceEntityAnnotator(EntitySalienceEntityAnnotator entitySalienceEntityAnnotator) {
        this.entitySalienceEntityAnnotator = entitySalienceEntityAnnotator;
    }

    public ClassificationMethod getClassificationMethod() {
        if(classificationMethod == null) {
            classificationMethod = ClassificationMethod.valueOf(getValue(DEFAULT_CLASSIFICATION_METHOD));
        }
        return classificationMethod;
    }

    public void setClassificationMethod(ClassificationMethod classificationMethod) {
        this.classificationMethod = classificationMethod;
    }

    public ClassificationStrategy getClassificationStrategy() {
        if(classificationStrategy == null) {
            classificationStrategy = ClassificationStrategy.valueOf(getValue(DEFAULT_CLASSIFICATION_STRATEGY));
        }
        return classificationStrategy;
    }

    public void setClassificationStrategy(ClassificationStrategy classificationStrategy) {
        this.classificationStrategy = classificationStrategy;
    }

    public Boolean getDocumentCoherent() {
        if(isDocumentCoherent == null) {
            isDocumentCoherent = getAsBoolean(COHERENT_DOCUMENT);
        }
        return isDocumentCoherent;
    }

    public void setDocumentCoherent(Boolean documentCoherent) {
        isDocumentCoherent = documentCoherent;
    }

    public Double getDocumentConfidenceThreshold() {
        if(documentConfidenceThreshold == null) {
            getAsDouble(CONFIDENCE_TRESHOLD);
        }
        return documentConfidenceThreshold;
    }

    public void setDocumentConfidenceThreshold(Double documentConfidenceThreshold) {
        this.documentConfidenceThreshold = documentConfidenceThreshold;
    }


    public Long getSeed() {
        if(seed == null) {
            seed = getAsLong(SEED);
        }
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Integer getMaxBins() {
        if(maxBins == null) {
            maxBins = getAsInteger(TREE_MAX_BINS);
        }
        return maxBins;
    }

    public void setMaxBins(Integer maxBins) {
        this.maxBins = maxBins;
    }

    public Integer getNumTrees() {
        if(numTrees == null) {
            numTrees = getAsInteger(TREE_NUM_TREES);
        }
        return numTrees;
    }

    public void setNumTrees(Integer numTrees) {
        this.numTrees = numTrees;
    }

    public Integer getMaxDepth() {
        if(maxDepth == null) {
            maxDepth = getAsInteger(TREE_MAX_DEPTH);
        }
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Double getSubsamplingRate() {
        if(subsamplingRate == null) {
            subsamplingRate = getAsDouble(TREE_SUBSAMPLING_RATE);
        }
        return subsamplingRate;
    }

    public void setSubsamplingRate(Double subsamplingRate) {
        this.subsamplingRate = subsamplingRate;
    }

    public String getFeatureSubsetStrategy() {
        if(featureSubsetStrategy == null) {
            featureSubsetStrategy = getValue(TREE_FEATURE_SUBSET_STRATEGY);
        }
        return featureSubsetStrategy;
    }

    public void setFeatureSubsetStrategy(String featureSubsetStrategy) {
        this.featureSubsetStrategy = featureSubsetStrategy;
    }

    public String getImpurity() {
        if(impurity == null) {
            impurity = getValue(TREE_IMPURITY);
        }
        return impurity;
    }

    public void setImpurity(String impurity) {
        this.impurity = impurity;
    }

    public int[] getNumTreesX() {
        if(numTreesX == null) {
            numTreesX = getAsIntArray(TREE_NUM_TREES_X);
        }
        return numTreesX;
    }

    public void setNumTreesX(int[] numTreesX) {
        this.numTreesX = numTreesX;
    }

    public int[] getMaxDepthX() {
        if(maxDepthX == null) {
            maxDepthX = getAsIntArray(TREE_MAX_DEPTH_X);
        }
        return maxDepthX;
    }

    public void setMaxDepthX(int[] maxDepthX) {
        this.maxDepthX = maxDepthX;
    }

    public double[] getSubsamplingRateX() {
        if(subsamplingRateX == null) {
            subsamplingRateX = getAsDoubleArray(TREE_SUBSAMPLING_RATE_X);
        }
        return subsamplingRateX;
    }

    public void setSubsamplingRateX(double[] subsamplingRateX) {
        this.subsamplingRateX = subsamplingRateX;
    }

    public Double getLrRegParam() {
        if(lrRegParam == null && getAsDouble(LR_REG_PARAM) != null) {
            lrRegParam = getAsDouble(LR_REG_PARAM);
        }
        return lrRegParam;
    }

    public void setLrRegParam(Double lrRegParam) {
        this.lrRegParam = lrRegParam;
    }

    public Double getLrElasticNetParam() {
        if(lrElasticNetParam == null && getAsDouble(LR_ELASTIC_NET_PARAM) != null) {
            lrElasticNetParam = getAsDouble(LR_ELASTIC_NET_PARAM);
        }
        return lrElasticNetParam;
    }

    public void setLrElasticNetParam(Double lrElasticNetParam) {
        this.lrElasticNetParam = lrElasticNetParam;
    }


    public double[] getLrRegParamCV() {
        if(lrRegParamCV == null && getAsDoubleArray(LR_REG_PARAM_CV) != null) {
            lrRegParamCV = getAsDoubleArray(LR_REG_PARAM_CV);
        }
        return lrRegParamCV;
    }

    public void setLrRegParamCV(double[] lrRegParamCV) {
        this.lrRegParamCV = lrRegParamCV;
    }

    public double[] getLrElasticNetParamCV() {
        if(lrElasticNetParamCV == null && getAsDoubleArray(LR_ELASTIC_NET_PARAM_CV) != null) {
            lrElasticNetParamCV = getAsDoubleArray(LR_ELASTIC_NET_PARAM_CV);
        }
        return lrElasticNetParamCV;
    }

    public void setLrElasticNetParamCV(double[] lrElasticNetParamCV) {
        this.lrElasticNetParamCV = lrElasticNetParamCV;
    }

    public Integer getMaxIterations() {
        if(maxIterations == null) {
            maxIterations = getAsInteger(MAX_ITERATIONS);
        }
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int[] getMaxIterationsX() {
        if(maxIterationsX == null) {
            maxIterationsX = getAsIntArray(MAX_ITERATIONS_X);
        }
        return maxIterationsX;
    }

    public void setMaxIterationsX(int[] maxIterationsX) {
        this.maxIterationsX = maxIterationsX;
    }

    public int[] getMpcLayers() {
        if(mpcLayers == null) {
            mpcLayers = getAsIntArray(MPC_LAYERS);
        }
        return mpcLayers;
    }

    public void setMpcLayers(int[] mpcLayers) {
        this.mpcLayers = mpcLayers;
    }

    public Integer getBlockSize() {
        if(blockSize == null) {
            blockSize = getAsInteger(MPC_BLOCK_SIZE);
        }
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public Integer getNumFolds() {
        if(numFolds == null) {
            numFolds = getAsInteger(EVALUATION_NUM_FOLDS);
        }
        return numFolds;
    }

    public void setNumFolds(Integer numFolds) {
        this.numFolds = numFolds;
    }

    public Integer getMaxCategories() {
        if(maxCategories == null) {
            maxCategories = getAsInteger(MAX_CATEGORIES);
        }
        return maxCategories;
    }

    public void setMaxCategories(Integer maxCategories) {
        this.maxCategories = maxCategories;
    }

    public String getMetricName() {
        if(metricName == null) {
            metricName = getValue(EVALUATION_METRIC_NAME);
        }
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Integer getPositiveInstanceScalingFactor() {
        if(positiveInstanceScalingFactor == null) {
            positiveInstanceScalingFactor = getAsInteger(POSITIVE_INSTANCE_SCALING_FACTOR);
        }
        return positiveInstanceScalingFactor;
    }

    public void setPositiveInstanceScalingFactor(Integer positiveInstanceScalingFactor) {
        this.positiveInstanceScalingFactor = positiveInstanceScalingFactor;
    }

    public String getAidaDefaultConf() {
        if(aidaDefaultConf == null) {
            aidaDefaultConf = getValue(AIDA_DEFAULT_CONF);
        }
        return aidaDefaultConf;
    }

    public void setAidaDefaultConf(String aidaDefaultConf) {
        this.aidaDefaultConf = aidaDefaultConf;
    }

    public String getBigramCountCache() {
        if(bigramCountCache == null) {
            bigramCountCache = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(BIGRAM_COUNT_CACHE);
        }
        return bigramCountCache;
    }

    public void setBigramCountCache(String bigramCountCache) {
        this.bigramCountCache = bigramCountCache;
    }

    public String getKeywordCountCache() {
        if(keywordCountCache == null) {
            keywordCountCache = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(KEYWORD_COUNT_CACHE);
        }
        return keywordCountCache;
    }

    public void setKeywordCountCache(String keywordCountCache) {
        this.keywordCountCache = keywordCountCache;
    }

    public String getWordExpansionsCache() {
        if(wordExpansionsCache == null) {
            wordExpansionsCache = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(WORD_EXPANSIONS_CACHE);
        }
        return wordExpansionsCache;
    }

    public void setWordExpansionsCache(String wordExpansionsCache) {
        this.wordExpansionsCache = wordExpansionsCache;
    }

    public String getWordContractionsCache() {
        if(wordContractionsCache == null) {
            wordContractionsCache = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(WORD_CONTRACTIONS_CACHE);
        }
        return wordContractionsCache;
    }

    public void setWordContractionsCache(String wordContractionsCache) {
        this.wordContractionsCache = wordContractionsCache;
    }

    public String getCassandraConfig() {
        if(cassandraConfig == null) {
            cassandraConfig = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(CASSANDRA_CONFIG);
        }
        return cassandraConfig;
    }

    public void setCassandraConfig(String cassandraConfig) {
        this.cassandraConfig = cassandraConfig;
    }

    public String getDatabaseAida() {
        if(databaseAida == null) {
            databaseAida = getAidaCacheRootHdfs()+getAidaDefaultConf()+"/"+getValue(DATABASE_AIDA);
        }
        return databaseAida;
    }

    public void setDatabaseAida(String databaseAida) {
        this.databaseAida = databaseAida;
    }

    public String getAidaCacheRootHdfs() {
        if(aidaCacheRootHdfs == null) {
            aidaCacheRootHdfs = getValue(AIDA_CACHE_ROOT_HDFS);
        }
        return aidaCacheRootHdfs;
    }

    public void setAidaCacheRootHdfs(String aidaCacheRootHdfs) {
        this.aidaCacheRootHdfs = aidaCacheRootHdfs;
    }
}
