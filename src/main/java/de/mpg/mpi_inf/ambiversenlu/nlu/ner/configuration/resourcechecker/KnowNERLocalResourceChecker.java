package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.KnowNERLanguageConfiguratorException;

import java.sql.SQLException;

/**
 * Abstract class which holds information about language and its configuration directory
  */
public abstract class KnowNERLocalResourceChecker implements KnowNERResourceChecker {

	protected final String mainDir;
	protected final String language;
	protected final String languageDir;

	/**
	 * Does not check the parameters as it should be checked before
	 * @param mainDir
	 * @param language
	 */
	public KnowNERLocalResourceChecker(String mainDir, String language) {
		this.mainDir = mainDir;
		this.language = language;
		this.languageDir = mainDir + (mainDir.endsWith("/")? "" : "/") + language;
	}

	static String getAidaPrefix() throws KnowNERLanguageConfiguratorException {
		try {
			return EntityLinkingManager.getAidaDbIdentifierLight() + "_";
		} catch (SQLException e) {
			throw new KnowNERLanguageConfiguratorException(e);
		}
	}
}
