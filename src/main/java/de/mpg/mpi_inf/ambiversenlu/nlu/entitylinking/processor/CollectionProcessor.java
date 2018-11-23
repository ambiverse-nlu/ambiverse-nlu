package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.extensions.SimplePipelineCasPoolIterator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Collection;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import static org.apache.uima.fit.util.JCasUtil.exists;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

public class CollectionProcessor {

  private Logger logger = LoggerFactory.getLogger(CollectionProcessor.class);

  private PipelineType type;

  private int threadCount = 1;

  private EvaluationSettings evsettings;

  private CollectionSettings csettings;

  public CollectionProcessor(int threadCount, PipelineType type)
      throws NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException, InvalidXMLException,
      ResourceInitializationException {
    this(threadCount, type, null, null);
  }

  public CollectionProcessor(int threadCount, PipelineType type, CollectionSettings csettings)
      throws NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException, InvalidXMLException,
      ResourceInitializationException {
    this(threadCount, type, null, csettings);
  }

  public CollectionProcessor(int threadCount, PipelineType type, EvaluationSettings evsettings, CollectionSettings csettings)
      throws NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException, InvalidXMLException,
      ResourceInitializationException {
    if( type.isEvaluation() ) {
      if (evsettings == null) {
        throw new IllegalArgumentException("Evaluation settings should be provided");
      }
      RunningTimer.enableRealTimeTracker();
    }
    if (csettings == null) {
      this.csettings = new CollectionSettings();
    } else {
      this.csettings = csettings;
    }

    this.type = type;
    this.threadCount = threadCount;
    this.evsettings = evsettings;
  }

  public<K> List<K> process(Collection collection, Class<K> typeToReturn)
      throws NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException, UIMAException, InterruptedException,
      ExecutionException, UnprocessableDocumentException, EntityLinkingDataAccessException {
    boolean get = true;
    if(typeToReturn.equals(ProcessedDocument.class)) {
      get = true;
    } else if(typeToReturn.equals(Void.class)) {
      get = false;
    } else {
      throw new IllegalArgumentException("A collection processor returns either a ProcessedDocument or Void");
    }
    ExecutorService es = Executors.newFixedThreadPool(threadCount);;
    Object[] params = buildParameters(collection);
    CollectionReaderDescription readerDescription = Reader.getCollectionReaderDescription(collection.getCollectionType(), params);
    List<Future<ProcessedDocument>> result = new ArrayList<>();
    List<ProcessedDocument> resultFinal = new ArrayList<>();
    type.dummyCall(collection);
    int count = 0;
    for (JCas jCas : SimplePipelineCasPoolIterator.iteratePipeline(threadCount, readerDescription)) {
      count++;
      if (jCas.getDocumentText() == null ||
          jCas.getDocumentText().isEmpty() ||
          collection.getDocStart() != null && count < collection.getDocStart()) {
        jCas.release();
        continue;
      } else if (collection.getDocEnd() != null && count >= collection.getDocEnd()) {
        jCas.release();
        break;
      }
      //System.out.println(jCas.getDocumentText());
      if(count % 100 == 0) {
        logger.info(count + " documents read");
      }
      Document doc = collection.clone();
      DocumentMetaData md;
      if (exists(jCas, DocumentMetaData.class)) {
        md = selectSingle(jCas, DocumentMetaData.class);
      } else {
        md = new DocumentMetaData(jCas);
        md.addToIndexes();
      }
      if(md.getDocumentId() ==null || md.getDocumentId().isEmpty()) {
        md.setDocumentId(Integer.toString(Integer.parseInt(collection.getDocumentId()) + count));
      }
      doc.setDocumentId(md.getDocumentId());
      doc.addSettingstoJcas(jCas);
      DocumentProcessor.CallableProcessor call = null;
      call = new DocumentProcessor.CallableProcessor(doc, jCas, type, evsettings);
      if(get) {
        result.add(es.submit(call));
      } else {
        es.submit(call);
      }
    }
    es.shutdown();
    es.awaitTermination(1, TimeUnit.DAYS);
    for (Future<ProcessedDocument> r : result) {
      logger.info("add to results " + r.get().getDocId());
      resultFinal.add(r.get());
    }
    if(get) {
      return (List<K>) resultFinal;
    } else {
      return null;
    }
  }
  
  private Object[] buildParameters(Collection collection) {
    List<Object> params = new ArrayList();
    params.add(ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION);
    params.add(collection.getPath());
    if (collection.getLanguage() != null) {
      params.add(ResourceCollectionReaderBase.PARAM_LANGUAGE);
      params.add(collection.getLanguage().toString());
    }

    if (evsettings != null) {
      params.add("evaluateConcepts");
      params.add(evsettings.isEvaluateConcepts());
      
      params.add("evaluateNE");
      params.add(evsettings.isEvaluateNE());
      
      params.add("evaluateRecognition");
      params.add(evsettings.isEvaluateRecognition());
      
      params.add("pipelineType");
      params.add(evsettings.getPipelineType());
      
      params.add("addMentionToBoth");
      params.add(evsettings.getAddMentionToBoth());
      
      params.add("trainingSimilarityWeights");
      params.add(evsettings.isTrainingSimilarityWeights());
    }
    
    params.add("isOneFile");
    params.add(csettings.isOneFile());
    if (collection.getCollectionType().equals(Reader.COLLECTION_FORMAT.XML_WP)) {
      params.add("IncludeTag");
      params.add(new String[] { "title", "content:encoded" });
    }
    if(collection.additionalParam != null) {
      params.addAll(Arrays.asList(collection.additionalParam));
    }
    
    
    return params.toArray(new Object[params.size()]);
  }

  public static Comparator<Future<ProcessedDocument>> FPDComprator = new Comparator<Future<ProcessedDocument>>() {
    @Override
    public int compare(Future<ProcessedDocument> o1, Future<ProcessedDocument> o2) {
      if (o1.isDone() && !o2.isDone()) {
        return -1;
      }
      else if (!o1.isDone() && o2.isDone()) {
        return 1;
      }
      return 0;
    }
  }; 
  
}



