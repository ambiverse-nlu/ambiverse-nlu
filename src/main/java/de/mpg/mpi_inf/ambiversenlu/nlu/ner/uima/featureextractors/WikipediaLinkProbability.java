package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERWikipediaProbabilitiesChecker;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.Constants;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.WikipediaLinkProbabilities;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.LocalFeaturesTcAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *         <p>
 *         Extracts the Wikipedia link probabilities for the current word.
 *         <p>
 *         REQUIREMENTS: See WikipediaLinkProbabilities
 */
public class WikipediaLinkProbability extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

    public static final String FEATURE_NAME = "WikipediaLinkProbability";

    private static Map<String, WikipediaLinkProbabilities> probs = new HashMap<>();

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
        boolean initflag = super.initialize(aSpecifier, aAdditionalParams);

        try {
            String l = (String) aAdditionalParams.get(LocalFeaturesTcAnnotator.PARAM_LANGUAGE);
            File probFile = new File(
                    KnowNERSettings.getLangResourcesPath(l) +
                            KnowNERSettings.getDumpName() + "_" +
                            KnowNERWikipediaProbabilitiesChecker.FILE_SUFFIX);
            WikipediaLinkProbabilities wlp = new WikipediaLinkProbabilities(probFile);
            probs.put(l, wlp);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        return initflag;
    }

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit) {
        String current;
        if (KnowNERLanguage.requiresLemma(view.getDocumentLanguage())) {
            current = JCasUtil.selectCovered(view, Lemma.class, classificationUnit).get(0).getValue();
        } else {
            current = classificationUnit.getCoveredText();
        }
        Double prob = probs.get(view.getDocumentLanguage())
                .getProbability(current.replaceAll(Constants.MENTION_LINKLIKELIHOOD_CLEAN_PATTERN, ""));

        return new Feature(FEATURE_NAME, prob).asSet();
    }

}
