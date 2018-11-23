/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors.dictionary;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.DictionaryFeaturesAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.DictionaryFeatureAnnotation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts the binary features for dictionaries.
 * <p>
 * REQUIREMENTS: Run {@link DictionaryFeaturesAnnotator}
 * as part of the pre-processing pipeline.
 */
public class DictionariesExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
    //
//    public static final String DICTIONARIES_LOCATION = "dictionariesLocation";
//    @ConfigurationParameter(name = DICTIONARIES_LOCATION, mandatory = false)
//    private String dictionariesLocation;
//
    private static String lastSeenDocumentId = "";

    private static Map<Token, Collection<DictionaryFeatureAnnotation>> dictionaryMap = null;

    private static Logger logger = Logger.getLogger(DictionariesExtractor.class);
    private static Map<String, Set<String>> dictionaries = new HashMap<>();

    static {

        for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {
            walk(language.name());
        }
    }

    private static void walk(String language) {
        try {
            dictionaries.put(language, Files.walk(Paths.get(KnowNERSettings.getLangResourcesPath(language) + "gazeteers/"), 1)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Feature> extract(JCas jcas, TextClassificationTarget unit) throws TextClassificationException {
        if (!isTheSameDocument(jcas)) {
            logger.trace("Building index of covering dictionaries annotations...");
            dictionaryMap = JCasUtil.indexCovering(jcas, Token.class, DictionaryFeatureAnnotation.class);
        }

        Token token = JCasUtil.selectCovered(jcas, Token.class, unit).iterator().next();

        Set<String> dictionaryFeatureAnnotations = dictionaryMap.get(token)
                .stream()
                .map(DictionaryFeatureAnnotation::getDictionary)
                .collect(Collectors.toSet());

        return dictionaries.get(jcas.getDocumentLanguage())
                .stream()
                .map(d -> new Feature(d, dictionaryFeatureAnnotations.contains(d)))
                .collect(Collectors.toSet());
    }

    private static boolean isTheSameDocument(JCas jcas) {
        DocumentMetaData meta = JCasUtil.selectSingle(jcas,
                DocumentMetaData.class);
        String currentId = meta.getDocumentTitle();

        if (currentId == null) return false;

        boolean isSame = currentId.equals(lastSeenDocumentId);
        logger.trace(lastSeenDocumentId + ", " + currentId);
        lastSeenDocumentId = currentId;
        return isSame;
    }

}
