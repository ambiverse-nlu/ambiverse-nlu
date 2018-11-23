package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public class LanguageAnnotator extends JCasAnnotator_ImplBase {

    public static final String LANGUAGE = "language";
    @ConfigurationParameter(name = LANGUAGE, mandatory = true)
    private String language;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        aJCas.setDocumentLanguage(language);
    }
}
