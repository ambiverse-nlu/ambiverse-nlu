/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERMentionTokenCountsChecker;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.TokenCountsUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This is a helper class that provides static access to mention token counts
 */
public class MentionTokenWeightsFactory {

	public static Map<String, Double> getMentionWeightsForLanguage(String language) throws IOException, ClassNotFoundException {
		File mtfcFile = new File(KnowNERSettings.getLangResourcesPath(language) +
										KnowNERSettings.getDumpName() + "_" +
										KnowNERMentionTokenCountsChecker.FILE_SUFFIX);

		MentionTokenFrequencyCounts counts = new MentionTokenFrequencyCounts(language, mtfcFile);
		return new TokenCountsUtil(counts).getMentionWeights();
	}
}
