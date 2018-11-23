package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.OutputUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.PrintWriter;



public class Dump2Json extends JCasAnnotator_ImplBase {

  public static final String PARAM_ENCODING = "sourceEncoding";
  @ConfigurationParameter(
      name = "sourceEncoding",
      mandatory = true,
      defaultValue = {"UTF-8"},
      description = "Name of configuration parameter that contains the character encoding used by the input files."
  )
  private String sourceEncoding;

  public static final String PARAM_OUTPUT_DIRECTORY = "outDirectory";
  @ConfigurationParameter(
      name = "outDirectory",
      mandatory = true
  )
  private String outDirectory;

  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String json = null;
    DocumentMetaData md = null;
    try {
      ProcessedDocument pd = OutputUtils.generateProcessedPutputfromJCas(aJCas);
      AnalyzeOutput elo = OutputUtils.generateAnalyzeOutputfromProcessedDocument(pd);
      json = OutputUtils.generateJSONStringfromAnalyzeOutput(elo);
      md = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    PrintWriter out = null;
    File dir = new File(outDirectory);
    if(!dir.exists()) {
      dir.mkdir();
    }
    try {
      File file = new File(dir.getPath() + "/" + md.getDocumentId() + ".json");
      out = new PrintWriter(file);
      out.println(json);
    } catch (Exception var7) {
        throw new AnalysisEngineProcessException(var7);
    } finally {
        IOUtils.closeQuietly(out);
    }
  }
}

