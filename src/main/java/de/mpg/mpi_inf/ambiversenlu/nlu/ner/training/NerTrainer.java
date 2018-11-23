package de.mpg.mpi_inf.ambiversenlu.nlu.ner.training;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.Conll2003AidaReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.OrderType;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.NerTrainingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.CorpusConfiguration;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.ConllEvaluation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.KnowNEREvaluation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.readers.Conll2003ReaderTc;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.readers.Conll2003ReaderTcBmeow;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.Experiment_ImplBase;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings.buildModelTitle;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.*;
import static java.util.Arrays.asList;

//import org.dkpro.tc.crfsuite.CRFSuiteAdapter;


public class NerTrainer {

    private static final Logger logger = LoggerFactory.getLogger(NerTrainer.class);

    private static final BatchTask.ExecutionPolicy EXECUTION_POLICY = BatchTask.ExecutionPolicy.RUN_AGAIN;
    private static final String FEATURE_MODE = Constants.FM_SEQUENCE;
    private static final String LEARNING_MODE = Constants.LM_SINGLE_LABEL;

    private final KnowNEREvaluation.EntityStandard entityStandard;
    private final String fullModelTitle;
    private final boolean useLemmatizer;
    private final boolean usePOSTagger;
    private final OrderType orderType;
    private final String language;

    private final NerTrainingConfig config;
    private final KnowNEREvaluation.TrainingMode mode;
    private final boolean evaluate;
    private final ParameterSpace parameterSpace;

    private final ConllEvaluation.TrainedPositionType positionType;
    private final String corpus;
    private final String parentDirectory;

    public NerTrainer(String[] args) throws ParseException, IOException, ResourceInitializationException {
        CommandLine cmd;
        try {
            cmd = new CommandLineUtils().parseCommandLineArgs(args);

        } catch (ParseException e) {
            logger.error("Failed to parse the command line");
            throw e;
        }

        config = new NerTrainingConfig(cmd.getOptionValue(Option.FILE.name));

        language = cmd.getOptionValue(Option.LANGUAGE.name);
        mode = KnowNEREvaluation.TrainingMode.valueOf(cmd.getOptionValue(Option.MODE.name).toUpperCase());

        evaluate = cmd.hasOption(Option.EVALUATION.name);

        positionType = ConllEvaluation.TrainedPositionType.valueOf(cmd.getOptionValue(Option.TRAINED_POSITION_TYPE.name));

        corpus = cmd.getOptionValue(Option.CORPUS.name);
        String langResourcesPath = KnowNERSettings.getLangResourcesPath(corpus, language);
        orderType = KnowNERTrainingUtils.getCorpusConfigurationForLanguage(corpus, KnowNERLanguage.valueOf(language)).getCorpusFormat();

        if (cmd.hasOption(Option.ENTITY_STANDARD.name)) {
            entityStandard = KnowNEREvaluation.EntityStandard.valueOf(cmd.getOptionValue(Option.ENTITY_STANDARD.name).toUpperCase());

        } else if (config.getFeatures()
                .stream()
                .anyMatch(s -> s.equals("BmeowTag"))) {
            entityStandard = orderType.hasEntity()? KnowNEREvaluation.EntityStandard.GOLD : KnowNEREvaluation.EntityStandard.AIDA;
            logger.warn("An option -s was not provided for ENTITY_STANDARD type, so deciding on the type based on the corpus configuration: " + entityStandard);
//            logger.error("An option -{} should be provided, because the feature {} is used",
//                    Option.ENTITY_STANDARD.name,
//                    "BmeowTag");
//            throw new MissingOptionException(Collections.singletonList(Option.ENTITY_STANDARD.name));
        } else {
            entityStandard = null;
        }
        parameterSpace = initParameterSpace();

        String prefix = cmd.hasOption(Option.PREFIX.name) ? cmd.getOptionValue(Option.PREFIX.name) : null;
        parentDirectory = cmd.hasOption(Option.PREFIX_DIR.name) ? cmd.getOptionValue(Option.PREFIX_DIR.name) : null;

        System.setProperty("DKPRO_HOME", KnowNERSettings.getDkproHome());

        fullModelTitle = buildModelTitle(prefix, corpus, language, config.getModelTitle(), mode, entityStandard, positionType);

        useLemmatizer = KnowNERLanguage.requiresLemma(language) && !orderType.hasLemma();
        usePOSTagger = !orderType.hasPOS();
    }

