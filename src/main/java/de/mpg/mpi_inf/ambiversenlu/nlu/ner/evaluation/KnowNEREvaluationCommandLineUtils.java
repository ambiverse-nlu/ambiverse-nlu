package de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.AbstractCommandLineUtils;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class KnowNEREvaluationCommandLineUtils extends AbstractCommandLineUtils {
	public KnowNEREvaluationCommandLineUtils() {
		super("KnowNEREvaluation", "KnowNEREvaluation");
	}
	@Override
	protected Options buildCommandLineOptions() throws ParseException {
		Options options = new Options();
		options
				.addOption(OptionBuilder
						.withLongOpt("model")
						.withDescription(
								"Path to the model directory (e.g. trained_models/<model>")
						.isRequired()
						.hasArg()
						.create("m"));
		options
				.addOption(OptionBuilder
						.withLongOpt("corpus")
						.withDescription(
								"c against which to run evaluation (e.g. conll, wiki500)")
						.hasArg()
						.create("c"));
		options
				.addOption(OptionBuilder
						.withLongOpt("labelling")
						.withDescription(
								"type of labelling you wanna check (e.g. single, multi)")
						.hasArg()
						.create("l"));

		return options;
	}
}
