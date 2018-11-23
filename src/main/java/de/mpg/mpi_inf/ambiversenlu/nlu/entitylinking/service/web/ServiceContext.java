package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.impl.AnalyzeResourceImpl;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Options;
import org.apache.uima.UIMAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.net.URISyntaxException;

public class ServiceContext implements ServletContextListener {

  Logger logger_ = LoggerFactory.getLogger(ServiceContext.class);

  @Override public void contextDestroyed(ServletContextEvent arg0) {
    try {
      logger_.info("Shutting down the Entity Linking Manager.");
      EntityLinkingManager.shutDown();
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  @Override public void contextInitialized(ServletContextEvent arg0) {
    try {
      logger_.info("Initializing the Entity Linking Manager");
      EntityLinkingManager.init();
      //This is for VW to avoid generating facts with appositions
      try {
        Options options = new Options();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      Options.processAppositions = false;

      logger_.info("Firing dummy calls to load the caches.");
      AnalyzeResourceImpl.getSaliencePipelineByNerConfig().dummyCall();
      AnalyzeResourceImpl.getConceptSaliencePipelineByNerConfig().dummyCall();
    } catch (EntityLinkingDataAccessException e) {
      throw new RuntimeException(e);
    } catch (MissingSettingException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (UnprocessableDocumentException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (UIMAException e) {
      throw new RuntimeException(e);
    }
  }
}