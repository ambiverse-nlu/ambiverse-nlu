package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasFlowController_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.flow.*;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.HashMap;
import java.util.Map;

public class DocumentProcessorFlowController extends JCasFlowController_ImplBase {

  public static final String PARAM_STEPS = "steps";

  @ConfigurationParameter(name = PARAM_STEPS, mandatory = true) private String steps;

  private Map<String, String> nextEngine;

  @Override public Flow computeFlow(JCas jCas) throws AnalysisEngineProcessException {
    return new DocumentProcessorFlow(jCas);
  }

  @Override public void initialize(FlowControllerContext context) throws ResourceInitializationException {
    super.initialize(context);
    nextEngine = new HashMap<>();
    String[] pairs = steps.split(",");
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("Steps must come in pairs");
    }
    for (int i = 0; i < pairs.length; i = i + 2) {
      nextEngine.put(pairs[i], pairs[i + 1]);
    }
  }

  public class DocumentProcessorFlow extends JCasFlow_ImplBase {

    private String lastStep = "first";

    private JCas jCas;

    private boolean knowsLanguage = false;

    public DocumentProcessorFlow(JCas jCas) {
      this.jCas = jCas;
    }

    @Override public Step next() throws AnalysisEngineProcessException {
      if (!knowsLanguage && jCas.getDocumentLanguage() != null && !jCas.getDocumentLanguage().equals("x-unspecified")) {
        try {
          Language.getLanguageForString(jCas.getDocumentLanguage());
        } catch (AidaUnsupportedLanguageException e) {
          throw new AnalysisEngineProcessException(e);
        }
        lastStep = jCas.getDocumentLanguage().toUpperCase();
        knowsLanguage = true;
      }

      String nextStep = nextEngine.get(lastStep);
      if (nextStep == null) {
        return new FinalStep();
      } else {
        lastStep = nextStep;
        return new SimpleStep(nextStep);
      }
    }
  }
}