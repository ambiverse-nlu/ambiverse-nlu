package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.LanguageNotSupportedException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class StanfordTokenizer
    extends JCasAnnotator_ImplBase {

  private static Properties props = new Properties();

  private static StanfordCoreNLP [] stanfordCoreNLPs = null;

  private static Map<String, Integer> languageMap = new HashMap<>();

  @Override
  public void initialize(UimaContext aContext)
      throws ResourceInitializationException {
    super.initialize(aContext);

    if(stanfordCoreNLPs != null) {
      return;
    }

    stanfordCoreNLPs = new StanfordCoreNLP[4];

    Properties props = new Properties();
    props.put("tokenize.options", "untokenizable=noneDelete");
    props.put("annotators", "tokenize, ssplit");
    props.put("tokenize.language", Language.English);
    stanfordCoreNLPs[0] =  new StanfordCoreNLP(props, true);
    languageMap.put("en", 0);

    props = new Properties();
    props.put("tokenize.options", "untokenizable=noneDelete");
    props.put("annotators", "tokenize, ssplit");
    props.put("tokenize.language", Language.German);
    stanfordCoreNLPs[1] =  new StanfordCoreNLP(props, true);
    languageMap.put("de", 1);

    props = new Properties();
    props.put("tokenize.options", "untokenizable=noneDelete");
    props.put("annotators", "tokenize, ssplit");
    props.put("tokenize.language", Language.Spanish);
    stanfordCoreNLPs[2] =  new StanfordCoreNLP(props, true);
    languageMap.put("es", 2);

    try {
      props = ClassPathUtils.getPropertiesFromClasspath("StanfordCoreNLP-chinese.properties");
      props.put("tokenize.options", "untokenizable=noneDelete");
      props.put("annotators", "tokenize, ssplit");
      props.put("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+");
      stanfordCoreNLPs[3] = new StanfordCoreNLP(props, true);
      languageMap.put("zh", 3);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }


  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String text = aJCas.getDocumentText();
    Annotation document = new Annotation(text);
    StanfordCoreNLP stanfordCoreNLP;

    if(!languageMap.containsKey(aJCas.getDocumentLanguage())) {
      throw new AnalysisEngineProcessException(new LanguageNotSupportedException("Language Not Supported"));
    }

    stanfordCoreNLP = stanfordCoreNLPs[languageMap.get(aJCas.getDocumentLanguage())];

    stanfordCoreNLP.annotate(document);
    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      int sstart = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
      int ssend = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
      Sentence jsentence = new Sentence(aJCas, sstart, ssend);
      jsentence.addToIndexes();

      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        Token casToken = new Token(aJCas, token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
        casToken.addToIndexes();
      }
    }
  }
}
