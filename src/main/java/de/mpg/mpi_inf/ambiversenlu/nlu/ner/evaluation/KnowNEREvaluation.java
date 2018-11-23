package de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.Conll2003AidaReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.OrderType;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.CorpusConfiguration;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.training.KnowNERTrainingUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.LocalFeaturesTcAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.SingleLabelAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers.PredictionsWriter;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class KnowNEREvaluation {

    public enum TrainingMode {TEST, DEV}

    public enum EntityStandard {GOLD, AIDA}

    private static final Logger logger = LoggerFactory.getLogger(KnowNEREvaluation.class);
    private final String fullModelTitle;
    private final String language;
    private final String corpus;
    private final List<String> features;
    private final EntityStandard entityStandard;
    private final boolean useLemmatizer;
    private final boolean usePOSTagger;
    private final OrderType orderType;
    private TrainingMode mode;
    private final boolean singleLabelling;
    private final ConllEvaluation.TrainedPositionType positionType;

    public KnowNEREvaluation(String fullModelTitle,
                             String corpus,
                             String language,
                             List<String> features, EntityStandard entityStandard,
                             boolean useLemmatizer, boolean usePOSTagger,
                             TrainingMode mode, ConllEvaluation.TrainedPositionType positionType,
                             boolean singleLabelling) throws IOException {
        this.fullModelTitle = fullModelTitle;
        this.features = features;
        this.language = language;
        this.positionType = positionType;

        this.corpus = corpus;
        this.entityStandard = entityStandard;
        this.useLemmatizer = useLemmatizer;
        this.usePOSTagger = usePOSTagger;
        this.mode = mode;
        this.singleLabelling = singleLabelling;

        orderType = KnowNERTrainingUtils.getCorpusConfigurationForLanguage(corpus, KnowNERLanguage.valueOf(language)).getCorpusFormat();

    }

    public KnowNEREvaluation(String fullModelTitle,
                             String corpus,
                             String language,
                             List<String> features, EntityStandard entityStandard,
                             boolean useLemmatizer, boolean usePOSTagger,
                             TrainingMode mode, ConllEvaluation.TrainedPositionType positionType) throws IOException {
        this(fullModelTitle, corpus, language, features, entityStandard, useLemmatizer, usePOSTagger, mode, positionType, false);
    }

    /**
     * stores evaluation in evaluation directory
     * @param modelPath
     * @throws UIMAException
     * @throws IOException
     */
    public void evaluateModel(File modelPath) throws UIMAException, IOException {

        int[] testSetRange = KnowNERTrainingUtils.getDatasetRangeForCorpus(corpus, KnowNERLanguage.valueOf(language),
                mode == TrainingMode.TEST ? CorpusConfiguration.Range.TESTB : CorpusConfiguration.Range.TESTA);

//        contains NamedEntities
        CollectionReaderDescription reader = createReaderDescription(Conll2003AidaReader.class,
                PARAM_LANGUAGE, language,
                Conll2003AidaReader.PARAM_SINGLE_FILE, true,
                Conll2003AidaReader.PARAM_ORDER, orderType,
                PARAM_SOURCE_LOCATION, KnowNERSettings.getLangResourcesPath(corpus, language),
                PARAM_PATTERNS, CorpusConfiguration.DEFAULT_FILE_NAME,
                Conll2003AidaReader.PARAM_FIRSTDOCUMENT, testSetRange[0],
                Conll2003AidaReader.PARAM_LASTDOCUMENT, testSetRange[1],
                Conll2003AidaReader.PARAM_NAMED_ENTITY_PER_TOKEN, true);

        AnalysisEngineDescription preprocessing = KnowNERTrainingUtils.getPreprocessing(language, features, entityStandard, useLemmatizer, usePOSTagger);

//      writes TCOutcomes
        AnalysisEngineDescription modelAnnotator = createEngineDescription(LocalFeaturesTcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelPath,
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());

        AnalysisEngineDescription predictionsWriter = createEngineDescription(PredictionsWriter.class,
                PredictionsWriter.PARAM_LANGUAGE, language,
                PredictionsWriter.PARAM_MENTION_OUTPUT_FILE, KnowNERSettings.getModelsMentionEvaluationLocation(fullModelTitle, corpus, singleLabelling),
                PredictionsWriter.PARAM_TOKEN_OUTPUT_FILE, KnowNERSettings.getModelsTokenEvaluationLocation(fullModelTitle, corpus, singleLabelling),
                PredictionsWriter.PARAM_KNOW_NER, true,
                PredictionsWriter.PARAM_POSITION_TYPE,  positionType);

        logger.info("Evaluating model: " + fullModelTitle);

        List<AnalysisEngineDescription> analysisEngineDescriptionList = new ArrayList<>();

        if (preprocessing != null) {
            analysisEngineDescriptionList.add(preprocessing);
        }
        analysisEngineDescriptionList.add(modelAnnotator);
        if (singleLabelling) {
            AnalysisEngineDescription singleLabellingAnnotator = createEngineDescription(SingleLabelAnnotator.class);
            analysisEngineDescriptionList.add(singleLabellingAnnotator);
        }
        analysisEngineDescriptionList.add(predictionsWriter);
        SimplePipeline.runPipeline(reader, analysisEngineDescriptionList.stream().toArray(AnalysisEngineDescription[]::new));
    }

    /**
     * runs evaluation on the default conll yago files
     * @param args
     * @throws IOException
     * @throws UIMAException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, UIMAException, ClassNotFoundException, ParseException {
        CommandLine cmd = new KnowNEREvaluationCommandLineUtils().parseCommandLineArgs(args);
        String modelPath = cmd.getOptionValue("m");
        String[] evaluationCorpuses = cmd.getOptionValues("c");
        String[] labellings = cmd.getOptionValues("l");

        List<String> features = extractFeaturesFromModel(modelPath);
        modelPath = modelPath.endsWith("/") ? modelPath.substring(0, modelPath.length() - 1) : modelPath;
        String[] split = modelPath.split("/");
        String modelTitle = split[split.length-1];
        String lang = KnowNERSettings.parseLanguageFromModelTitle(modelTitle);

        ConllEvaluation.TrainedPositionType positionType = extractPositionTypeFromModel(modelTitle);

        TrainingMode mode = TrainingMode.TEST;

        for (String corpus : evaluationCorpuses) {
            for (String labelling : labellings) {
                OrderType orderType = KnowNERTrainingUtils.getCorpusConfigurationForLanguage(corpus, KnowNERLanguage.valueOf(lang)).getCorpusFormat();

                EntityStandard entityStandard = orderType.hasEntity()? EntityStandard.AIDA : EntityStandard.GOLD;
                boolean usePOSTagger = !orderType.hasPOS();
                boolean useLemmatizer = !orderType.hasLemma() && KnowNERLanguage.requiresLemma(lang);

                new KnowNEREvaluation(modelTitle, corpus, lang, features,
                        entityStandard, useLemmatizer, usePOSTagger, mode, positionType, labelling.equals("single")).evaluateModel(new File(modelPath));
            }
        }

        writeEvaluationSummary(modelTitle, evaluationCorpuses, labellings);
    }

    private static final Pattern evaluationFigures = Pattern.compile(".*precision: (.*)%; recall: (.*)%; FB1: (.*)");

    private static void writeEvaluationSummary(String modelTitle, String[] evaluationCorpuses, String[] labellings) throws IOException {
//        String[] evaluationCorpuses = {"default", Util.parseCorpusFromModelTitle(modelTitle)};
        String[] labellingsOrder = {"multi", "single"};
        List<String> lines = new ArrayList<>();
        lines.add("modelTitle;corpus;multi-precision;multi-recall;multi-F1;single-precision;single-recall;single-F1;");
        for (String corpus : evaluationCorpuses) {
            StringBuilder sb = new StringBuilder(modelTitle);
            sb.append(";").append(corpus).append(";");
            for (String labelling : labellingsOrder) {
                List<String> labellingsList = Arrays.asList(labellings);
                if (!labellingsList.contains(labelling)) {
                    continue;
                }
                String evalFilePath = KnowNERSettings.getModelsMentionEvaluationLocation(modelTitle, corpus, labelling.equals("single"));
                List<String> evalFile = Files.readAllLines(Paths.get(evalFilePath));
                Matcher matcher = evaluationFigures.matcher(evalFile.get(1));
                if (!matcher.matches()) {
                    throw new RuntimeException("Evaluation file " + evalFilePath + " has wrong format!");
                }
                for (int i : new Integer[]{1,2,3}) {
                    sb.append(matcher.group(i)).append(";");
                }
            }
            lines.add(sb.deleteCharAt(sb.length()-1).toString());
        }
        Files.write(Paths.get(KnowNERSettings.getModelsEvaluationLocation(), modelTitle + "_eval_summary.txt"), lines);

    }

    private static ConllEvaluation.TrainedPositionType extractPositionTypeFromModel(String fullModelTitle) {

        if (fullModelTitle.contains("BMEOW")) {
            return ConllEvaluation.TrainedPositionType.BMEOW;
        }
        return ConllEvaluation.TrainedPositionType.ORIGINAL;
    }

    private static List<String> extractFeaturesFromModel(String modelPath) throws IOException {

        List<String> featLines = Files.readAllLines(Paths.get(modelPath + "/featureExtractorConfiguration.txt"));
        return featLines.stream().map(l -> l.split("\t")[0]).collect(Collectors.toList());
    }


}
