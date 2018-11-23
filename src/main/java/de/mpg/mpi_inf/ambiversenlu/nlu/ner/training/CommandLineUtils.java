package de.mpg.mpi_inf.ambiversenlu.nlu.ner.training;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.AbstractCommandLineUtils;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Used for new language configuration
 */

enum Option {
	FILE("f"),
	LANGUAGE("l"),
	MODE("m"),
	ENTITY_STANDARD("s"),
	EVALUATION("e"),
	PREFIX("p"),
	PREFIX_DIR("d"),
	TRAINED_POSITION_TYPE("t"),
//	USE_LEMMATIZER("useLemmatizer"),
//	USE_POSTAGGER("usePOSTagger"),
	CORPUS("c");

	final String name;

	Option(String name) {
		this.name = name;
	}
}

public class CommandLineUtils extends AbstractCommandLineUtils {

	public CommandLineUtils() {
		super("NerTrainer", "NerTrainer");
	}

	@Override
	protected Options buildCommandLineOptions() throws ParseException {
		Options options = new Options();
		options
				.addOption(OptionBuilder
						.withLongOpt("corpus")
						.withDescription(
								"Available options: conll, wiki500 (check in src/main/resources/ner/generated_configurations/<lang>/)")
						.hasArg()
						.isRequired()
						.create(Option.CORPUS.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("prefix")
						.withDescription(
								"Prefix")
						.hasArg()
						.create(Option.PREFIX.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("prefixdir")
						.withDescription(
								"Parent directory")
						.hasArg()
						.create(Option.PREFIX_DIR.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("mode")
						.withDescription(
								"With mode=TEST we train the model on the \'train\' set " +
										"and evaluate on the \'testa\' set. " +
										"With mode=DEV we train the model on the \'train=testa\' set " +
										"and evaluate on the \'testb\' set")
						.isRequired()
						.hasArg()
						.create(Option.MODE.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("language")
						.withDescription(
								"A language of the model we want to train (i.e. en or de)")
						.isRequired()
						.hasArg()
						.create(Option.LANGUAGE.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("entitystandard")
						.withDescription(
								"For the NED stage we need to specify what entities will be our ground truth. " +
										"It can be either \'GOLD\' or \'AIDA\'.")
						.hasArg()
						.create(Option.ENTITY_STANDARD.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("file")
						.withDescription(
								"Configuration file with features")
						.isRequired()
						.hasArg()
						.create(Option.FILE.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("evaluation")
						.withDescription(
								"Pass the option if you also need to evaluate the model")
						.create(Option.EVALUATION.name));
		options
				.addOption(OptionBuilder
						.withLongOpt("trainpositiontype")
						.isRequired()
						.hasArg()
						.withDescription(
								"Accepts two values ORIGINAL and BMEOW, it defines which position-tags " +
										"will be used for predictions by the trained model")
						.create(Option.TRAINED_POSITION_TYPE.name));

//		options.addOption(OptionBuilder
//				.withDescription("Pass the option if the data does not contain lemma tags")
//				.create(Option.USE_LEMMATIZER.name));
//		options.addOption(OptionBuilder
//				.withDescription("Pass the option if the data does not contain POS tags")
//				.create(Option.USE_POSTAGGER.name));

		options.addOption(OptionBuilder.withLongOpt("help").create('h'));
		return options;
	}
}
