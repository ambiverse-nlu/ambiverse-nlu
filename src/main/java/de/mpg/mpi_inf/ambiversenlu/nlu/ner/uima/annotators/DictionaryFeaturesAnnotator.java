package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.DictionaryFeatureAnnotation;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Annotates a JCas object with annotations of type {@link DictionaryFeatureAnnotation}.
 * Each dictionary engine annotates with parameter value equal name of the dictionary file.
 */
public class DictionaryFeaturesAnnotator extends JCasAnnotator_ImplBase {
    private static Logger logger = Logger.getLogger(DictionaryFeaturesAnnotator.class);

    private static final Map<String, Set<AnalysisEngine>> analysisEngines = new HashMap<>();

    public static final String PARAM_LANGUAGE = "language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        initForLanguage(language);
    }

    private void initForLanguage(String language) {
        String dictionariesLocation = KnowNERSettings.getLangResourcesPath(language) + "gazeteers/";
        ResourceManager mgr = ((UimaContextAdmin) getContext()).getResourceManager();

        try {
            List<Path> failedDictionaries = new ArrayList<>();
            analysisEngines.put(language, Files.walk(Paths.get(dictionariesLocation), 1)
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            AnalysisEngineDescription desc = createEngineDescription(DictionaryAnnotator.class,
                                    DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryFeatureAnnotation.class,
                                    DictionaryAnnotator.PARAM_MODEL_LOCATION, p.toString(),
                                    DictionaryAnnotator.PARAM_VALUE_FEATURE, "dictionary",
                                    DictionaryAnnotator.PARAM_VALUE, p.getFileName().toString());
                            return UIMAFramework.produceAnalysisEngine(desc, mgr, null);
                        } catch (ResourceInitializationException e) {
                            logger.error(e);
                            failedDictionaries.add(p);
                            return null;
                        }
                    })
                    .collect(Collectors.toSet()));
            if (!failedDictionaries.isEmpty()) {
                throw new RuntimeException("Some of the dictionary annotators were not created: " + failedDictionaries);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.trace("Starting new dictionary annotations...");
        for (AnalysisEngine ae : analysisEngines.get(aJCas.getDocumentLanguage())) {
            ae.process(aJCas);
        }

    }
}
