package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.AbstractCommandLineUtils;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class WikiCorpusGeneratorCommandLineUtils extends AbstractCommandLineUtils {
	public WikiCorpusGeneratorCommandLineUtils() {
		super("WikiCorpusGenerator");
	}

	@Override
	protected Options buildCommandLineOptions() throws ParseException {
		Options options = new Options();
		options
				.addOption(OptionBuilder
						.isRequired()
						.withLongOpt("corpusprefix")
						.withDescription(
								"Prefix for the generated corpus")
						.hasArg()
						.create("p"));
		options
				.addOption(OptionBuilder
						.withLongOpt("thresholds")
						.withDescription(
								"List of entity thresholds in format [0.2,1.0]. 0.2 means that only 20% of the most " +
										"similar entities are considered (measured by milneSimilarity). By default [1.0]")
						.hasArg()
						.create("t"));
		options
				.addOption(OptionBuilder
						.isRequired()
						.withLongOpt("language")
						.withDescription(
								"Language of the corpus")
						.hasArg()
						.create("l"));
		options
				.addOption(OptionBuilder
						.withLongOpt("source")
						.withDescription(
								"This points to the source of the documents. Each document is based on wikipedia article about " +
										"the given entity. Base takes one of the following values: CONLL_ENTITIES, " +
										"ENTITY_RANK. If base is not specified then we generate a test corpus for manual " +
										"evaluation from hard coded wikipedia articles.")
						.hasArg()
						.create("s"));
		options
				.addOption(OptionBuilder
						.withLongOpt("base")
						.withDescription(
								"Aida document to which we append the entities")
						.hasArg()
						.create("b"));
		options
				.addOption(OptionBuilder
						.withLongOpt("appendtobase")
						.withDescription(
								"append the resulting document to base if base is provided.")
						.create("a"));
		options
				.addOption(OptionBuilder
						.withLongOpt("maximum")
						.withDescription(
								"Maximum number of the documents in the corpus. The generator can produce less then the " +
										"given number in case of small number of available articles or if it was unable to" +
										" find the wikipedia article in the given language or if the article was too small" +
										" or if there were no mentions found for the entities in the article")
						.hasArg()
						.create("m"));
		return options;
	}
}
