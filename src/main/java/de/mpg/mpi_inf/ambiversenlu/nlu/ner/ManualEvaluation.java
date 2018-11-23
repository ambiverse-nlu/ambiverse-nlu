package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.Conll2003AidaReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.ConllEvaluation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.ManualAnnotationsAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.NerMentionAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers.MentionSpansEvaluationWriter;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers.PredictionsWriter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.OrderType.WORD_POSITION_TYPE;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_LANGUAGE;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class ManualEvaluation {


    private static boolean singleLabelling;
    private static String language;
    private static AnalysisEngineDescription nerMentionAnnotatorDescription;
    private static String directory;
    private static AnalysisEngineDescription manualAnnotatorPerMentionDescription;
    private static AnalysisEngineDescription manualAnnotatorPerTokenDescription;

    public static void main(String[] args) throws IOException, UIMAException {
        String manualGoldFile = args[0];
        String trainedPrefix = args[1];
        language = args[2];
        singleLabelling = args.length > 3 && args[3].equals("singleLabelling");
        if (singleLabelling) {
            Path manualPath = generateSingleLabeledFile(manualGoldFile);
            manualGoldFile = manualPath.toString();
        }

//        annotate with manual annotations from manualGoldFile
        manualAnnotatorPerMentionDescription = createEngineDescription(ManualAnnotationsAnnotator.class,
		ManualAnnotationsAnnotator.MANUAL_ANNOTATIONS_LOCATION, manualGoldFile,
		ManualAnnotationsAnnotator.PARAM_NAMED_ENTITY_PER_TOKEN, false);

//        annotate with manual annotations from manualGoldFile
        manualAnnotatorPerTokenDescription = createEngineDescription(ManualAnnotationsAnnotator.class,
		ManualAnnotationsAnnotator.MANUAL_ANNOTATIONS_LOCATION, manualGoldFile,
		ManualAnnotationsAnnotator.PARAM_NAMED_ENTITY_PER_TOKEN, true);
        System.setProperty("aida.conf", "default");

        nerMentionAnnotatorDescription = createEngineDescription(NerMentionAnnotator.class);

        Matcher matcher = Pattern.compile("(.*/)*(.*)").matcher(trainedPrefix);
        if (!matcher.matches()) {
            throw new RuntimeException("Bad trained file prefix: " + trainedPrefix);
        }

        directory = matcher.group(1) == null? "./" : matcher.group(1);
        String prefix = matcher.group(2);
        Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.startsWith(prefix) && fileName.endsWith(".tsv")) {
                    try {
                        evaluateTrainedFile(fileName);
                    } catch (UIMAException e) {
                        e.printStackTrace();
                    }
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private static void evaluateTrainedFile(String fileName) throws IOException, UIMAException {
        Path directoryPath;
        String trainedFile = directory + fileName;
        if (singleLabelling) {
            directoryPath = Paths.get(directory, fileName + "-singleLabel-evaluation");

            Path trainedPath = generateSingleLabeledFile(trainedFile);
            trainedFile = trainedPath.toString();
        } else {
            directoryPath = Paths.get(trainedFile + "-evaluation");
        }

        Files.createDirectory(directoryPath);
//         produce jcas with trained file
        CollectionReaderDescription reader = createReaderDescription(Conll2003AidaReader.class,
                PARAM_LANGUAGE, language,
                Conll2003AidaReader.PARAM_SINGLE_FILE, true,
                Conll2003AidaReader.PARAM_ORDER, WORD_POSITION_TYPE,
                PARAM_SOURCE_LOCATION, trainedFile,
                Conll2003AidaReader.PARAM_MANUAL_TOKENS_NER, false,
                Conll2003AidaReader.PARAM_NAMED_ENTITY_PER_TOKEN, true);

        AnalysisEngineDescription mentionSpansWriter = createEngineDescription(MentionSpansEvaluationWriter.class,
                MentionSpansEvaluationWriter.PARAM_OUTPUT_FILE, directoryPath.toString() + "/ManualSpanEvaluation.txt");

        System.out.println("Running mention spans evaluation");
        SimplePipeline.runPipeline(reader, manualAnnotatorPerMentionDescription, nerMentionAnnotatorDescription, mentionSpansWriter);

        AnalysisEngineDescription predictionsWriter = createEngineDescription(PredictionsWriter.class,
                PredictionsWriter.PARAM_LANGUAGE, language,
                PredictionsWriter.PARAM_MENTION_OUTPUT_FILE, directoryPath.toString() + "/ConllMentionEvaluation.txt",
                PredictionsWriter.PARAM_TOKEN_OUTPUT_FILE, directoryPath.toString() + "/ConllTokenEvaluation.txt",
                PredictionsWriter.PARAM_KNOW_NER, true,
                PredictionsWriter.PARAM_POSITION_TYPE,  ConllEvaluation.TrainedPositionType.ORIGINAL);

        System.out.println("Running tokens and mentions evaluation");
        SimplePipeline.runPipeline(reader, manualAnnotatorPerTokenDescription, predictionsWriter);
    }

    private static Path generateSingleLabeledFile(String file) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(file));

        String previousType = null;
        List<String> singleLabeledFile = new ArrayList<>();
        for (String line : strings) {
            String[] split = line.split("\t");
            if (split.length > 1) {
                if (previousType != null && !previousType.equals(split[2])) {
                    split[1] = "B";
                }

                previousType = split[2];
                split[2] = "ENTITY";
            } else {
                previousType = null;
            }
            singleLabeledFile.add(String.join("\t", Arrays.asList(split)));
        }
        Path singleLabeledFilePath = Files.createTempFile("singleLabeledFile", ".txt");
        Files.write(singleLabeledFilePath, singleLabeledFile);
        return singleLabeledFilePath;
    }
}
