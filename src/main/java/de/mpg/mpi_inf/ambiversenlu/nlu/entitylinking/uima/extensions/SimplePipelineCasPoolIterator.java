package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.extensions;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

public class SimplePipelineCasPoolIterator {

  public static JCasPoolIterable iteratePipeline(int casPoolSize, CollectionReaderDescription aReader, AnalysisEngineDescription... aEngines)
      throws ResourceInitializationException, CASException {
    return new JCasPoolIterable(casPoolSize, aReader, aEngines);
  }
}
