package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.impl;

import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.JsonLogger;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.MessageResponse;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.AnalyzeResourceWithFacts;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.utils.AnalyzeInputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * Main Web Service class for the Entity Linking API.
 */
public class AnalyzeResourceWithFactsImpl implements AnalyzeResourceWithFacts {

  @Context HttpServletRequest req;

  static Logger requestLogger;

  static org.slf4j.Logger defaultLogger = org.slf4j.LoggerFactory.getLogger(AnalyzeResourceWithFactsImpl.class);

  private static int errorCounter = 0;

  public AnalyzeResourceWithFactsImpl() {

    //Formatting the date to RFC3339 for Filebeat to use the timestamp from here instead of when the file was read
    LoggerFactory.setDateFormatString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    requestLogger = LoggerFactory.getLogger("requestLogger");
  }

  @Override public PostAnalyzeResponse postAnalyze(AnalyzeInput input) {

    JsonLogger logger;
    String user = null;
    String ip = null;

    if (req != null) {
      user = req.getHeader("X-3scale-App-Id");
      if (getCallerIp(req) != null) {
        ip = getCallerIp(req);
      }
    }

    try {
      // Log request.
      logger = requestLogger.info().field("request", input);
      if (user != null) {
        logger.field("user", user);
      }

      if (ip != null) {
        logger.field("IP", ip);
      }

      long time = System.currentTimeMillis();
      // Process the input document with the document processor.

      if(input.getLanguage()==null) {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Please provide language on input");
        return PostAnalyzeResponse.withJsonInternalServerError(messageResponse);
      }
      PipelineType pipelineType;
      switch (input.getLanguage()) {
        case "en":
          if (input.getExtractConcepts() != null && input.getExtractConcepts()) {
            pipelineType = getConceptSaliencePipelineByNerConfig();
          } else {
            pipelineType = getSaliencePipelineByNerConfig();
          }

          break;
        default:
          MessageResponse messageResponse = new MessageResponse();
          messageResponse.setMessage("Not used for now, the Fact Extraction Service only supports English.");
          return PostAnalyzeResponse.withJsonInternalServerError(messageResponse);
      }


      Document doc = AnalyzeInputUtils.getDocumentfromAnalyzeInput(input);
      doc.setLanguage(Language.getLanguageForString(input.getLanguage()));
      DocumentProcessor dp = DocumentProcessor.getInstance(pipelineType); //Change the pipeline here!
      AnalyzeOutput elo = OutputUtils.generateAnalyzeOutputfromProcessedDocument(dp.process(doc));

      long duration = System.currentTimeMillis() - time;

      //Logging some metadata for the request
      logger.field("duration", duration);

      logger.field("response", elo);
      logger.log();

      return PostAnalyzeResponse.withJsonOK(elo);
    } catch (UnprocessableDocumentException e) {
      // For exceptions that occur in the analysis engine, we need to return the root cause.
      e.printStackTrace();
      MessageResponse messageResponse = new MessageResponse();
      messageResponse.setMessage(e.getMessage());

      defaultLogger.error("(" + (++errorCounter) + ") " + input);
      defaultLogger.error("ERROR MESSAGE: " + e.getMessage());

      logger = requestLogger.error().field("request", input);
      if (user != null) {
        logger.field("user", user);
      }

      if (ip != null) {
        logger.field("IP", ip);
      }
      logger.field("error", messageResponse);
      logger.log();

      return PostAnalyzeResponse.withJsonInternalServerError(messageResponse);

    } catch (AnalysisEngineProcessException e) {
      // For exceptions that occur in the analysis engine, we need to return the root cause.
      MessageResponse messageResponse = new MessageResponse();
      messageResponse.setMessage(e.getCause().getMessage());

      defaultLogger.error("(" + (++errorCounter) + ") " + input);
      defaultLogger.error("ERROR MESSAGE: " + e.getCause().getMessage());

      logger = requestLogger.error().field("request", input);
      if (user != null) {
        logger.field("user", user);
      }

      if (ip != null) {
        logger.field("IP", ip);
      }
      logger.field("error", messageResponse);
      logger.log();

      return PostAnalyzeResponse.withJsonInternalServerError(messageResponse);
    } catch (Exception e) {
      e.printStackTrace();
      // The default exception handler.
      MessageResponse messageResponse = new MessageResponse();
      messageResponse.setMessage(e.getMessage());

      defaultLogger.error("(" + (++errorCounter) + ") " + input);
      defaultLogger.error("ERROR MESSAGE: " + e.getMessage());

      logger = requestLogger.error().field("request", input);
      if (user != null) {
        logger.field("user", user);
      }

      if (ip != null) {
        logger.field("IP", ip);
      }
      logger.field("error", messageResponse);
      logger.log();

      return PostAnalyzeResponse.withJsonInternalServerError(messageResponse);
    }
  }

  public static PipelineType getSaliencePipelineByNerConfig() {
    if (EntityLinkingConfig.get(EntityLinkingConfig.WEBSERVICE_NER).equals("knowner")) {
      return PipelineType.FACTS_WITH_SALIENCE_EN;
    } else {
      return PipelineType.FACTS_WITH_SALIENCE_EN_STANFORD;
    }
  }

  public static PipelineType getConceptSaliencePipelineByNerConfig() {
    if (EntityLinkingConfig.get(EntityLinkingConfig.WEBSERVICE_NER).equals("knowner")) {
      return PipelineType.FACT_ENTITY_CONCEPT_SALIENCE;
    } else {
      return PipelineType.FACT_ENTITY_CONCEPT_SALIENCE_STANFORD;
    }
  }


  private static String getCallerIp(HttpServletRequest req) {
    String ip = req.getRemoteAddr();
    // Make sure to get the actual IP of the requester if
    // the service works behind a gateway.
    String forward = req.getHeader("X-Forwarded-For");
    if (forward != null) {
      ip = forward;
    }
    return ip;
  }

}