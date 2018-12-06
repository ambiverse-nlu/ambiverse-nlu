package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineUtils extends AbstractCommandLineUtils {

  public CommandLineUtils() {
    super("AmbiverseNLU", "CommandLineRunner");
  }

  @Override
  protected Options buildCommandLineOptions() throws ParseException {
    Options options = new Options();
    options
        .addOption(OptionBuilder
            .withLongOpt("directory")
            .withDescription(
                "An input directory.")
            .hasArg()
            .create("d"));
    options
        .addOption(OptionBuilder
            .withLongOpt("technique")
            .withDescription(
                "Set the disambiguation-technique to be used: PRIOR, LOCAL, FAST-LOCAL, LOCAL-IDF, LM, GRAPH, GRAPH-IDF, GRAPH-KORE, or COLLECTION. Default is GRAPH.")
            .hasArg()
            .withArgName("TECHNIQUE")
            .create("t"));
    options
        .addOption(OptionBuilder
            .withLongOpt("outputformat")
            .withDescription(
                "Set the output-format to be used: HTML, JSON, TSV, ALL. Default is HTML.")
            .hasArg()
            .withArgName("FORMAT")
            .create("o"));
    options
        .addOption(OptionBuilder
            .withLongOpt("documentid")
            .withDescription(
                "Set the field for the document ID. Use for XML or JSON input to specify where the document ID is stored.")
            .hasArg()
            .withArgName("DOCUMENTID")
            .create("did"));
    options
        .addOption(OptionBuilder
            .withLongOpt("threadcount")
            .withDescription(
                "Set the number of documents to be processed in parallel.")
            .hasArg()
            .withArgName("COUNT")
            .create("c"));
    options
        .addOption(OptionBuilder
            .withLongOpt("numChunkThread")
            .withDescription("Set the maximum number of chunk disambiguation threads to be created.")
            .hasArg()
            .withArgName("NUMCHUNKTHREAD")
            .create("x"));
    options
        .addOption(OptionBuilder
            .withLongOpt("minmentioncount")
            .withDescription(
                "Set the minimum occurrence count of a mention to be considered for disambiguation. Default is 1.")
            .hasArg()
            .withArgName("COUNT")
            .create("m"));
    options
        .addOption(OptionBuilder
            .withLongOpt("confidencethreshold")
            .withDescription(
                "Sets the confidence threshold below which to drop entities. Default is given by the technique.")
            .hasArg()
            .withArgName("THRESHOLD")
            .create("r"));
    options
        .addOption(OptionBuilder
            .withLongOpt("encoding")
            .withDescription(
                "String encoding of the input file(s).")
            .hasArg()
            .withArgName("ENCODING")
            .create("n"));
    options
        .addOption(OptionBuilder
            .withLongOpt("language")
            .withDescription(
                "Collection language.")
            .hasArg()
            .withArgName("LANGUAGE")
            .create("l"));
    options
        .addOption(OptionBuilder
            .withLongOpt("evaluate")
            .withDescription(
                "Run Evaluation.")
            .hasOptionalArg()
            .withArgName("evaluation settings directory")
            .create("e"));
    options
        .addOption(OptionBuilder
            .withLongOpt("reader")
            .withDescription(
                "Collection reader type")
            .hasArg()
            .withArgName("reader type")
            .create("i"));

    options
        .addOption(OptionBuilder
            .withLongOpt("start")
            .withDescription(
                "Where to start processing documents")
            .hasArg()
            .withArgName("doc. number start")
            .create("b"));

    options
        .addOption(OptionBuilder
            .withLongOpt("finish")
            .withDescription(
                "Where to finish document processing")
            .hasArg()
            .withArgName("doc. number finish")
            .create("f"));
    options.addOption(OptionBuilder
        .withLongOpt("kEntities")
        .withDescription("Sets the factor of how many entities to include in the initial graph (i * mentions)")
        .hasArg()
        .withArgName("factor")
        .withArgName("k")
        .create());
    options.addOption(OptionBuilder
        .withLongOpt("cSettings")
        .withDescription("TrainingSettings for the collection to be loaded")
        .hasArg()
        .withArgName("collection settings")
        .create("z"));
    options.addOption(OptionBuilder
        .withLongOpt("cPart")
        .withDescription("Collection part")
        .hasArg()
        .withArgName("collection part")
        .create("p"));
    options.addOption(OptionBuilder
        .withLongOpt("exhaustive search")
        .withDescription("if present, run exhaustive search to solve disambiguation, otherwise local search")
        .withArgName("exhaustive search")
        .create("u"));
    options.addOption(OptionBuilder
        .withLongOpt("use normalized objective")
        .withDescription("If present, use normalized objective for graph - otherwise, divide edge weighted degree by number of remaining entities")
        .withArgName("normalized objective")
        .create("w"));
    options.addOption(OptionBuilder
        .withLongOpt("prune candidates")
        .withDescription("Use coherence mention pruning, keep K candidates")
        .hasArg()
        .withArgName("prune candidates")
        .create("y"));
    options.addOption(OptionBuilder
        .withLongOpt("compute confidence")
        .withDescription("If present, use confidence computation instead of local similarity for scores")
        .withArgName("compute confidence")
        .create("v"));
    options.addOption(OptionBuilder
        .withLongOpt("ME multiplier")
        .withDescription("a is multiplied to ME edges, 1-a to EE edges (a is in [0.0, 1.0])")
        .hasArg()
        .withArgName("ME multiplier")
        .create("a"));
    options.addOption(OptionBuilder
        .withLongOpt("coherence robustness NE")
        .withDescription("Use coherence robustness with the given threshold")
        .hasArg()
        .withArgName("coherence robustness NE")
        .create("hNE"));
    options.addOption(OptionBuilder
        .withLongOpt("coherence robustness C")
        .withDescription("Use coherence robustness with the given threshold")
        .hasArg()
        .withArgName("coherence robustness C")
        .create("hC"));
    options.addOption(OptionBuilder
        .withLongOpt("coherence confidence threshold")
        .withDescription("Use coherence confidence threshold, fix mentions > local sim threshold ")
        .hasArg()
        .withArgName("coherence confidence threshold")
        .create("q"));

    options.addOption(OptionBuilder
        .withLongOpt("similarity setting name NE")
        .withDescription("similarity setting used in disambiguation and cohrence robustnes test for named entities")
        .hasArg()
        .withArgName("similarity setting name NE")
        .create("ssNE"));
    options.addOption(OptionBuilder
        .withLongOpt("similarity setting name C")
        .withDescription("similarity setting used in disambiguation and cohrence robustnes test for concepts")
        .hasArg()
        .withArgName("similarity setting name C")
        .create("ssC"));
    options.addOption(OptionBuilder
      .withLongOpt("coherence config NE")
      .withDescription("use which methods for coherence ")
      .hasArg()
      .withArgName("coherence config NE")
      .create("ccNE"));
    options.addOption(OptionBuilder
        .withLongOpt("coherence config C")
        .withDescription("use which methods for coherence ")
        .hasArg()
        .withArgName("coherence config C")
        .create("ccC"));
    options.addOption(OptionBuilder
        .withLongOpt("training corpus")
        .withDescription("the corpus used for training ")
        .hasArg()
        .withArgName("training corpus")
        .create("tc"));
    options.addOption(OptionBuilder
        .withLongOpt("extended name")
        .withDescription("if present, some config is added as suffix to evaluation result file")
        .withArgName("extended name")
        .create("extendedName"));
    options.addOption(OptionBuilder
        .withLongOpt("with tracing")
        .withDescription("if present, create tracing files ")
        .withArgName("tracing")
        .create("tr"));
    options.addOption(OptionBuilder
        .withLongOpt("pipeline")
        .withDescription("Pipeline to be executed")
        .hasArg()
        .withArgName("pipeline")
        .create("pip"));


    options.addOption(OptionBuilder.withLongOpt("help").create('h'));
    return options;
  }
}