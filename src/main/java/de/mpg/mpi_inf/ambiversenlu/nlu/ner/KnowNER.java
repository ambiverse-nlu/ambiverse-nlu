package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

//import mpi.ner.evaluation.TcAnnotator;


@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkopro.core.api.lexmorph.type.pos.POS"
        },
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity"})
public class KnowNER extends JCasAnnotator_ImplBase {

    private static final Logger logger = LoggerFactory.getLogger(KnowNER.class);

    private final Map<KnowNERLanguage, AnalysisEngine> classifierMap = new HashMap<>();
    private Properties trainedModelsMapping;


    private static final String PARAM_MODEL = "model";
    @ConfigurationParameter(name = PARAM_MODEL, mandatory = true)
    private String model;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        logger.info("Initializing KnowNER.");

        for (KnowNERLanguage language : KnowNERLanguage.activeLanguages()) {
            try {
                classifierMap.put(language, initializeClassifier(language.name()));
            } catch (Exception e) {
                logger.error("Could not initialize KnowNER classifierMap({}) for {} language", model, language);
                throw new ResourceInitializationException(e);
            }
        }
    }

    private AnalysisEngine initializeClassifier(String language) throws ResourceInitializationException, SQLException {
        logger.debug("Initializing KnowNER for '" + language + "'.");

        long start = System.currentTimeMillis();

        List<AnalysisEngineDescription> descriptions = new ArrayList<>();

        if (model.equals("NED")) {
            descriptions.add(AnalysisEngineFactory.createEngineDescription(BmeowTypeAnnotator.class,
                    BmeowTypeAnnotator.GOLD, false));
            descriptions.add(AnalysisEngineFactory.createEngineDescription(RemoveNamedEntityAnnotator.class));
        }
        descriptions.add(AnalysisEngineFactory.createEngineDescription(DictionaryMatchAnnotator.class,
                DictionaryMatchAnnotator.PARAM_LANGUAGE, language));
        descriptions.add(AnalysisEngineFactory.createEngineDescription(DictionaryFeaturesAnnotator.class,
                DictionaryFeaturesAnnotator.PARAM_LANGUAGE, language));

        descriptions.add(createEngineDescription(LocalFeaturesTcAnnotator.class,
					TcAnnotator.PARAM_TC_MODEL_LOCATION, KnowNERSettings.getModelPath(language, model).toFile(),
					LocalFeaturesTcAnnotator.PARAM_LANGUAGE, language,
					TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
					TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName()));

        descriptions.add(createEngineDescription(KnowNERNamedEntityPostClassificationBMEOWAnnotator.class));

        AnalysisEngineDescription[] analysisEngineDescriptions = new AnalysisEngineDescription[descriptions.size()];
        for (int i = 0; i < descriptions.size(); i++) {
            analysisEngineDescriptions[i] = descriptions.get(i);
        }

        ResourceManager mgr = ((UimaContextAdmin) getContext()).getResourceManager();

        AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(
                createEngineDescription(analysisEngineDescriptions), mgr, null);

        long dur = System.currentTimeMillis() - start;
        logger.info("Initialized KnowNER-" + language + " in " + dur/1000 + "s.");

        return ae;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        KnowNERLanguage language = KnowNERLanguage.valueOf(aJCas.getDocumentLanguage());
        classifierMap.get(language).process(aJCas);
    }
}
