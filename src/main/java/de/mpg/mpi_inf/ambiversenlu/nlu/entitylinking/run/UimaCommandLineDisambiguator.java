package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.run;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationCounts;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarityCombinationsIds;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.CollectionProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.CollectionSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.EvaluationStats;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.Stats;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CommandLineUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Collection;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.NERManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Disambiguates a document from the command line.
 *
 */
public class UimaCommandLineDisambiguator {

  //-s "Einstein was born in Ulm." -l en -pip ENTITY_SALIENCE

  private Logger logger = LoggerFactory.getLogger(UimaCommandLineDisambiguator.class);

  public static void main(String[] args) throws Throwable {
    try {
      new UimaCommandLineDisambiguator().run(args);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public void run(String args[]) throws Throwable {
    Document.Builder builder;

    CommandLine cmd = new CommandLineUtils().parseCommandLineArgs(args);

    if (cmd.hasOption("d") && cmd.hasOption("s")) {
      throw new IllegalArgumentException("Either a directory (-d) or a sentence (-s) can be specified but not both");
    }

    if (cmd.hasOption("z") && !cmd.hasOption("d")) {
      throw new IllegalArgumentException("A collection settings should be accompanied by a collection path");
    }

    PipelineType defaultPipeline = null;
    if(!cmd.hasOption("e")) {
      if (!cmd.hasOption("pip")) {
        throw new IllegalArgumentException("A pipeline must be specified in the command line (e.g., -pip ENTITY_SALIENCE) when not running in evaluation mode (e.g., -e)");
      }
      try {
        defaultPipeline = PipelineType.valueOf(cmd.getOptionValue("pip"));
      } catch (Exception e) {
        throw new IllegalArgumentException("The pipeline must exist in PipelineType.java");
      }
    }

    
    String inputDirectory = null;
    String reader;
    if (cmd.hasOption("d")) {
      builder = new Collection.Builder();
      inputDirectory = cmd.getOptionValue("d");
      File file = new File(inputDirectory);
      if (!file.exists()) {
        file = ClassPathUtils.getFileFromClassPath(inputDirectory);
        if (!file.exists()) {
          throw new FileNotFoundException("Input directory " + inputDirectory + " does not exist");
        }
      }
      ((Collection.Builder) builder).withPath(file.getPath() + "/*");
      if (!cmd.hasOption("i")) {
        throw new FileNotFoundException("A reader should be specified for the specific collection");
      }
      reader = cmd.getOptionValue("i");
      ((Collection.Builder) builder).withCollectionType(Reader.COLLECTION_FORMAT.valueOf(reader));
    } else if (cmd.hasOption("s")) {
      builder = new Document.Builder();
      String text = cmd.getOptionValue("s");
      builder.withText(text);
    } else {
      throw new IllegalArgumentException("An input must be provided, either a sentence (-s) or a directory (-d)");
    }

    CollectionSettings collectionSetting = null;
    if (cmd.hasOption("z")) {
      String collectionSettingsPath = cmd.getOptionValue("z");
      File file = new File(collectionSettingsPath);
      if (!file.exists()) {
        file = ClassPathUtils.getFileFromClassPath(collectionSettingsPath);
        if (!file.exists()) {
          throw new FileNotFoundException("The collection settings file does not exist");
        }
      }
      collectionSetting = CollectionSettings.readFromFile(file.getPath());
    }

    Language language;
    if (collectionSetting != null) {
      language = collectionSetting.getLanguage();
      builder.withLanguage(language);
    } else if (cmd.hasOption("l")) {
      language = Language.getLanguageForString(cmd.getOptionValue("l"));
      builder.withLanguage(language);
    }

    String encoding = "UTF-8";
    if (cmd.hasOption("n")) {
      encoding = cmd.getOptionValue("n");
      builder.withDocumentEncoding(encoding);
    }

    String documentId;
    if (cmd.hasOption("did")) {
      documentId = cmd.getOptionValue("did");
      builder.withId(documentId);
    }

    DisambiguationSettings.Builder dsbuilder = new DisambiguationSettings.Builder();

    DisambiguationSettings.DISAMBIGUATION_METHOD dm = null;
    if (cmd.hasOption("t")) {
      dm = DisambiguationSettings.DISAMBIGUATION_METHOD.valueOf(cmd.getOptionValue("t"));
      dsbuilder.withDisambiguationMethod(dm);
    }

    if (cmd.hasOption("x")) {
      int chunkThreadCount = Integer.parseInt(cmd.getOptionValue("x"));
      dsbuilder.withNumChunkThreads(chunkThreadCount);
    }

    if (cmd.hasOption("r")) {
      double threshold = Double.parseDouble(cmd.getOptionValue("r"));
      dsbuilder.withNullMappingThreshold(threshold);
    }

    if (cmd.hasOption("m")) {
      int minCount = Integer.parseInt(cmd.getOptionValue("m"));
      dsbuilder.withMinMentionOcurrenceCount(minCount);
    }

    if (cmd.hasOption("u")) {
      dsbuilder.withExhaustiveSearch(true);
    }

    if (cmd.hasOption("w")) {
      dsbuilder.withUseNormalizedObjective(true);
    }

    if (cmd.hasOption("v")) {
      dsbuilder.withComputeConfidence(true);
    }

    if (cmd.hasOption("y")) {
      int prune = Integer.parseInt(cmd.getOptionValue("y"));
      dsbuilder.withPruneCandidateEntities(prune);
    }

    if (cmd.hasOption("a")) {
      double alpha = Double.parseDouble(cmd.getOptionValue("a"));

      if (alpha < 0.0 || alpha > 1.0) {
        throw new IllegalArgumentException("parameter a should be in the range [0.0, 1.0]");
      }
      dsbuilder.withGraphAlpha(alpha);
    }

    if (cmd.hasOption("hNE")) {
      double thresh = Double.parseDouble(cmd.getOptionValue("hNE"));
      dsbuilder.withCohRobustnessThresholdNE(thresh);
    }

    if (cmd.hasOption("hC")) {
      double thresh = Double.parseDouble(cmd.getOptionValue("hC"));
      dsbuilder.withCohRobustnessThresholdC(thresh);
    }

    if (cmd.hasOption("q")) {
      double confCoherenceThresh = Double.parseDouble(cmd.getOptionValue("q"));
      dsbuilder.withCoherenceConfidenceThreshold(confCoherenceThresh);
    }

    if (cmd.hasOption("tc")) {
      dsbuilder.withTrainingCorpus(cmd.getOptionValue("tc"));
    }

    if (cmd.hasOption("ssNE")) {
      dsbuilder.withSimilaritySettingNameNE(cmd.getOptionValue("ssNE"));
    }

    if (cmd.hasOption("ssC")) {
      dsbuilder.withSimilaritySettingNameC(cmd.getOptionValue("ssC"));
    }

    if (cmd.hasOption("ccNE")) {
      EntityEntitySimilarityCombinationsIds cohConfId = EntityEntitySimilarityCombinationsIds.valueOf(cmd.getOptionValue("ccNE"));
      if (cohConfId == null) {
        throw new IllegalArgumentException("the cohrence combination id is not available");
      }
      dsbuilder.withCohConfigsNE(cohConfId);
    }

    if (cmd.hasOption("ccC")) {
      EntityEntitySimilarityCombinationsIds cohConfId = EntityEntitySimilarityCombinationsIds.valueOf(cmd.getOptionValue("ccC"));
      if (cohConfId == null) {
        throw new IllegalArgumentException("the cohrence combination id is not available");
      }
      dsbuilder.withCohConfigsC(cohConfId);
    }

    if (cmd.hasOption("u")) {
      dsbuilder.withExhaustiveSearch(true);
    }

    if (cmd.hasOption("w")) {
      dsbuilder.withUseNormalizedObjective(true);
    }

    if (cmd.hasOption("v")) {
      dsbuilder.withComputeConfidence(true);
    }

    if (cmd.hasOption("y")) {
      int prune = Integer.parseInt(cmd.getOptionValue("y"));
      dsbuilder.withPruneCandidateEntities(prune);
    }

    String outputDirectory = "./disambiguationOutput/";
    if (inputDirectory != null) {
      File file = new File(inputDirectory);
      if (file.isDirectory()) {
        if (inputDirectory.endsWith("*")) {
          inputDirectory = inputDirectory.substring(0, inputDirectory.length() - 1);
        }
        outputDirectory = inputDirectory + "/disambiguationOutput/";
      } else {
        outputDirectory = file.getParent() + "/disambiguationOutput/";
      }
    }

    DisambiguationSettings ds = dsbuilder.build();
    builder.withDisambiguationSettings(ds);

    Integer threadCount = 1;
    if (cmd.hasOption("c")) {
      threadCount = Integer.parseInt(cmd.getOptionValue("c"));
    }

    if(cmd.hasOption("tr")) {
      ds.setTracingTarget(GraphTracer.TracingTarget.STATIC);
      ds.setTracingPath(outputDirectory + "/tracing/");
      if (dm == null || dm.equals(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE)) {
        GraphTracer.gTracer = new GraphTracer();
      }
    }

    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    UUID uuid = UUID.randomUUID();
    String confName = df.format(new Date())+"-"+uuid.toString().substring(0, uuid.toString().indexOf("-"));
    String configFolder = "runs/"+confName+"/";


    EvaluationSettings evsettings = null;
    if (cmd.hasOption("e")) {
      String input = cmd.getOptionValue("e");
      if (input == null) {
        input = ConfigUtils.DEFAULT_EVALUATION + "/" + "evaluation.properties";
      }
      evsettings = EvaluationSettings.readFromFile(input);
      if (evsettings.isTrace()) {
        ds.setTracingTarget(GraphTracer.TracingTarget.STATIC);
        ds.setTracingPath(outputDirectory + "/tracing/"+confName+"/");
        if (dm == null || dm.equals(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE)) {
          GraphTracer.gTracer = new GraphTracer();
        }
      }
    }

    List<ProcessedDocument> result;
    if (cmd.hasOption("d")) {
      if (cmd.hasOption("b")) {
        ((Collection.Builder) builder).withDocStart(Integer.parseInt(cmd.getOptionValue("b")));
      }

      if (cmd.hasOption("f")) {
        ((Collection.Builder) builder).withDocEnd(Integer.parseInt(cmd.getOptionValue("f")));
      }

      Collection collection = ((Collection.Builder) builder).build();
      CollectionProcessor cp;
      if (cmd.hasOption("e")) {
        logger.info("Running evaluation");
        defaultPipeline = evsettings.getPipelineType();
        logger.info("PIPELINE TYPE: " + defaultPipeline.name());
        cp = new CollectionProcessor(threadCount, defaultPipeline, evsettings, collectionSetting);
      } else {
        logger.info("Running disambiguation");
        cp = new CollectionProcessor(threadCount, defaultPipeline, evsettings, collectionSetting);
      }
      result = cp.process(collection, ProcessedDocument.class);
    } else {
      logger.info("Running disambiguation");
      Document.Builder nbuilder = builder;
      Document doc = nbuilder.build();
      DocumentProcessor dp = DocumentProcessor.getInstance(defaultPipeline);
      result = new ArrayList<>();
      result.add(dp.process(doc));
    }

    List<EvaluationCounts> ecsd = new ArrayList<>();
    List<EvaluationCounts> ecsdNE = new ArrayList<>();
    List<EvaluationCounts> ecsdC = new ArrayList<>();
    List<EvaluationCounts> ecsdUN = new ArrayList<>();
    List<EvaluationCounts> ecsd_FP = new ArrayList<>();
    List<EvaluationCounts> ecsdNE_FP = new ArrayList<>();
    List<EvaluationCounts> ecsdC_FP = new ArrayList<>();
    List<EvaluationCounts> ecsner = new ArrayList<>();
    List<EvaluationCounts> ecsnerNE = new ArrayList<>();
    List<EvaluationCounts> ecsnerC = new ArrayList<>();
    List<EvaluationCounts> ecsnerUN = new ArrayList<>();
    List<EvaluationCounts> ecsner_FP = new ArrayList<>();
    List<EvaluationCounts> ecsnerNE_FP = new ArrayList<>();
    List<EvaluationCounts> ecsnerC_FP = new ArrayList<>();


    //TODO This needs to be redone, quite messy at the moment
    logger.info("Writing output to '" + outputDirectory + configFolder + "'.");
    for (ProcessedDocument pd : result) {
      AnalyzeOutput ao = OutputUtils.generateAnalyzeOutputfromProcessedDocument(pd);
      String resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(ao);
      EvaluationStats documentStats = new EvaluationStats();
      FileUtils.writeStringToFile(new File(outputDirectory + configFolder+ pd.getDocId()+".json"), resultString, encoding);
      if (cmd.hasOption("e")) {
        ecsd.add(pd.getEvaluationCountsDisambiguation());
        documentStats.setNedStats(new Stats(pd.getEvaluationCountsDisambiguation().getPrecision(), pd.getEvaluationCountsDisambiguation().getRecall(), pd.getEvaluationCountsDisambiguation().getFScore(1)));
        if(evsettings.isEvaluateConcepts()) {
          ecsdC.add(pd.getEvaluationCountsConceptsDisambiguation());
          ecsdUN.add(pd.getEvaluationCountsUnknownsDisambiaguation());
          ecsdC_FP.add(pd.getEvaluationCountsConceptsDisambiguationFP());
          documentStats.setConceptNEDStats(new Stats(pd.getEvaluationCountsConceptsDisambiguation().getPrecision(), pd.getEvaluationCountsConceptsDisambiguation().getRecall(), pd.getEvaluationCountsConceptsDisambiguation().getFScore(1)));
          if(evsettings.isEvaluateRecognition()) {
            ecsnerC.add(pd.getEvaluationCountsNERConcepts());
            ecsnerUN.add(pd.getEvaluationCountsNERUnknowns());
            documentStats.setConceptNERStats(new Stats(pd.getEvaluationCountsNERConcepts().getPrecision(), pd.getEvaluationCountsNERConcepts().getRecall(), pd.getEvaluationCountsNERConcepts().getFScore(1)));
          }
        }
        if(evsettings.isEvaluateRecognition()) {
          ecsner.add(pd.getEvaluationCountsNER());
          documentStats.setNerStats(new Stats(pd.getEvaluationCountsNER().getPrecision(), pd.getEvaluationCountsNER().getRecall(), pd.getEvaluationCountsNER().getFScore(1)));
        }
        if(evsettings.isEvaluateNE()) {
          ecsdNE.add(pd.getEvaluationCountsNamedEntitiesDisambiguation());
          documentStats.setEntityNEDStats(new Stats(pd.getEvaluationCountsNamedEntitiesDisambiguation().getPrecision(), pd.getEvaluationCountsNamedEntitiesDisambiguation().getRecall(), pd.getEvaluationCountsNamedEntitiesDisambiguation().getFScore(1)));
          if(evsettings.isEvaluateRecognition()) {
            ecsnerNE.add(pd.getEvaluationCountsNERNamedEntities());
            documentStats.setEntityNERStats(new Stats(pd.getEvaluationCountsNERNamedEntities().getPrecision(), pd.getEvaluationCountsNERNamedEntities().getRecall(), pd.getEvaluationCountsNERNamedEntities().getFScore(1)));
          }
        }
        //Write the eval json for each document
        FileUtils.writeStringToFile(new File(outputDirectory + configFolder+ pd.getDocId()+".json.eval"), documentStats.toJSONString(), encoding);
      }
    }

    if (cmd.hasOption("e")) {
      logger.info("Computing evaluation");
      EvaluationCounts ecd = EvaluationCounts.summarize("Evaluation", ecsd);
      EvaluationCounts ecdC = EvaluationCounts.summarize("EvaluationC", ecsdC);
      EvaluationCounts ecdNE = EvaluationCounts.summarize("EvaluationNE", ecsdNE);
      EvaluationCounts ecsUN = EvaluationCounts.summarize("EvaluationUN", ecsdUN);
      EvaluationCounts ecd_FP = EvaluationCounts.summarize("Evaluation_FP", ecsd_FP);
      EvaluationCounts ecdC_FP = EvaluationCounts.summarize("EvaluationC_FP", ecsdC_FP);
      EvaluationCounts ecdNE_FP = EvaluationCounts.summarize("EvaluationNE_FP", ecsdNE_FP);
      EvaluationCounts ecn = null,ecnC = null,ecnNE = null,ecnUN = null,ecn_FP = null,ecnC_FP = null,ecnNE_FP = null;
      if (!evsettings.getPipelineType().name().contains("ONLY_DISAMBIGUATION")) {
        ecn = EvaluationCounts.summarize("EvaluationNer", ecsner);
        ecnC = EvaluationCounts.summarize("EvaluationNerC", ecsnerC);
        ecnNE = EvaluationCounts.summarize("EvaluationNerNE", ecsnerNE);
        ecnUN = EvaluationCounts.summarize("EvaluationNerUN", ecsnerUN);
        ecn_FP = EvaluationCounts.summarize("EvaluationNer_FP", ecsner_FP);
        ecnC_FP = EvaluationCounts.summarize("EvaluationNerC_FP", ecsnerC_FP);
        ecnNE_FP = EvaluationCounts.summarize("EvaluationNerNE_FP", ecsnerNE_FP);
      }

      evsettings.setExtraFlag(dsbuilder.toStringBeautiful() + evsettings.getExtraFlag());
      String config = "Summary Config \n"  +
      evsettings.getSettingInString() + "\n" +
      "Configuration \n" +
      ds.toStringBeautiful() + "\n";
      String evalInfo =  config +
          "Disambiguation \n" + ecd.toString() +
          "Disambiguation NE \n" + ecdNE.toString() +
          "Disambiguation C\n" + ecdC.toString() +
          "Disambiguation UN\n" + ecsUN.toString() +
          "DisambiguationFP \n" + ecd_FP.toString() +
          "DisambiguationFP NE \n" + ecdNE_FP.toString() +
          "DisambiguationFP C\n" + ecdC_FP.toString();
      if (!evsettings.getPipelineType().name().contains("ONLY_DISAMBIGUATION")) {
        evalInfo +=
            ("NER \n" + ecn.toString() +
            "NER NE\n" + ecnNE.toString() +
            "NER C\n" + ecnC.toString() +
            "NER UN\n" + ecnUN.toString() +
            "NERFP \n" + ecn_FP.toString() +
            "NERFP NE\n" + ecnNE_FP.toString() +
            "NERFP C\n" + ecnC_FP.toString());
      }
      evalInfo += "\n\nTiming\n" + RunningTimer.getDetailedOverview();

      FileUtils.writeStringToFile(
          new File(
              outputDirectory + "evaluation" + (cmd.hasOption("extendedName") ? ("_" + evsettings.getSettingInString()) : "")),
          evalInfo, encoding);
    }
    EntityLinkingManager.shutDown();
    NERManager.shutDown();
  }
}