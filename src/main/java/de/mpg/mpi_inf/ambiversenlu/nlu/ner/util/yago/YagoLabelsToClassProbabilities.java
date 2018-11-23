/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.yago;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3DictionaryEntriesSources;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.Constants;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType.Label;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.TsvReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;

import java.io.*;
import java.util.*;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Run this to create the tokenClassProbs.tsv file from yago labels.
 * The output path can be specified in variable OUTPUT_FILE.
 *
 * If the language is english, then it searches for skos:prefLabel mentions, otherwise rdfs:label
 *
 * REQUIREMENTS: The path to the yagoLabels.tsv file needs to be
 * specified in variable PATH_TO_YAGO_LABELS. It can be found
 * on the yago homepage at:
 * http://resources.mpi-inf.mpg.de/yago-naga/yago/download/yago/yagoLabels.tsv.7z
 * You need to unzip the file first!
 * 
 * You also need to have a file with a list of stopwords at PATH_TO_STOPWORDS and
 * a list of symbols at PATH_TO_SYMBOLS
 * 
 */
public class YagoLabelsToClassProbabilities {
	
	private static Map<String, Multiset<Label>> classTypeCounts = new HashMap<>();

	public static void storeClassTypeProbabilities(String language, DictionaryEntriesDataProvider dictionaryEntriesDataProvider,
												   String outputFile, String stopWordsPath, String symbolsPath)
			throws EntityLinkingDataAccessException, MissingSettingException,
			IOException, ClassNotFoundException, UnprocessableDocumentException, NoSuchMethodException, UIMAException {
		Set<String> stopwords = listToSet(stopWordsPath);
		Set<String> symbols = listToSet(symbolsPath);

		for (Map.Entry<String, List<DictionaryEntity>> e : dictionaryEntriesDataProvider) {
			String mention = e.getKey();
			for (DictionaryEntity de : e.getValue()) {
				if (de.language.name().equals(language) &&
						!de.knowledgebase.toLowerCase().contains("wordnet") &&
						(!"en".equals(language) &&
								Yago3DictionaryEntriesSources.LABEL.equals(de.source) ||
								"en".equals(language) &&
										Yago3DictionaryEntriesSources.PREF_LABEL.equals(de.source))) {
					String subject = de.entity;
					int internalId = DataAccess.getInternalIdForKBEntity(
							new KBIdentifiedEntity(Constants.YAGO_KB_IDENTIFIER + ":" + subject));
					int[] typeIds = DataAccess.getTypeIdsForEntityId(internalId);
					Set<Label> nerTypes = NerType.getNerTypesForTypeIds(typeIds);
					for (Token token : UimaTokenizer.tokenize(Language.getLanguageForString(language), mention)) {
						for (Label label : nerTypes) {
							if (!StringUtils.isNumeric(token.getOriginal()) &&
									!stopwords.contains(token.getOriginal()) &&
									!symbols.contains(token.getOriginal())) {
								addNerTypeLabel(token.getOriginal(), label);
							}
						}
					}
				}
			}
		}
		writeCounts(outputFile);
	}
    public static void storeClassTypeProbabilities(String language, String pathToYagoLabels, String outputFile, String stopWordsPath, String symbolsPath) throws IOException,
			MissingSettingException, ClassNotFoundException, NoSuchMethodException, UIMAException {
		Set<String> stopwords = listToSet(stopWordsPath);
		Set<String> symbols = listToSet(symbolsPath);

		TsvReader yagoLabels = new TsvReader(new File(pathToYagoLabels));

		while(yagoLabels.hasNext()){
			Fact fact = yagoLabels.next();
			if(fact.getRelation().equals("skos:prefLabel") && language.equals("en") ||
					fact.getRelation().equals("rdfs:label") && !language.equals("en")){
				String subject = fact.getSubject();
				if(!subject.contains("<wordnet") && !subject.contains("/")){
					String object = fact.getObject();
					if(object.contains("@" + Language.get3letterLanguage(language))){
						try {
							int internalId = DataAccess.getInternalIdForKBEntity(
                                    new KBIdentifiedEntity(Constants.YAGO_KB_IDENTIFIER + ":" + subject));
							int[] typeIds = DataAccess.getTypeIdsForEntityId(internalId);
							Set<Label> nerTypes = NerType.getNerTypesForTypeIds(typeIds);
							String mention = fact.getObjectAsJavaString();
							mention = mention.replaceAll("\\(.*\\)", "");

							for(Token token : UimaTokenizer.tokenize(Language.getLanguageForString(language), mention)){
								for (Label label : nerTypes) {
									if(!StringUtils.isNumeric(token.getOriginal()) &&
										!stopwords.contains(token.getOriginal()) &&
										!symbols.contains(token.getOriginal())){
										addNerTypeLabel(token.getOriginal(), label);
									}
								}
							}
						} catch (EntityLinkingDataAccessException e) {
							throw new IOException(e);
						} catch (UnprocessableDocumentException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		writeCounts(outputFile);
		yagoLabels.close();
	}

	private static void writeCounts(String outputFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
		writer.write("#Token\tP(PER)\tP(ORG)\tP(LOC)\tP(MISC)\n");

		for(String token : classTypeCounts.keySet()){
			int person = classTypeCounts.get(token).count(Label.PERSON);
			int organization = classTypeCounts.get(token).count(Label.ORGANIZATION);
			int location = classTypeCounts.get(token).count(Label.LOCATION);
			int misc = classTypeCounts.get(token).count(Label.MISC);
			double total = person + organization + location + misc;
			if(total > 0){
				writer.write(token + "\t"
						+ person/total + "\t"
						+ organization/total + "\t"
						+ location/total + "\t"
						+ misc/total
						+ "\n");
			}

		}

		writer.close();
	}

	private static void addNerTypeLabel(String token, Label label) {
		Multiset<Label> labelCount = classTypeCounts.get(token);
		
		if(labelCount == null){
			labelCount = HashMultiset.create();
		}
		
		labelCount.add(label);
		classTypeCounts.put(token, labelCount);
	}
	
	private static Set<String> listToSet(String file) throws FileNotFoundException{
		Set<String> set = new HashSet<>();
		
		FileLines reader = new FileLines(new BufferedReader(new FileReader(new File(file))));
		while(reader.hasNext()){
			set.add(reader.next());
		}
		
		reader.close();
		
		return set;
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, UIMAException, MissingSettingException {
        storeClassTypeProbabilities("en", "/home/tdembelo/Downloads/yagoLabels.tsv",  "/home/tdembelo/IdeaProjects/ner/src/main/resources/yago/en/tokenClassProbs.tsv",
				"/home/tdembelo/IdeaProjects/entity-linking/src/main/resources/ner/en/stopwords.txt", "/home/tdembelo/IdeaProjects/entity-linking/src/main/resources/ner/en/symbols.txt");
	}

}
