package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.yago;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3DictionaryEntriesSources;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.TsvReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YagoLabelsToYagoDictionary {

	public static void main(String[] args) {
		generateYagoDictionary(args[0], args[1], Paths.get(args[2]));
	}

	private static final Logger logger = Logger.getLogger(YagoLabelsToYagoDictionary.class);

	public static boolean generateYagoDictionary(String language, DictionaryEntriesDataProvider provider, Path path) {
		List<String> dictionary = new ArrayList<>();
		for (Map.Entry<String, List<DictionaryEntity>> e : provider) {
			String mention = e.getKey();
			if (mention == null || "".equals(mention.trim())) {
				continue;
			}
			for (DictionaryEntity de : e.getValue()) {
				if (de.language.name().equals(language) &&
						!de.knowledgebase.toLowerCase().contains("wordnet") &&
						(!"en".equals(language) &&
								Yago3DictionaryEntriesSources.LABEL.equals(de.source) ||
								"en".equals(language) &&
										Yago3DictionaryEntriesSources.PREF_LABEL.equals(de.source))) {
					dictionary.add(mention);
					break;
				}
			}
		}

		try {
			Files.write(path, dictionary);
		} catch (IOException e) {
			logger.error("Could not write to " + path, e);
			return false;
		}
		return true;
	}

	public static boolean generateYagoDictionary(String language, String yagoLabelsFile, Path path) {


		List<String> dictionary = new ArrayList<>();

		try {
			TsvReader yagoLabels = new TsvReader(new File(yagoLabelsFile));

			while (yagoLabels.hasNext()) {
				Fact fact = yagoLabels.next();
				if (fact.getRelation().equals("skos:prefLabel") && language.equals("en") ||
						fact.getRelation().equals("rdfs:label") && !language.equals("en") ) {
					String subject = fact.getSubject();
					if (!subject.contains("<wordnet") && !subject.contains("/")) {
						String object = fact.getObject();
						if (object.contains("@" + Language.get3letterLanguage(language))) {
							String mention = fact.getObjectAsJavaString();
							dictionary.add(mention.replaceAll("\\(.*\\)", ""));
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not read " + yagoLabelsFile, e);
			return false;
		}

		try {
			Files.write(path, dictionary);
		} catch (IOException e) {
			logger.error("Could not write to " + path, e);
			return false;
		}
		return true;
	}
}
