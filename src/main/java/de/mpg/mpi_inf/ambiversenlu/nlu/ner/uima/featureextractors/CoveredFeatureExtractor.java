package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.window.CoveredTextExtractor;
import org.dkpro.tc.features.window.WindowFeatureExtractor;

import java.util.Map;
import java.util.Set;

public class CoveredFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements FeatureExtractor {

    private WindowFeatureExtractor<?> extractors[] = new WindowFeatureExtractor<?>[2];

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
        boolean initialize = super.initialize(aSpecifier, aAdditionalParams);

        if (initialize) {
            extractors[0] = new CoveredLemmaExtractor();
            initialize = extractors[0].initialize(aSpecifier, aAdditionalParams);
        }
        if (initialize) {
            extractors[1] = new CoveredTextExtractor();
            initialize = extractors[1].initialize(aSpecifier, aAdditionalParams);
        }

        return initialize;
    }

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit) throws TextClassificationException {
        if (KnowNERLanguage.requiresLemma(view.getDocumentLanguage())) {
            return extractors[0].extract(view, classificationUnit);
        }
        return extractors[1].extract(view, classificationUnit);
    }

    public static class CoveredLemmaExtractor extends WindowFeatureExtractor<Lemma> {
        @Override
        protected Class<Lemma> getTargetType() {
            return Lemma.class;
        }

        @Override
        protected String getFeatureName() {
            return "Lemma";
        }

        @Override
        protected String getFeatureValue(Lemma a) {
            return a.getValue();
        }
    }
}