    public String run() throws Exception {

        logger.info("Starting training of the model {}", config.getModelTitle());

        File model = storeModel();

        if (evaluate) {
            new KnowNEREvaluation(fullModelTitle, corpus, language, config.getFeatures(),
                    entityStandard, useLemmatizer, usePOSTagger, mode, positionType)
                    .evaluateModel(model);
        }

        return fullModelTitle;
    }

    private File storeModel() throws Exception {
        String modelPath = KnowNERSettings.getTrainedModelDirectory(parentDirectory, fullModelTitle);
        Files.createDirectories(Paths.get(modelPath));

        logger.info("Storing the model {} to {}", fullModelTitle, modelPath);

        File outputFolder = new File(modelPath);
        ExperimentSaveModel experiment = new ExperimentSaveModel(fullModelTitle,
                CRFSuiteAdapter.class, outputFolder);

        experiment.setParameterSpace(parameterSpace);
        experiment.setExecutionPolicy(EXECUTION_POLICY);
        experiment.setPreprocessing(KnowNERTrainingUtils.getPreprocessing(language, config.getFeatures(), entityStandard, useLemmatizer, usePOSTagger));

        runExperiment(experiment);
        return outputFolder;
    }

    private String runExperiment(Experiment_ImplBase experiment) throws Exception {
        Lab lab = Lab.getInstance();
        return lab.run(experiment);
    }

    private ParameterSpace initParameterSpace() throws IOException, ResourceInitializationException {

        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<>();
//        choosing which position type we use for training
        Class<? extends ResourceCollectionReaderBase> trainReaderClass =
                positionType == ConllEvaluation.TrainedPositionType.ORIGINAL ?
                        Conll2003ReaderTc.class : Conll2003ReaderTcBmeow.class;
        int[] trainSetRange = KnowNERTrainingUtils.getDatasetRangeForCorpus(corpus, KnowNERLanguage.valueOf(language),
                mode == KnowNEREvaluation.TrainingMode.TEST ? CorpusConfiguration.Range.TRAINA : CorpusConfiguration.Range.TRAIN);

        dimReaders.put(Constants.DIM_READER_TRAIN, CollectionReaderFactory.createReaderDescription(trainReaderClass,
                PARAM_LANGUAGE, language,
                "readerClassName", Conll2003AidaReader.class,
                Conll2003AidaReader.PARAM_SINGLE_FILE, true,
                Conll2003AidaReader.PARAM_ORDER, orderType,
                PARAM_SOURCE_LOCATION, KnowNERSettings.getLangResourcesPath(corpus, language),
                PARAM_PATTERNS, CorpusConfiguration.DEFAULT_FILE_NAME,
                Conll2003AidaReader.PARAM_FIRSTDOCUMENT, trainSetRange[0],
                Conll2003AidaReader.PARAM_LASTDOCUMENT, trainSetRange[1]));


        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension.create("pipelineParameters",
                asList(config.getPipelineParameters()));

        @SuppressWarnings("unchecked")
        /* If no algorithm is provided, CRFSuite takes lbfgs*/
                Dimension<List<String>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
                asList(new String[]{CRFSuiteAdapter.ALGORITHM_LBFGS, "-p feature.possible_states=1"}));


        @SuppressWarnings("unchecked")
        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
                config.getFeatureSet());

        return new ParameterSpace(
                Dimension.createBundle("readers", dimReaders),
                Dimension.create(Constants.DIM_LEARNING_MODE, LEARNING_MODE),
                Dimension.create(Constants.DIM_FEATURE_MODE, FEATURE_MODE),
                Dimension.create(Constants.DIM_FEATURE_STORE, SparseFeatureStore.class.getName()),
                dimPipelineParameters,
                dimFeatureSets,
                dimClassificationArgs
        );
    }

    public static void main(String[] args) throws Exception {
        String modelTitle = new NerTrainer(args).run();
        logger.info("NER model has been trained: " + modelTitle);
    }
}
