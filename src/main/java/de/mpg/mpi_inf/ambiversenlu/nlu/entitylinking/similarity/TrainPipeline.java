package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.CollectionProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.CollectionSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Collection;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A pipeline to get settings with trained weights.
 */
public class TrainPipeline {

  private static final Logger logger = LoggerFactory.getLogger(TrainPipeline.class);

  private Options commandLineOptions;

  public static void main(String[] args) throws Throwable {
    new TrainPipeline().run(args);
  }

  private TrainPipeline() {
    commandLineOptions = buildCommandLineOptions();
  }

  private void run(String[] args) throws Throwable {
    CommandLineParser parser = new PosixParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(commandLineOptions, args);
    } catch (MissingOptionException e) {
      System.out.println("\n\n" + e.getMessage() + "\n\n");
      printHelp(commandLineOptions);
      return;
    }
    if (cmd.hasOption("h")) {
      printHelp(commandLineOptions);
    }
    Set<File> settingsFilesAndDirectories = Arrays.stream(cmd.getOptionValues("s")).map(File::new).collect(Collectors.toSet());

    Collection.Builder builder = new Collection.Builder();
    String inputDirectory = cmd.getOptionValue("d");
    if (!inputDirectory.endsWith("*")) {
      inputDirectory = inputDirectory + "*";
    }
    builder.withPath(inputDirectory);
    if (!cmd.hasOption("i")) {
      throw new IllegalArgumentException("A reader should be specified for the specific collection");
    }
    String reader = cmd.getOptionValue("i");
    builder.withCollectionType(Reader.COLLECTION_FORMAT.valueOf(reader));

    String encoding = "UTF-8";
    if (cmd.hasOption("n")) {
      encoding = cmd.getOptionValue("n");
      builder.withDocumentEncoding(encoding);
    }

    Integer threadCount = 1;
    if (cmd.hasOption("c")) {
      threadCount = Integer.parseInt(cmd.getOptionValue("c"));
    }

    if (cmd.hasOption("b")) {
      builder.withDocStart(Integer.parseInt(cmd.getOptionValue("b")));
    }

    if (cmd.hasOption("f")) {
      builder.withDocEnd(Integer.parseInt(cmd.getOptionValue("f")));
    }

    CollectionSettings collectionSetting = null;
    if (cmd.hasOption("z")) {
      File file = new File(cmd.getOptionValue("z"));
      if (!file.exists()) {
        throw new IllegalArgumentException("The collection settings file does not exist");
      }
      collectionSetting = CollectionSettings.readFromFile(cmd.getOptionValue("z"));
    }

    Language language = null;
    if (collectionSetting != null) {
      language = collectionSetting.getLanguage();
      builder.withLanguage(language);
    } else if (cmd.hasOption("l")) {
      language = Language.getLanguageForString(cmd.getOptionValue("l"));
      builder.withLanguage(language);
    }

    if (language == null) {
      throw new IllegalArgumentException("A language must be specified");
    }

    EvaluationSettings evsettings = null;
    evsettings = EvaluationSettings.readFromFile("src/test/resources/evaluation/similarity_training.properties");
    if (evsettings.isEvaluateConcepts() && evsettings.isEvaluateNE()) {
      System.err.println("Only train NE or C at a time. Set one to true and one to false in similarity_training.properties");
      System.exit(1);
    }
    if (!evsettings.isEvaluateConcepts() && !evsettings.isEvaluateNE()) {
      System.err.println("Specify what is being trained by setting evaluateConcepts or evaluateNE to true in similarity_training.properties");
      System.exit(1);
    }
    boolean isNamedEntity = true;
    if(evsettings.isEvaluateConcepts()) {
      isNamedEntity = false;
    }
    logger.info("isNamedEntity: " + isNamedEntity);

    Collection collection = builder.build();
    CollectionProcessor cp = new CollectionProcessor(threadCount, PipelineType.READ_COLLECTION, evsettings, collectionSetting);
    List<ProcessedDocument> documents = cp.process(collection, ProcessedDocument.class);

    File destDir = new File(cmd.getOptionValue("o"));
    File tempDir = new File(cmd.getOptionValue("tmp"));

    // checking files and directories
    Set<File> settingsFiles = new HashSet<>();
    for (File settingsFileOrDirectory : settingsFilesAndDirectories) {
      if (!settingsFileOrDirectory.exists() || !settingsFileOrDirectory.canRead()) {
        logger.warn(settingsFileOrDirectory + " does not exist or is not readable.");
        continue;
      }
      if (settingsFileOrDirectory.isDirectory()) {
        Arrays.stream(settingsFileOrDirectory.listFiles()).filter(File::isFile).forEach(settingsFiles::add);
      } else if (settingsFileOrDirectory.isFile()) {
        settingsFiles.add(settingsFileOrDirectory);
      }
    }

    if (settingsFiles.isEmpty()) {
      System.err.print("No existing and readable Settings files!");
      System.exit(1);
    }

