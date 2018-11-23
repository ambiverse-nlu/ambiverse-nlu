/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.DictionaryTrie;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.CountryDictionaryMatch;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.PosDictionaryMatch;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.YagoDictionaryMatch;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *         <p>
 *         This finds the largest possible match in the YAGO mentions
 *         dictionary and the frequent POS sequence list.
 *         <p>
 *         REQUIREMENTS: You need to provide the file YAGO_DICTIONARY
 *         which can be created using the Python script
 *         python/generateYagoDictionary.py
 *         and POS_DICTIONARY, which was created by Luciano del Corro
 */
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs = {
                "YagoDictionaryMatch",
                "PosDictionaryMatch",
                "CountryDictionaryMatch"
        })
public class DictionaryMatchAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_LANGUAGE = "language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    private static final String YAGO_DICTIONARY = "yagoDictionary";

    private static final String COUNTRY_DICTIONARY = "gazeteers/known_country.csv";

    private static final String POS_DICTIONARY = "posDictionary";

//    private static final String WORDNET_DICTIONARY = "wn-words.txt";

//    private static final Map<String, AnalysisEngine> wordNet = new HashMap<>();

    private static final Logger logger = Logger.getLogger(DictionaryMatchAnnotator.class);

    private static final Map<String, DictionaryTrie> yagoTrie = new HashMap<>();
    private static final Map<String, DictionaryTrie> countryTrie = new HashMap<>();
    private static final Map<String, DictionaryTrie>  posTrie = new HashMap<>();

    private static void initForLanguage(String language) {
        Path lowercaseCountriesPath = null;
        try {
            String langResourcesPath = KnowNERSettings.getLangResourcesPath(language);
            lowercaseCountriesPath = Files.createTempFile("country_lowercase.lst", "");
            Files.write(lowercaseCountriesPath, Files.readAllLines(Paths.get(langResourcesPath + COUNTRY_DICTIONARY))
                    .stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
//            wordNet.put(language, createEngine(DictionaryAnnotator.class,
//                    DictionaryAnnotator.PARAM_ANNOTATION_TYPE, WordNetDictionaryMatch.class,
//                    DictionaryAnnotator.PARAM_MODEL_LOCATION, langResourcesPath + WORDNET_DICTIONARY));
            yagoTrie.put(language, new DictionaryTrie(langResourcesPath + KnowNERSettings.getDumpName() + "_" + YAGO_DICTIONARY, " "));
            countryTrie.put(language, new DictionaryTrie(lowercaseCountriesPath.toString(), " "));

            posTrie.put(language, new DictionaryTrie(langResourcesPath + POS_DICTIONARY, "*"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lowercaseCountriesPath != null) {
                try {
                    Files.delete(lowercaseCountriesPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        initForLanguage(language);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String language = jCas.getDocumentLanguage();
//        wordNet.get(language).process(jCas);


        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            //Annotate tokens
            List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
            for (int i = 0; i < tokens.size(); i++) {
                Token firstToken = tokens.get(i);
                List<Token> currentTokenSequence = new ArrayList<>();
                currentTokenSequence.add(firstToken);
                String tokenSequenceText = tokenSequenceToString(currentTokenSequence);

                if (!yagoTrie.get(language).contains(tokenSequenceText) || StringUtils.isNumeric(tokenSequenceText)) {
                    logger.trace("Token not added: " + tokenSequenceText);
                    continue;
                }
                for (int j = i + 1; j < tokens.size(); ) {
                    Token currentToken = tokens.get(j);
                    currentTokenSequence.add(currentToken);
                    tokenSequenceText = tokenSequenceToString(currentTokenSequence);
                    if (yagoTrie.get(language).contains(tokenSequenceText)) {
                        j++;
                        if (j == tokens.size()) {
                            addYagoDictionaryMatch(jCas, currentTokenSequence);
                        }
                    } else {
                        while (j >= i) {
                            currentTokenSequence.remove(currentTokenSequence.size() - 1);
                            tokenSequenceText = tokenSequenceToString(currentTokenSequence);
                            if (yagoTrie.get(language).isEntity(tokenSequenceText)) {
                                addYagoDictionaryMatch(jCas, currentTokenSequence);
                                i = j;
                                break;
                            } else {
                                j--;
                            }

                        }
                        break;
                    }
                }
            }

            //Annotate country
            for (int i = 0; i < tokens.size(); i++) {
                Token firstToken = tokens.get(i);
                List<Token> currentTokenSequence = new ArrayList<>();
                currentTokenSequence.add(firstToken);
                String tokenSequenceText = tokenSequenceToStringLowercase(currentTokenSequence);

                if (!countryTrie.get(language).contains(tokenSequenceText)) {
                    logger.trace("Token not added: " + tokenSequenceText);
                    continue;
                }
                for (int j = i + 1; j < tokens.size(); ) {
                    Token currentToken = tokens.get(j);
                    currentTokenSequence.add(currentToken);
                    tokenSequenceText = tokenSequenceToStringLowercase(currentTokenSequence);
                    if (countryTrie.get(language).contains(tokenSequenceText)) {
                        j++;
                        if (j == tokens.size()) {
                            addCountryDictionaryMatch(jCas, currentTokenSequence);
                        }
                    } else {
                        while (j >= i) {
                            currentTokenSequence.remove(currentTokenSequence.size() - 1);
                            tokenSequenceText = tokenSequenceToStringLowercase(currentTokenSequence);
                            if (countryTrie.get(language).isEntity(tokenSequenceText)) {
                                addCountryDictionaryMatch(jCas, currentTokenSequence);
                                i = j;
                                break;
                            } else {
                                j--;
                            }

                        }
                        break;
                    }
                }
            }

            //Annotate POS tags
            List<POS> posTags = JCasUtil.selectCovered(jCas, POS.class, sentence);

            for (int i = 0; i < posTags.size(); i++) {
                POS firstPos = posTags.get(i);
                List<POS> currentPosSequence = new ArrayList<>();
                currentPosSequence.add(firstPos);
                String posSequenceText = posSequenceToString(currentPosSequence);

                if (!posTrie.get(language).contains(posSequenceText)) {
                    logger.trace("POS not added: " + posSequenceText);
                    continue;
                }
                for (int j = i + 1; j < posTags.size(); ) {
                    POS currentPos = posTags.get(j);
                    currentPosSequence.add(currentPos);
                    posSequenceText = posSequenceToString(currentPosSequence);
                    if (posTrie.get(language).contains(posSequenceText)) {
                        j++;
                        if (j == posTags.size()) {
                            addPosDictionaryMatch(jCas, currentPosSequence);
                        }
                    } else {
                        while (j >= i) {
                            currentPosSequence.remove(currentPosSequence.size() - 1);
                            posSequenceText = posSequenceToString(currentPosSequence);
                            if (posTrie.get(language).isEntity(posSequenceText)) {
                                addPosDictionaryMatch(jCas, currentPosSequence);
                                i = j;
                                break;
                            } else {
                                j--;
                            }

                        }
                        break;
                    }
                }
            }

        }

    }

    private void addPosDictionaryMatch(JCas jCas, List<POS> currentPosSequence) {
        for (POS pos : currentPosSequence) {
            PosDictionaryMatch match = new PosDictionaryMatch(jCas,
                    pos.getBegin(), pos.getEnd());
            match.addToIndexes();
            logger.trace("Pos match added for tag: " + pos.getPosValue() +
                    " from sequence: " + posSequenceToString(currentPosSequence) +
                    " for tokens: " + currentPosSequence.stream()
                    .map(Annotation::getCoveredText)
                    .collect(Collectors.joining(" ")));
        }

    }

    private void addYagoDictionaryMatch(JCas jCas, List<Token> currentTokenSequence) {
        for (Token token : currentTokenSequence) {
            YagoDictionaryMatch match = new YagoDictionaryMatch(jCas,
                    token.getBegin(), token.getEnd());
            match.addToIndexes();
            logger.trace("Yago match added for token: " + token.getCoveredText() +
                    " from sequence: " + tokenSequenceToString(currentTokenSequence));
        }

    }

    private void addCountryDictionaryMatch(JCas jCas, List<Token> currentTokenSequence) {
        for (Token token : currentTokenSequence) {
            CountryDictionaryMatch match = new CountryDictionaryMatch(jCas,
                    token.getBegin(), token.getEnd());
            match.addToIndexes();
            logger.trace("Country match added for token: " + token.getCoveredText() +
                    " from sequence: " + tokenSequenceToStringLowercase(currentTokenSequence));
        }

    }

    private String tokenSequenceToString(List<Token> currentTokenSequence) {
        StringBuffer sb = new StringBuffer();
        currentTokenSequence.forEach(s -> sb.append(s.getCoveredText()).append(" "));
        return sb.toString().trim();
    }

    private String tokenSequenceToStringLowercase(List<Token> currentTokenSequence) {
        StringBuffer sb = new StringBuffer();
        currentTokenSequence.forEach(s -> sb.append(s.getCoveredText().toLowerCase()).append(" "));
        return sb.toString().trim();
    }

    private String posSequenceToString(List<POS> currentPosSequence) {
        StringBuffer sb = new StringBuffer();
        currentPosSequence.forEach(pos -> sb.append(pos.getPosValue()).append(" "));
        return sb.toString().trim();
    }

}
