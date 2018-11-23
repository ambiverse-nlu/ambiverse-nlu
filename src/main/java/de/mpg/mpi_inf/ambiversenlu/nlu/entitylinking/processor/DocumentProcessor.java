package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EvaluationEntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationCounts;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.PipelinesHolder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.textmanipulation.Whitespace;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.CasManager;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.InvalidXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.apache.uima.fit.util.JCasUtil.selectSingle;

public class DocumentProcessor {

  private static Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);

  private static CasManager[] casManager = new CasManager[PipelineType.values().length];

  private static DocumentProcessor[] instances = new DocumentProcessor[PipelineType.values().length];

  private PipelineType type;

  private DocumentProcessor(PipelineType type)
      throws EntityLinkingDataAccessException, ResourceInitializationException, IOException, InvalidXMLException, NoSuchMethodException,
      MissingSettingException, ClassNotFoundException {
    this.type = type;
    if (casManager[type.ordinal()] == null) {
      AnalysisEngine ae = PipelinesHolder.getAnalyisEngine(type);
      ResourceManager rm = ResourceManagerFactory.newResourceManager();
      rm.getCasManager().addMetaData(ae.getProcessingResourceMetaData());
      casManager[type.ordinal()] = rm.getCasManager();
      casManager[type.ordinal()]
          .defineCasPool(type.toString(), Integer.parseInt(EntityLinkingConfig.get(EntityLinkingConfig.DOCUMENT_PARALLEL_COUNT)), null);
    }
  }

  public ProcessedDocument process(Document d)
      throws UIMAException, IOException, ClassNotFoundException, NoSuchMethodException, MissingSettingException, EntityLinkingDataAccessException,
      UnprocessableDocumentException {
    CAS cas = casManager[type.ordinal()].getCas(type.toString());
    ProcessedDocument result;
    JCas jcas = cas.getJCas();
    d.addSettingstoJcas(jcas);
    try {
      jcas.setDocumentText(d.getText());
      result = process(jcas);
    } catch (Exception e) {
      throw new UnprocessableDocumentException(e.getCause() != null? e.getCause().getMessage() : e.getMessage());
    } finally {
      jcas.release();
    }
    return result;
  }

  public JCas processDev(Document d)
      throws UIMAException, IOException, ClassNotFoundException, NoSuchMethodException, MissingSettingException, EntityLinkingDataAccessException,
      UnprocessableDocumentException {
    JCas jcas = JCasFactory.createJCas();
    d.addSettingstoJcas(jcas);
    jcas.setDocumentText(d.getText());
    process(jcas);
    return jcas;
  }

  private ProcessedDocument process(JCas jcas)
      throws UIMAException, IOException, ClassNotFoundException, EntityLinkingDataAccessException, MissingSettingException, NoSuchMethodException,
      UnprocessableDocumentException {
    if (!canProcess(jcas.getDocumentText())) {
      AidaDocumentSettings md = selectSingle(jcas, AidaDocumentSettings.class);
      throw new UnprocessableDocumentException(
          "Document " + md.getDocumentId() + " cannot be processed"); //Add id reference check what to do with collections
    }
    AnalysisEngine ae = PipelinesHolder.getAnalyisEngine(type);
    ae.process(jcas);
    ProcessedDocument pd = OutputUtils.generateProcessedPutputfromJCas(jcas);
    return pd;
  }

  private boolean canProcess(String documentText) {
    return !Whitespace.isWhiteSpace(documentText);
  }

  public static DocumentProcessor getInstance(PipelineType type)
      throws EntityLinkingDataAccessException, ResourceInitializationException, IOException, InvalidXMLException, NoSuchMethodException,
      MissingSettingException, ClassNotFoundException {
    synchronized (instances) {
      if (instances[type.ordinal()] == null) {
        logger.info("Initializing DocumentProcessor for type '" + type + "'.");
        instances[type.ordinal()] = new DocumentProcessor(type);
      }
    }
    return instances[type.ordinal()];
  }

  public static class CallableProcessor implements Callable<ProcessedDocument> {

    Document ds;

    PipelineType type;

    JCas jCas;

    EvaluationSettings es;

    public CallableProcessor(Document ds, PipelineType type) {
      this(ds, null, type, null);
    }

    public CallableProcessor(Document ds, JCas jCas, PipelineType type) {
      this(ds, jCas, type, null);
    }

    public CallableProcessor(Document ds, JCas jCas, PipelineType type, EvaluationSettings es) {
      super();
      if (type.isEvaluation() && es == null) {
        throw new IllegalStateException("Evaluation settings must be provided to proceed with evaluation");
      }
      this.es = es;
      this.ds = ds;
      this.jCas = jCas;
      this.type = type;
    }

    @Override public ProcessedDocument call() {
      return processDocument();
    }

    private ProcessedDocument processDocument() {
      try {
        DocumentProcessor dp = getInstance(type);
        if (jCas == null) {
          return dp.process(ds);
        } else {
          Integer id = RunningTimer.recordStartTime("DOC");
          ProcessedDocument result = dp.process(jCas);
          RunningTimer.recordEndTime("DOC", id);
          if ( type.isEvaluation() ) {
            Map<String, EvaluationCounts> temp = Utils.computeDisambiguationEvaluationfromJCas(jCas, es, EvaluationEntityType.ALL);
            result.setEvaluationCountsDisambiguation(temp.get("ec"));
            if (es.isEvaluateConcepts()) {
              temp = Utils.computeDisambiguationEvaluationfromJCas(jCas, es, EvaluationEntityType.CONCEPT);
              result.setEvaluationCountsConceptsDisambiguation(temp.get("ec"));
              result.setEvaluationCountsConceptsDisambiguationFP(temp.get("ecFP"));
            }
            if (es.isEvaluateNE()) {
              temp = Utils.computeDisambiguationEvaluationfromJCas(jCas, es, EvaluationEntityType.NAMED_ENTITY);
              result.setEvaluationCountsNamedEntitiesDisambiguation(temp.get("ec"));
            }
            temp = Utils.computeDisambiguationEvaluationfromJCas(jCas, es, EvaluationEntityType.UNKNOWN);
            result.setEvaluationCountsUnknownsDisambiaguation(temp.get("ec"));

            if (es.isEvaluateRecognition()) {
              temp = Utils.computeNEREvaluationfromJCas(jCas, es, EvaluationEntityType.ALL);
              result.setEvaluationCountsNER(temp.get("ec"));

              if (es.isEvaluateConcepts()) {
                temp = Utils.computeNEREvaluationfromJCas(jCas, es, EvaluationEntityType.CONCEPT);
                result.setEvaluationCountsNERConcepts(temp.get("ec"));
              }
              if (es.isEvaluateNE()) {
                temp = Utils.computeNEREvaluationfromJCas(jCas, es, EvaluationEntityType.NAMED_ENTITY);
                result.setEvaluationCountsNERNamedEntities(temp.get("ec"));
              }
              temp = Utils.computeNEREvaluationfromJCas(jCas, es, EvaluationEntityType.UNKNOWN);
              result.setEvaluationCountsNERUnknowns(temp.get("ec"));
            }
          }
          return result;
        }
      } catch (EntityLinkingDataAccessException | IOException | NoSuchMethodException | MissingSettingException | ClassNotFoundException | UIMAException | UnprocessableDocumentException e) {
        e.printStackTrace();
        logger.info("Failed document: " + ds.getDocumentId());
        throw new RuntimeException(e);
      } finally {
        if (jCas != null) {
          jCas.release(); //It is imnportant that this happens here, this method assumes that the cas comes from some poll. To use without a poll for testing or dev purposes use  processDev
        }
      }
    }

  }

  public static void main(String[] args) throws Exception {
    DocumentProcessor processor = new DocumentProcessor(PipelineType.DISAMBIGUATION);

    Document.Builder builder = new Document.Builder();

    builder.withText("Messi and Maradona played soccer in Barcelona. Maradona also played in Napoli.");
    ProcessedDocument ao = processor.process(builder.build());
    String resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(OutputUtils.generateAnalyzeOutputfromProcessedDocument(ao));
    System.out.println(resultString);

    builder.withText("梅西和马拉多纳在巴萨踢足球。马拉多纳在那不勒斯也发挥。");
    ao = processor.process(builder.build());
    resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(OutputUtils.generateAnalyzeOutputfromProcessedDocument(ao));
    System.out.println(resultString);

    builder.withText("Messi juega al futbol en Barcelona. Maradona tambien jugo en Barcelona y luego fue al Napoli.");
    ao = processor.process(builder.build());
    resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(OutputUtils.generateAnalyzeOutputfromProcessedDocument(ao));
    System.out.println(resultString);

    builder.withText("Lionel Messi will nicht mehr für die [[argentinische Fußball-Nationalmannschaft]] spielen.");
    ao = processor.process(builder.build());
    resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(OutputUtils.generateAnalyzeOutputfromProcessedDocument(ao));
    System.out.println(resultString);

    builder.withText("Gucci and Chanel were the main fashion brands at the Oscars.");
    ao = processor.process(builder.build());
    resultString = OutputUtils.generateJSONStringfromAnalyzeOutput(OutputUtils.generateAnalyzeOutputfromProcessedDocument(ao));
    System.out.println(resultString);
  }
}