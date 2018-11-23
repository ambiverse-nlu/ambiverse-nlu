package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.CorpusConfiguration;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.ConllEvaluation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.KnowNEREvaluation;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class KnowNERSettings {

    private static KnowNERSettings instance;

    private static final Logger logger = LoggerFactory.getLogger(KnowNERSettings.class);
    private static final Pattern MODEL_TITLE_PATTERN = Pattern.compile("(.+?_)?(.+?)_(.+?)_(.+?)_(.+?)(?:_(.+?))?_(.+)");

//    hacky things for debugging...
    private final Integer mentionTokenFrequencyCountsLimit;
    private final Integer wikilinksProbabilitiesLimit;

    //    contains all the models in production
    private final Map<String, Map<String, Path>> modelPathsMap;
    //   directory to store the newly trained models
    //private final String trainedModelsDirectory;
    //private final String trained_models_evaluation_directory;

    private final String keyspacePattern;
    private final String langResourcesLocation;
    private final String dkproHome;
    private final String conllEvalPath;
    private final String languages;

    private final String dumpName;
    private final Boolean loadFromClassPath;

    private final String CLASSPATH_GENERATED_RESOURCES_LOCATION = "mpi/ner/";
    private final String CLASSPATH_MODELS_PATH = "mpi/ner/";

    private static Properties properties;

    public static synchronized KnowNERSettings getInstance() {
        if (instance == null) {
            try {
                instance = new KnowNERSettings();
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private KnowNERSettings() throws SQLException, IOException {
        logger.info("Configuring KnowNER.");

        long start = System.currentTimeMillis();

        try {
            properties = ConfigUtils.loadProperties("ner.properties");
        } catch (IOException e) {
            throw new RuntimeException("Could not load ner.properties: " + e.getLocalizedMessage());
        }

        mentionTokenFrequencyCountsLimit = Integer.valueOf(properties.getProperty("mention_token_frequency_counts_limit", "0"));
        wikilinksProbabilitiesLimit = Integer.valueOf(properties.getProperty("wikilink_probabilities_limit", "0"));
        loadFromClassPath = Boolean.valueOf((properties.getProperty("load_models_from_classpath", "false")));

        this.dumpName = properties.containsKey("dump_name")? properties.getProperty("dump_name") : EntityLinkingManager.getAidaDbIdentifierLight();
        String classPathModelsPathForDump = CLASSPATH_MODELS_PATH + dumpName + "/models";
        String classPathGeneretatedResourcesLocForDump = CLASSPATH_GENERATED_RESOURCES_LOCATION + dumpName + "/generated_configurations";

        String modelsPath = properties.getProperty("models_path");
        if(loadFromClassPath) { //If the models are stored in the classpath, get the classpath as a folder
            modelsPath =  ResourceUtils.getClasspathAsFolder(classPathModelsPathForDump, true).getPath();
            logger.debug("Transforming classpath {} to directory {}.", classPathModelsPathForDump, modelsPath);
        }

        Map<String, Map<String, Path>> modelPathsMap_;
        try {
            modelPathsMap_ = getDefaultModelPathsMap(Paths.get(modelsPath));
        } catch (Exception e) {
            logger.warn("KnowNER models could not be loaded from '" + modelsPath + "'.");
            modelPathsMap_ = null;
        }
        modelPathsMap = modelPathsMap_;


        this.keyspacePattern = properties.getProperty("keyspace_pattern");

        String langResourcesLocation = properties.getProperty("lang_resources_location");
        if(loadFromClassPath) {
            langResourcesLocation =  ResourceUtils.getClasspathAsFolder(classPathGeneretatedResourcesLocForDump, true).getPath();
            logger.debug("Transforming classpath {} to directory {}.", classPathGeneretatedResourcesLocForDump, langResourcesLocation);
        }

        this.langResourcesLocation = langResourcesLocation + (langResourcesLocation.endsWith("/")? "" : "/");


        String dkproHome = properties.getProperty("dkpro_home");
        this.dkproHome = dkproHome + (dkproHome.endsWith("/") ? "" : "/");

        this.conllEvalPath = properties.getProperty("conll_eval_path");

        this.languages = properties.getProperty("languages");

        long dur = System.currentTimeMillis() - start;

        logger.info("Configured KnowNER in " + dur / 1_000 + "s.");
    }

    private String createEvaluationDirectory() {
        String trainedModelsEvaluationDirectory = properties.getProperty("trained_models_evaluation_directory");
        trainedModelsEvaluationDirectory = trainedModelsEvaluationDirectory +
            (trainedModelsEvaluationDirectory.endsWith("/") ? "" : "/");

        Path trainedModelsEvaluationPath = Paths.get(trainedModelsEvaluationDirectory);
        if (!Files.exists(trainedModelsEvaluationPath)) {
            try {
                Files.createDirectory(trainedModelsEvaluationPath);
            } catch (IOException e) {
                logger.error("Could not create trained_models_evaluation_directory: " + trainedModelsEvaluationPath);
                throw new RuntimeException(e);
            }
        }

        return trainedModelsEvaluationDirectory;
    }

    public String createTrainedModelsDirectory() {
        String trainedModelsDirectory = properties.getProperty("trained_models_directory");
        trainedModelsDirectory = trainedModelsDirectory + (trainedModelsDirectory.endsWith("/") ? "" : "/");
        Path trainedModelsPath = Paths.get(trainedModelsDirectory);
        if (!Files.exists(trainedModelsPath)) {
            try {
                Files.createDirectory(trainedModelsPath);
            } catch (IOException e) {
                logger.error("Could not create trained_models_directory: " + trainedModelsPath);
                throw new RuntimeException(e);
            }
        }


        return trainedModelsDirectory;
    }

    public static String buildModelTitle(String prefix, String corpus, String language, String stage, KnowNEREvaluation.TrainingMode mode,
        KnowNEREvaluation.EntityStandard entityStandard, ConllEvaluation.TrainedPositionType positionType) {
		return (prefix != null ? (prefix + "_") : "")
				+ corpus + "_" + language + "_"
				+ stage + "_"
				+ mode + (entityStandard != null ? ("_" + entityStandard) : "") + "_" + positionType;
	}

    public static String[] parseModelTitle(String modelTitle) {
		Matcher matcher = MODEL_TITLE_PATTERN.matcher(modelTitle);
		if (!matcher.matches()) {
			throw new RuntimeException("Wrong model title! " + modelTitle);
		}
		return IntStream.range(1, 8).mapToObj(matcher::group).toArray(String[]::new);
	}

    public static String parseLanguageFromModelTitle(String modelTitle) {
		String[] split = parseModelTitle(modelTitle);
		return split[2];
	}

    public static String parseCorpusFromModelTitle(String modelTitle) {
		String[] split = parseModelTitle(modelTitle);
		return split[1];
	}

    public static String parseModelFromModelTitle(String modelTitle) {
		String[] split = parseModelTitle(modelTitle);
		return split[3];
	}

	public static String getDumpName() {
        return getInstance().dumpName;
    }

    private Map<String, Map<String, Path>> getDefaultModelPathsMap(Path modelsPath) throws SQLException, IOException, URISyntaxException {
        Map<String, Map<String, Path>> map = new HashMap<>();


        Files.walkFileTree(modelsPath, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                String corpus = parseCorpusFromModelTitle(fileName);
                if (CorpusConfiguration.DEFAULT_CORPUS_NAME.equals(corpus)) {
                    String language = parseLanguageFromModelTitle(fileName);
                    String model = parseModelFromModelTitle(fileName);
                    if (!map.containsKey(language)) {
                        map.put(language, new HashMap<>());
                    }
                    map.get(language).put(model, file);
                }
                return super.visitFile(file, attrs);
            }
        });
        return map;
    }

    public static String getLangResourcesPath(String corpus, String language) {
        if (language == null) throw new IllegalStateException("Please set language");
        return getInstance().langResourcesLocation + language + "/corpus/" + corpus + "/";

    }

    public static String getLangResourcesPath(String language) {
        if (language == null) throw new IllegalStateException("Please set language");
        return getInstance().langResourcesLocation + language + "/";

    }


    public static Integer getMentionTokenFrequencyCountsLimit() {
        return getInstance().mentionTokenFrequencyCountsLimit;
    }

    public static Integer getWikilinksProbabilitiesLimit() {
        return getInstance().wikilinksProbabilitiesLimit;
    }

    public static Path getModelPath(String language, String model) {
        return getInstance().modelPathsMap.get(language).get(model);
    }

    public static String getKeyspace(String language) {
        return KnowNERSettings.getDumpName() + "_" + getInstance().keyspacePattern + language;
    }

    public static String getLangResourcesLocation() {
        return getInstance().langResourcesLocation;
    }

    public static String getDkproHome() {
        return getInstance().dkproHome;
    }

    public static String getModelsEvaluationLocation() {

        return getInstance().createEvaluationDirectory();
    }

    public static String getModelsMentionEvaluationLocation(String modelTitle, String corpus, boolean singleLabelling) {
        return getInstance().createEvaluationDirectory() + "ConllMentionEvaluation_" + modelTitle + "-" + corpus + (singleLabelling? "-single" : "-multi") + ".txt";
    }

    public static String getModelsTokenEvaluationLocation(String modelTitle, String corpus, boolean singleLabelling) {

        return getInstance().createEvaluationDirectory() + "ConllTokenEvaluation_" + modelTitle + corpus + (singleLabelling? "-single" : "-multi") + ".txt";
    }

    public static String getConllEvalPath() {
        return getInstance().conllEvalPath;
    }

    public static String getTrainedModelDirectory(String parentDirectory, String modeltitle) {

        return getInstance().createTrainedModelsDirectory() + (parentDirectory == null? "" : parentDirectory + "/") + modeltitle;
    }

    public static String getLanguages() {
        return getInstance().languages;
    }
}
