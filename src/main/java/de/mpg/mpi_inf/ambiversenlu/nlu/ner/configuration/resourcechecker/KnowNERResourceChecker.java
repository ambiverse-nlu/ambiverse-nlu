package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;

public interface KnowNERResourceChecker {
	/**
	 * Returns null if there are no mistakes or message with explanation what is the problem.
	 * @return message with explanation what is the problem or null
	 */
	KnowNERResourceResult check() throws KnowNERLanguageConfiguratorException;
}
