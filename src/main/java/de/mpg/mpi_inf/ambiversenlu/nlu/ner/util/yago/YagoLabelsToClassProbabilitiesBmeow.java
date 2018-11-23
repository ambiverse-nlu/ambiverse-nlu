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
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.Constants;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.BmeowTag;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.BmeowTypePair;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.TsvReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Run this to create the tokenClassProbs-bmeow.tsv file from yago labels.
 * The output path can be specified in variable OUTPUT_FILE.
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
public class YagoLabelsToClassProbabilitiesBmeow {

	private static Map<String, Multiset<BmeowTypePair>> classTypeCounts = new HashMap<>();
	
	private static Set<String> stopwords;
	
	private static Set<String> symbols;
	
	
	public static void storeClassTypeProbabilities(String language, DictionaryEntriesDataProvider dictionaryEntriesDataProvider, String outputFile, String stopWordsPath, String symbolsPath) throws IOException, EntityLinkingDataAccessException, ClassNotFoundException, NoSuchMethodException, UIMAException, MissingSettingException, UnprocessableDocumentException {
		Set<String> stopwords = listToSet(stopWordsPath);
		Set<String> symbols = listToSet(symbolsPath);
		for (Map.Entry<String, List<DictionaryEntity>> e : dictionaryEntriesDataProvider) {
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
					String subject = de.entity;
					int internalId = DataAccess.getInternalIdForKBEntity(
							new KBIdentifiedEntity(Constants.YAGO_KB_IDENTIFIER + ":" + subject));
					int[] typeIds = DataAccess.getTypeIdsForEntityId(internalId);
					Set<NerType.Label> nerTypes = NerType.getNerTypesForTypeIds(typeIds);
					Tokens parse = UimaTokenizer.tokenize(Language.getLanguageForString(language), mention);
					for(int i = 0; i < parse.size(); i++) {
						BmeowTag tag;
						if (parse.size() == 1) {
							tag = BmeowTag.WORD;
						} else if (i == 0) {
							tag = BmeowTag.BEGIN;
						} else if (i < parse.size() - 1) {
							tag = BmeowTag.MIDDLE;
						} else {
							tag = BmeowTag.END;
						}
						Token token = parse.getToken(i);
						for (NerType.Label label : nerTypes) {
							if (!StringUtils.isNumeric(token.getOriginal()) &&
									!stopwords.contains(token.getOriginal()) &&
									!symbols.contains(token.getOriginal())) {
								addNerTypeLabel(token.getOriginal(), new BmeowTypePair(tag, label));
							}
						}
					}
				}
			}
		}
		writeCounts(outputFile);

	}
	public static void storeClassTypeProbabilities(String language, String pathToYagoLabels, String outputFile, String stopWordsPath, String symbolsPath) throws IOException, EntityLinkingDataAccessException, ClassNotFoundException, NoSuchMethodException, UIMAException, MissingSettingException, UnprocessableDocumentException {
		stopwords = listToSet(stopWordsPath);
		symbols = listToSet(symbolsPath);

		TsvReader yagoLabels = new TsvReader(new File(pathToYagoLabels));
		while(yagoLabels.hasNext()){
			Fact fact = yagoLabels.next();
			if(fact.getRelation().equals("skos:prefLabel") && language.equals("en") ||
					fact.getRelation().equals("rdfs:label") && !language.equals("en")){
				String subject = fact.getSubject();
				if(!subject.contains("<wordnet") && !subject.contains("/")){
					String object = fact.getObject();
					if(object.contains("@"+ Language.get3letterLanguage(language))){
						try {
							int internalId = DataAccess.getInternalIdForKBEntity(
                                    new KBIdentifiedEntity(Constants.YAGO_KB_IDENTIFIER + ":" + subject));
							int[] typeIds = DataAccess.getTypeIdsForEntityId(internalId);
							Set<NerType.Label> nerTypes = NerType.getNerTypesForTypeIds(typeIds);
							String mention = fact.getObjectAsJavaString();
							mention = mention.replaceAll("\\(.*\\)", "");
							Tokens parse = UimaTokenizer.tokenize(Language.getLanguageForString(language), mention);
							for(int i = 0; i < parse.size(); i++) {
								BmeowTag tag;
								if (parse.size() == 1) {
									tag = BmeowTag.WORD;
								} else if (i == 0) {
									tag = BmeowTag.BEGIN;
								} else if (i < parse.size() - 1) {
									tag = BmeowTag.MIDDLE;
								} else {
									tag = BmeowTag.END;
								}
								Token token = parse.getToken(i);
								for (NerType.Label label : nerTypes) {
									if (!StringUtils.isNumeric(token.getOriginal()) &&
											!stopwords.contains(token.getOriginal()) &&
											!symbols.contains(token.getOriginal())) {
										addNerTypeLabel(token.getOriginal(), new BmeowTypePair(tag, label));
									}
								}
							}
						} catch (EntityLinkingDataAccessException e) {
								throw new IOException(e);
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
		writer.write("#Token\tP(B-PER)\tP(M-PER)\tP(E-PER)\tP(W-PER)\t"
				+ "P(B-ORG)\tP(M-ORG)\tP(E-ORG)\tP(W-ORG)\t"
				+ "P(B-LOC)\tP(M-LOC)\tP(E-LOC)\tP(W-LOC)\t"
				+ "P(B-MISC)\tP(M-MISC)\tP(E-MISC)\tP(W-MISC)\n");

		for(String token : classTypeCounts.keySet()){
			int[] counts = new int[16];
			//PERSON
			counts[0] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.BEGIN, NerType.Label.PERSON));
			counts[1] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.MIDDLE, NerType.Label.PERSON));
			counts[2] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.END, NerType.Label.PERSON));
			counts[3] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.WORD, NerType.Label.PERSON));

			//ORG
			counts[4] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.BEGIN, NerType.Label.ORGANIZATION));
			counts[5] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.MIDDLE, NerType.Label.ORGANIZATION));
			counts[6] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.END, NerType.Label.ORGANIZATION));
			counts[7] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.WORD, NerType.Label.ORGANIZATION));

			//LOC
			counts[8] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.BEGIN, NerType.Label.LOCATION));
			counts[9] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.MIDDLE, NerType.Label.LOCATION));
			counts[10] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.END, NerType.Label.LOCATION));
			counts[11] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.WORD, NerType.Label.LOCATION));

			//MISC
			counts[12] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.BEGIN, NerType.Label.MISC));
			counts[13] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.MIDDLE, NerType.Label.MISC));
			counts[14] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.END, NerType.Label.MISC));
			counts[15] = classTypeCounts.get(token).count(new BmeowTypePair(BmeowTag.WORD, NerType.Label.MISC));

			double total = IntStream.of(counts).sum();

			if(total > 0){
				writer.write(token + "\t");
				for (int i = 0; i < counts.length; i++) {
					writer.write(String.valueOf(counts[i]/total));
					if(i < counts.length -1){
						writer.write("\t");
					}
				}
				writer.write("\n");
			}

		}

		writer.close();
	}

	private static void addNerTypeLabel(String token, BmeowTypePair pair) {
		Multiset<BmeowTypePair> labelCount = classTypeCounts.get(token);
		
		if(labelCount == null){
			labelCount = HashMultiset.create();
		}
		
		labelCount.add(pair);
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
	
	
	public static void main(String[] args) throws IOException, NoSuchMethodException, MissingSettingException, EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, UnprocessableDocumentException {
		storeClassTypeProbabilities(args[0], args[1], args[2], args[3], args[4]);
	}

}
