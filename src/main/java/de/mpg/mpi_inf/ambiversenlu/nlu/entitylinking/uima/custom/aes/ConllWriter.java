package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import static org.apache.uima.fit.util.JCasUtil.selectCovering;



public class ConllWriter extends JCasAnnotator_ImplBase {

  public static final String PARAM_OUTPUT_DIRECTORY = "targetLocation";
  @ConfigurationParameter(
      name = "targetLocation",
      mandatory = true
  )
  private String targetLocation;

  public static final String PARAM_WRITE = "write";
  @ConfigurationParameter(
      name = "write",
      mandatory = true
  )
  private String write;
  private static Set<String> toWrite = new HashSet<>();

  @Override public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if(write != null) {
      for(String w: write.split(",")) {
        toWrite.add(w);
      }
    }
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String output = null;
    DocumentMetaData md = null;
    int seAdded = 0;
    try {
      md = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
      StringJoiner docBuilder = new StringJoiner("\n");

      docBuilder.add("-DOCSTART- (" +  md.getDocumentId() + ")");
      docBuilder.add("");

      if(toWrite.contains("token")) {
        Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
        for(Sentence sentence: sentences) {
          List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
          String pner = null;
          String psal = null;
          Integer psalEnd = null;
          for(Token token: tokens) {
            CoreLabel taggedWord = CoreNlpUtils.tokenToWord(token);
            StringJoiner lineBuilder = new StringJoiner("\t");
            lineBuilder.add(taggedWord.word());
            if(toWrite.contains("ner")) {

              List<NamedEntity> nes = selectCovering(NamedEntity.class, token);
              if (nes.size() > 0) {
                String id = nes.get(0).getValue();
                if (pner != null && !pner.equals(id)) {
                  id = "B-" + id;
                } else {
                  id = "I-" + id;
                }
                pner = nes.get(0).getValue();
                lineBuilder.add(id);
              } else {
                lineBuilder.add("O");
                pner = null;
              }
            }
            if(toWrite.contains("sle")) {
              List<SalientEntity> neds = JCasUtil.selectCovering(jCas, SalientEntity.class, token);
              if(neds.size() > 0) {
                if(psal != null && !psal.equals(neds.get(0).getID())) {
                  lineBuilder.add("B-SLE");
                  seAdded++;
                } else {
                  lineBuilder.add("I-SLE");
                }
                psal = neds.get(0).getID();
              } else {
                if(psal != null) {
                  seAdded++;
                }
                lineBuilder.add("O");
                psal = null;
              }
            }
            docBuilder.add(lineBuilder.toString());
          }
          docBuilder.add("");

      }
    } else {
        docBuilder.add(jCas.getDocumentText());
      }
      PrintWriter out = null;
      File dir = new File(targetLocation);
      if(!dir.exists()) {
        dir.mkdir();
      }
      try {
        File file = new File(dir.getPath() + "/" + md.getDocumentId() + ".txt");
        out = new PrintWriter(file);
        out.println(docBuilder.toString());
      } catch (Exception var7) {
        throw new AnalysisEngineProcessException(var7);
      } finally {
        IOUtils.closeQuietly(out);
      }
  } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
