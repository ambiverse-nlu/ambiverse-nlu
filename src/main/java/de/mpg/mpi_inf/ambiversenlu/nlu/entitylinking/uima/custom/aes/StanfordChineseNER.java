package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.*;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

@TypeCapability(inputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, outputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })

/**
 * Stanford sentence splitter and tokenizer.
 */ public class StanfordChineseNER extends JCasAnnotator_ImplBase {

  private static Map<String, String> expectedSuccessdingTags = new HashMap<>();

  private static AbstractSequenceClassifier<CoreLabel> classifier;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    expectedSuccessdingTags.put("GPE", "GPE");
    expectedSuccessdingTags.put("I-GPE", "I-GPE");
    expectedSuccessdingTags.put("B-GPE", "I-GPE");
    expectedSuccessdingTags.put("LOC", "LOC");
    expectedSuccessdingTags.put("I-LOC", "I-LOC");
    expectedSuccessdingTags.put("B-LOC", "I-LOC");
    expectedSuccessdingTags.put("PERSON", "PERSON");
    expectedSuccessdingTags.put("I-PER", "I-PER");
    expectedSuccessdingTags.put("B-PER", "I-PER");
    expectedSuccessdingTags.put("ORG", "ORG");
    expectedSuccessdingTags.put("I-ORG", "I-ORG");
    expectedSuccessdingTags.put("B-ORG", "I-ORG");
    expectedSuccessdingTags.put("MISC", "MISC");
    expectedSuccessdingTags.put("I-MISC", "I-MISC");
    expectedSuccessdingTags.put("B-MISC", "I-MISC");

    try {
      Properties props = ClassPathUtils.getPropertiesFromClasspath("edu/stanford/nlp/models/ner/chinese.kbp.distsim.prop");
      props.put("ner.useSUTime", "false"); //false not for english
      props.put("ner.applyNumericClassifiers", "false"); //false not for english
      props.put("mergeTags", "false");
      classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/chinese.kbp.distsim.crf.ser.gz", props);
    } catch (IOException | ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override public void process(JCas aJCas) throws AnalysisEngineProcessException {

    String previousTag = null;

    List<CoreLabel> words = new ArrayList<>();

    for (Sentence sentence : select(aJCas, Sentence.class)) {
      List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

      for (Token t : tokens) {
        words.add(CoreNlpUtils.tokenToWord(t));
      }
    }
    Integer start = null;
    Integer end = null;

    List<CoreLabel> taggedWords = classifier.classify(words);
    for (CoreLabel token : taggedWords) {
      String currentTokenTag = token.get(CoreAnnotations.AnswerAnnotation.class);

      if (previousTag == null) {
        if (expectedSuccessdingTags.containsKey(currentTokenTag)) {
          previousTag = currentTokenTag;
          start = token.get(CharacterOffsetBeginAnnotation.class);
          end = token.get(CharacterOffsetEndAnnotation.class);
        }
      } else if (expectedSuccessdingTags.get(previousTag).equals(currentTokenTag)) {
        end = token.get(CharacterOffsetEndAnnotation.class);
      } else {
        NamedEntity neAnno = new NamedEntity(aJCas, start, end);
        neAnno.addToIndexes();
        previousTag = null;
        if (expectedSuccessdingTags.containsKey(currentTokenTag)) {
          previousTag = currentTokenTag;
          start = token.get(CharacterOffsetBeginAnnotation.class);
          end = token.get(CharacterOffsetEndAnnotation.class);
        }
      }
    }
    if (previousTag != null) {
      NamedEntity neAnno = new NamedEntity(aJCas, start, end);
      neAnno.addToIndexes();
    }
    previousTag = null;
  }
}