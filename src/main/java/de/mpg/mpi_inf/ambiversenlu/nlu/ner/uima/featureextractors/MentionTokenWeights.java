/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.MentionTokenWeightsFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.LocalFeaturesTcAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *         <p>
 *         Extracts the weights that each token has according to it's
 *         frequency in the Yago mentions
 *         <p>
 *         REQUIREMENTS: You need to set the PARAM_TOP_K parameter, which
 *         specifies how many tokens are considered from the top list
 *         ranked by frequency. The default is -1, which means that the
 *         whole list is considered.
 *         See MentionTokenFrequencyCounts for more
 *         information on how to set up the required data.
 */
public class MentionTokenWeights extends FeatureExtractorResource_ImplBase
        implements FeatureExtractor {
    public static final String FEATURE_NAME = "MentionTokenWeights";
    
    private Map<String, Map<String, Double>> languageMentionWeights = new HashMap<>();

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
        boolean initFlag = super.initialize(aSpecifier, aAdditionalParams);

        try {
            String language = (String) aAdditionalParams.get(LocalFeaturesTcAnnotator.PARAM_LANGUAGE);
            languageMentionWeights.put(language, MentionTokenWeightsFactory.getMentionWeightsForLanguage(language));
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        return initFlag;
    }

    @Override
    public Set<Feature> extract(JCas jCas, TextClassificationTarget classificationUnit) throws TextClassificationException {

        Map<String, Double> mentionWeights = languageMentionWeights.get(jCas.getDocumentLanguage());

        String current = KnowNERLanguage.requiresLemma(jCas.getDocumentLanguage()) ?
                JCasUtil.selectCovered(jCas, Lemma.class, classificationUnit).get(0).getValue() :
                classificationUnit.getCoveredText();
        current = EntityLinkingManager.conflateToken(current, Language.getLanguageForString(jCas.getDocumentLanguage()), true);

        Double value = 0.0;

        if (mentionWeights.containsKey(current)) {
            value = mentionWeights.get(current);
        }

        return new Feature(FEATURE_NAME, value).asSet();
    }
}