    if (!(destDir.exists() || destDir.mkdirs()) || !destDir.canWrite() || !destDir.isDirectory()) {
      System.err.println("Can't create or access output directory '" + destDir + "'.");
      System.exit(1);
    }
    if (!(tempDir.exists() || tempDir.mkdirs()) || !tempDir.canWrite() || !tempDir.isDirectory()) {
      System.err.println("Can't create or access temporary directory '" + tempDir + "'.");
      System.exit(1);
    }

    // creating settings from files
    List<SimilaritySettings> settings = settingsFiles.stream().map(SimilaritySettings::new).collect(Collectors.toList());

    boolean includeIncorrectEntities = !cmd.hasOption("t");
    boolean doDocumentNormalization = !cmd.hasOption("n");
    boolean useDistanceDiscount = cmd.hasOption("dd");
    String method = cmd.getOptionValue("m", "SMO");

    File scoresDir = new File(tempDir, "scores_train");

    settings.forEach(setting -> {
      if (!EstimateParameterWeightsFromScores.settingNeedsPrior(setting)) {
        logger.warn("Prior is ignored for '" + setting.getIdentifier() + ".properties' because priorWeight is set to 0.0. "
            + "To include the prior, set it to 1.0");
      }
    });

    logger.info("Generate scores");
    new GenerateScores(settings).generate(LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(language), documents, scoresDir, includeIncorrectEntities,
      doDocumentNormalization, useDistanceDiscount, new NullTracer(), isNamedEntity);

    settings.parallelStream().forEach(setting -> {
      try {
        logger.info("Estimate parameter weights from scores for: " + setting.getIdentifier());
        EstimateParameterWeightsFromScores epwfs = new EstimateParameterWeightsFromScores(setting, scoresDir).loadData();
        if (cmd.hasOption("arff")) {
          FileWriter fileWriter = new FileWriter(new File(tempDir, setting.getIdentifier() + ".arff"));
          fileWriter.write(epwfs.getDataInArffFormat());
          fileWriter.flush();
          fileWriter.close();
        }
        epwfs.estimateWeight(new File(destDir, setting.getIdentifier() + ".properties"), method);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    EntityLinkingManager.shutDown();
  }

  @SuppressWarnings("AccessStaticViaInstance") private static Options buildCommandLineOptions() {
    Options options = new Options();

    options.addOption(OptionBuilder.withLongOpt("settings").withDescription(
        "A comma separated list of SimilaritySettings files which should be used. "
            + "If a directory is given, all files in this directory will be used (without subdirectories).").hasArgs(Integer.MAX_VALUE)
        .withValueSeparator(',').withArgName("SETTINGS_FILES").isRequired().create("s"));

    options.addOption(OptionBuilder.withLongOpt("temporary-directory").withDescription("The directory where the temporary data should go").hasArg()
        .withArgName("TEMPORARY_DIRECTORY").isRequired().create("tmp"));


    options.addOption(OptionBuilder.withLongOpt("output-directory").withDescription("The directory where the output data should go").hasArg()
        .withArgName("OUTPUT_DIRECTORY").isRequired().create("o"));

    options.addOption(
        OptionBuilder.withLongOpt("collection-path").withDescription("Path to collection data (reader needs to be aware of structure).").hasArg()
            .withArgName("COLLECTION_PATH").isRequired().create("d"));

    options.addOption(
        OptionBuilder.withLongOpt("reader").withDescription("AIDA or NONE").hasArg().withArgName("Collection reader type").isRequired().create("i"));

    options.addOption(
        OptionBuilder.withLongOpt("from").withDescription("Only read collection starting from 'f'").hasArg().withArgName("BEGIN").create("b"));

    options.addOption(OptionBuilder.withLongOpt("to").withDescription("Only read collection up to 't'").hasArg().withArgName("FINISH").create("f"));

    options.addOption(OptionBuilder.withLongOpt("method").withDescription("Method to estimate the parameter weights (SMO or Logistic)").hasArg()
        .withArgName("METHOD").create("m"));


    options.addOption(
        OptionBuilder.withLongOpt("dont-include-incorrect-entities").withDescription("If this flag is set incorrect entities are excluded.")
            .create("e"));

    options.addOption(
        OptionBuilder.withLongOpt("dont-do-document-normalization").withDescription("If this flag is set document normalisation is disabled.")
            .create("n"));

    options.addOption(
        OptionBuilder.withLongOpt("language").withDescription("Collection language.").hasArg().isRequired().withArgName("LANGUAGE").create("l"));

    options.
        addOption(OptionBuilder.withLongOpt("cSettings").withDescription("Settings for the collection to be loaded").hasArg()
            .withArgName("collection settings").create("z"));
    options.
        addOption(OptionBuilder.withLongOpt("trueEvidence").withDescription("Include only True Evidence").withArgName("true evidence").create("t"));

    options.addOption(OptionBuilder.withLongOpt("use-distance-discount").create("dd"));

    options.addOption(OptionBuilder.withDescription("Prints out a arff file into the temp directory.").create("arff"));

    options.addOption(OptionBuilder.withLongOpt("help").create('h'));

    return options;
  }

  private void printHelp(Options commandLineOptions) {
    String header = "\n\nRun Training:\n\n";
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("TrainPipeline", header, commandLineOptions, "", true);
    System.exit(0);
  }
}
