package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.nlp.ProperNounManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

@TypeCapability(inputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, outputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })

public class StanfordProperNounNER extends JCasAnnotator_ImplBase {

  private static ProperNounManager properNounManager = new ProperNounManager();

  @Override public void process(JCas aJCas) throws AnalysisEngineProcessException {

    for (Sentence sentence : select(aJCas, Sentence.class)) {
      List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

      List<CoreLabel> words = new ArrayList<>(tokens.size());
      Integer start = null;
      Integer end = null;
      for (Token t : tokens) {  //This step should be avoided. Transform directly from DKPRO to AIDA TOKEN. Problem POS mappings. AIDA works with Stanford tags
        words.add(CoreNlpUtils.tokenToWord(t));
      }

      for (CoreLabel token : words) {

        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

        if (properNounManager.isProperNounTag(pos, Language.getLanguageForString("de"))) {
          if (start == null) {
            start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
          }
          end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
        } else {
          if (start != null) {
            NamedEntity ne = new NamedEntity(aJCas, start, end);
            ne.setValue("pn");
            ne.addToIndexes();
            start = null;
            end = null;
          }
        }
      }
      if (start != null) {
        NamedEntity ne = new NamedEntity(aJCas, start, end);
        ne.setValue("pn");
      }
    }
  }
}
