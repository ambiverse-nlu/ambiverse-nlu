/*
 *  Copyright 2013 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.extensions.SimplePipelineCasPoolIterator;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SparkUimaUtils {

  private static Logger logger = LoggerFactory.getLogger(SparkUimaUtils.class);

  public static void createSequenceFile(Object[] params, String uri)
      throws URISyntaxException, IOException, UIMAException, NoSuchMethodException, MissingSettingException, ClassNotFoundException {
    Configuration conf = new Configuration();
    Path path = new Path(uri);
    Writer writer =
        SequenceFile.createWriter(
            conf, Writer.file(path),
            Writer.keyClass(Text.class),
            Writer.valueClass(SCAS.class));

    int count = 0;

    CollectionReaderDescription readerDescription = Reader.getCollectionReaderDescription(Reader.COLLECTION_FORMAT.NYT, params);
    for (JCas jCas : SimplePipelineCasPoolIterator.iteratePipeline(20, readerDescription)) {
        if(JCasUtil.exists(jCas, DocumentMetaData.class)) {
          ++count;
          // Get the ID.
          DocumentMetaData dmd = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
          String docId = "NULL";
          if (dmd != null) {
            docId = dmd.getDocumentId();
          } else {
            throw new IOException("No Document ID for xml: " + jCas.getView("xml").getDocumentText());
          }
          Text docIdText = new Text(docId);
          SCAS scas = new SCAS(jCas.getCas());
          writer.append(docIdText, scas);
        }
        jCas.release();
    }
    logger.info("Wrote " + count + " documents to " + uri);
    IOUtils.closeStream(writer);
  }

 public static List<SCAS> readFrom(CollectionReader reader) throws IOException, UIMAException {
   List<SCAS> scasList = new ArrayList<>();
   while (reader.hasNext()) {
     JCas jcas = JCasFactory.createJCas();
     CAS cas = jcas.getCas();
     reader.getNext(cas);
     scasList.add(new SCAS(cas));
   }
   return scasList;
 }

  public static JavaRDD<SCAS> makeRDD(CollectionReader reader, JavaSparkContext jsc) throws IOException, UIMAException {
    List<SCAS> scasList = readFrom(reader);
    return jsc.parallelize(scasList);
  }

  /**
   * Use this method for testing only, as it creates a new AnalysisEngine from the
   * description for EVERY SINGLE CALL.
   *
   * @param scas
   * @param desc
   * @return
   * @throws CASException
   * @throws AnalysisEngineProcessException
   * @throws ResourceInitializationException
   */
  public static SCAS process(SCAS scas, AnalysisEngineDescription desc)
      throws CASException, AnalysisEngineProcessException, ResourceInitializationException {
    AnalysisEngine ae = AnalysisEngineFactory.createEngine(desc);
    ae.process(scas.getJCas());
    return scas;
  }

  /**
   * Set the disambiguation settings for each document.
   * @param jcas
   * @param isCoherent
   * @param confidenceThreshold
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws MissingSettingException
   * @throws IOException
   */
  public static void addSettingsToJCas(JCas jcas, Boolean isCoherent, Double confidenceThreshold)
          throws ClassNotFoundException, NoSuchMethodException, MissingSettingException, IOException {
    Document.Builder docBuilder = new Document.Builder();
    docBuilder.withText(jcas.getDocumentText());
    DisambiguationSettings.Builder disBuilder = new DisambiguationSettings.Builder();

    if (isCoherent != null) {
      if (isCoherent) {
        disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);
      } else {
        disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_LOCAL);
      }
    } else {
      disBuilder.withDisambiguationMethod(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE);
    }

    if (confidenceThreshold != null) {
      disBuilder.withNullMappingThreshold(confidenceThreshold);
    }

    docBuilder.withDisambiguationSettings(disBuilder.build());
    Document d = docBuilder.build();

    d.addSettingstoJcas(jcas);
  }
}
