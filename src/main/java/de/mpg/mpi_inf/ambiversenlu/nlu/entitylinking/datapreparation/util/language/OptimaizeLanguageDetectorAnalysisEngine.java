package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.language;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;


public class OptimaizeLanguageDetectorAnalysisEngine extends JCasAnnotator_ImplBase {

  private static OptimaizeLanguageDetector detector = new OptimaizeLanguageDetector();

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    Language language;
    try {
      language = detector.detectLanguage(jCas.getDocumentText());
    } catch (AidaUnsupportedLanguageException e) {
      throw new AnalysisEngineProcessException(e);
    }
    jCas.setDocumentLanguage(language.toString());
  }

}
