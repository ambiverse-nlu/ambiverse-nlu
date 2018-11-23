package de.mpg.mpi_inf.ambiversenlu.nlu.drivers.sample;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.CollectionProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Collection;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.apache.uima.UIMAException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class OpenIE {
  public static void main(String args[])
      throws InterruptedException, ExecutionException, NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException,
      UIMAException, EntityLinkingDataAccessException, UnprocessableDocumentException {
    EntityLinkingManager.init();
    CollectionProcessor cp = new CollectionProcessor(10, PipelineType.OPENIE_EN);
    Collection.Builder builder = new Collection.Builder();
    builder.withPath("TEST");
    builder.withCollectionType(Reader.COLLECTION_FORMAT.TEXT);
    builder.withLanguage(Language.getLanguageForString("en"));
    List<ProcessedDocument> pds =  cp.process(builder.build(), ProcessedDocument.class);
    for(ProcessedDocument pd: pds) {
      OutputUtils.generateAnalyzeOutputfromProcessedDocument(pd);
    }
  }
}
