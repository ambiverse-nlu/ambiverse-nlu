package de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConllEvaluation {

    private static final Pattern CONLL_FINAL_FIGURES_PATTERN = Pattern.compile("\\d+\\.\\d+");

    private static final Logger logger = LoggerFactory.getLogger(ConllEvaluation.class);
    private final String fullModelTitle;
    private final boolean tempFiles;
    private final File tokenOutputFile;
    private final File mentionOutputFile;
    private String startDateTime;


    public enum TrainedPositionType {BMEOW, ORIGINAL}

    private final TrainedPositionType trainedPositionType;

    public ConllEvaluation(File tokenOutputFile, File mentionOutputFile, TrainedPositionType trainedPositionType, boolean tempFiles) {

        this.tokenOutputFile = tokenOutputFile;
        this.mentionOutputFile = mentionOutputFile;
        this.trainedPositionType = trainedPositionType;
        this.tempFiles = tempFiles;
        this.fullModelTitle = null;
    }
    /**
     * Gold standard file is passed when position type is bmeow type, to have a clean gold standard
     * @param fullModelTitle
     * @param goldStandardFile
     */
    @Deprecated
    public ConllEvaluation(String fullModelTitle, Path goldStandardFile) throws IOException {
        tokenOutputFile = null;
        mentionOutputFile = null;
        this.fullModelTitle = fullModelTitle;
        tempFiles = false;
        trainedPositionType = TrainedPositionType.BMEOW;
    }

    @Deprecated
    public ConllEvaluation(String fullModelTitle, boolean tempFiles) {
        tokenOutputFile = null;
        mentionOutputFile = null;
        this.fullModelTitle = fullModelTitle;
        this.tempFiles = tempFiles;
        trainedPositionType = TrainedPositionType.ORIGINAL;
    }

    @Deprecated
    public ConllEvaluation(String fullModelTitle) {
        tokenOutputFile = null;
        mentionOutputFile = null;
        this.fullModelTitle = fullModelTitle;
        tempFiles = false;
        trainedPositionType = TrainedPositionType.ORIGINAL;
    }

    @Deprecated
    public ConllEvaluation(String fullModelTitle, TrainedPositionType trainedPositionType) {
        tokenOutputFile = null;
        mentionOutputFile = null;
        this.fullModelTitle = fullModelTitle;
        this.trainedPositionType = trainedPositionType;
        this.tempFiles = false;
    }

    @Deprecated
    public ConllEvaluation(String fullModelTitle, TrainedPositionType trainedPositionType, boolean tempFiles) {
        tokenOutputFile = null;
        mentionOutputFile = null;
        this.fullModelTitle = fullModelTitle;
        this.trainedPositionType = trainedPositionType;
        this.tempFiles = tempFiles;
    }

    public String run(List<String> predictionLines) throws IOException, InterruptedException {
        logger.info("Running conll evaluation");
        startDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSS"));

        List<String[]> splitLines = predictionLines
                .stream()
                .filter(s -> !s.startsWith("#"))
                .map(s -> s.trim().split("\t"))
                .collect(Collectors.toList());
        splitLines.add(0, new String[]{""});

        ArrayList<String> tokenPredictions = new ArrayList<>();
        ArrayList<String> mentionPredictions = new ArrayList<>();
        IntStream.range(1, splitLines.size())
                .forEachOrdered(i -> {
                            if (trainedPositionType == TrainedPositionType.ORIGINAL) {
                                transformFromResults(splitLines, tokenPredictions, mentionPredictions, i);
                            } else {
                                transformFromBMEOWResults(splitLines, tokenPredictions, mentionPredictions, i);
                            }
                        }
                );


        runConlleval(mentionPredictions, "mention", mentionOutputFile, KnowNERSettings.getConllEvalPath());
        runConlleval(tokenPredictions, "token", tokenOutputFile, KnowNERSettings.getConllEvalPath(), "-d", ",", "-r", "-o", "OTH");

//        logger.info("--mention result file---");
//        logger.info(org.apache.commons.lang.StringUtils.join(Files.readAllLines(mentionResultFile), "\n"));
//
//        logger.info("--token result file---");
//        logger.info(org.apache.commons.lang.StringUtils.join(Files.readAllLines(tokenResultFile), "\n"));
//        return extractCsvLine(fullModelTitle, tokenOutputFile.toPath(), mentionOutputFile.toPath());
        return null;
    }

    public String run(Path predictionsFile) throws IOException, InterruptedException {
        return run(Files.readAllLines(predictionsFile));
    }

    private void transformFromBMEOWResults(List<String[]> splitLines, ArrayList<String> tokenPredictions, ArrayList<String> mentionPredictions, int i) {
        String[] split = splitLines.get(i);
        if (split.length == 1 && "".equals(split[0])) {
            tokenPredictions.add("");
            mentionPredictions.add("");
            return;
        }

        StringBuilder sb = new StringBuilder("dummy dummy ");
        String gold = split[0];
        String pred = split[1];

        String goldClass = gold.replaceFirst(".-", "");
        String predClass = pred.replaceFirst(".-", "");

        String tokenPred = ",," + goldClass + "," + predClass;
        tokenPredictions.add(tokenPred);

        if (gold.contains("OTH")) {
            sb.append("O ");

        } else {
            sb.append(gold).append(" ");
        }

        if (pred.contains("OTH")) {
            sb.append("O");

        } else {
//					if beginning of a sentence
            if (splitLines.get(i - 1).length == 1 && "".equals(splitLines.get(i - 1)[0])) {
                sb.append("I-").append(predClass);

            } else {
                String prevPredClass = splitLines.get(i - 1)[1].replaceFirst(".-", "");

                if (predClass.equals(prevPredClass) && (pred.startsWith("W") || pred.startsWith("B"))) {
                    sb.append("B-");

                } else {
                    sb.append("I-");
                }
                sb.append(predClass);
            }
        }

        String mentionPred = sb.toString();
        mentionPredictions.add(mentionPred);

        logger.trace(Arrays.toString(split) + " | " + tokenPred + " | " + mentionPred);
    }

    private void transformFromResults(List<String[]> splitLines, ArrayList<String> tokenPredictions, ArrayList<String> mentionPredictions, int i) {
        String[] split = splitLines.get(i);
        if (split.length == 1 && "".equals(split[0])) {
            tokenPredictions.add("");
            mentionPredictions.add("");
            return;
        }

        StringBuilder sb = new StringBuilder("dummy dummy ");
        String gold = split[0];
        String pred = split[1];

        String goldClass = gold.replaceFirst(".-", "");
        String predClass = pred.replaceFirst(".-", "");

        String tokenPred = ",," + goldClass + "," + predClass;
        tokenPredictions.add(tokenPred);
        if (gold.contains("OTH")) {
            sb.append("O ");

        } else {
            sb.append(gold).append(" ");
        }

        if (pred.contains("OTH")) {
            sb.append("O");

        } else {
            sb.append(pred);
        }

        String mentionPred = sb.toString();
        mentionPredictions.add(mentionPred);

        logger.trace(Arrays.toString(split) + " | " + tokenPred + " | " + mentionPred);
    }

    private void runConlleval(ArrayList<String> predictions, String suffix, File resultFile, String... cmd) throws IOException, InterruptedException {
        Path predFile = generateFilePath(suffix, "predictions");
        Files.write(predFile, predictions);

//        Path resultFile = generateFilePath(suffix, "conlleval_result");

        ProcessBuilder pb = new ProcessBuilder(cmd)
                .redirectInput(predFile.toFile())
                .redirectOutput(resultFile);

        Process process = pb.start();
        try {
            process.waitFor();
            int exitVal = process.exitValue();
            logger.info("Conlleval terminated, exit value={}", exitVal);

        } catch (InterruptedException e) {
            logger.info("The conlleval process was interrupted");
            throw e;
        }
    }

    private Path generateFilePath(String suffix, String name) throws IOException {
        return tempFiles? Files.createTempFile(fullModelTitle, suffix + name + startDateTime + ".txt") :
                Paths.get(fullModelTitle + "_" + suffix + name + "_" + startDateTime + ".txt");
    }

    private static String extractCsvLine(String fullModelTitle, Path... resultFiles) throws IOException {
        if (resultFiles == null) {
            throw new IllegalArgumentException("No conlleval result files provided");
        }

        StringBuilder sb = new StringBuilder(fullModelTitle).append("\t");
//        StringBuilder headerSb = new StringBuilder("Model title\tTotal token precision\tTotal token recall\tTotal token F1\t");
//        headerSb.append("LOCATION token precision\tLOCATION token recall\tLOCATION token F1\t");
//        headerSb.append("MISC token recision\tMISC token recall\tMISC token F1\t");
//        headerSb.append("ORGANIZATION token precision\tORGANIZATION token recall\tORGANIZATION token F1\t");
//        headerSb.append("PERSON token precision\tPERSON token recall\tPERSON token F1\t");
//        headerSb.append("Total mention precision\tTotal mention recall\tTotal mention F1\t");
//        headerSb.append("LOCATION mention precision\tLOCATION mention recall\tLOCATION mention F1\t");
//        headerSb.append("MISC mention recision\tMISC mention recall\tMISC mention F1\t");
//        headerSb.append("ORGANIZATION mention precision\tORGANIZATION mention recall\tORGANIZATION mention F1\t");
//        headerSb.append("PERSON mention precision\tPERSON mention recall\tPERSON mention F1\t");
//        StringBuilder sb = new StringBuilder("de_AgnosticModel_DEV").append("\t");

        for (Path resultFile : resultFiles) {

            List<String> resultLines = Files.readAllLines(resultFile);
            String total = Arrays.stream(resultLines.get(1).split(";"))
                    .skip(1)
                    .map(s -> {
                        Matcher matcher = CONLL_FINAL_FIGURES_PATTERN.matcher(s.trim());
                        if (matcher.find()) {
                            return matcher.group();
                        }

                        throw new RuntimeException();
                    })
                    .collect(Collectors.joining("\t"));
            sb.append(total).append("\t");

            sb.append(resultLines.subList(2,resultLines.size())
                    .stream()
                    .map(l -> {

                        return Arrays.stream(l.split(";"))
                                .map(s -> {
                                    Matcher matcher = CONLL_FINAL_FIGURES_PATTERN.matcher(s.trim());
                                    if (matcher.find()) {
                                        return matcher.group();
                                    }

                                    throw new RuntimeException();
                                })
                                .collect(Collectors.joining("\t"));
                    })
                    .collect(Collectors.joining("\t")));
            sb.append("\t");

        }

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
//        if (args.length != 3) {
//            System.out.println("Usage: ConllEvaluation <modelTitle> <ORIGINAL|BMEOW> <path to predictions.txt>");
//            return;
//        }
//        System.out.println(new ConllEvaluation(args[0], TrainedPositionType.getLanguageForString(args[1])).run(Paths.get(args[2])));
    }

}
