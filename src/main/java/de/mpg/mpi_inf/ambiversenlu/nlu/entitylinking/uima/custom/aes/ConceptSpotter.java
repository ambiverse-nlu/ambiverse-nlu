package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.StopWord;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.Spot;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TextSpotter;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TrieBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.Utils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.lucene.util.fst.FST;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * This UIMA component is intended for spotting concepts or mention in an input text.
 * It is implemented via finite state transducers. The component is language dependant.
 *
 */
public class ConceptSpotter extends JCasAnnotator_ImplBase {

  private static FST[] trie = new FST[Language.activeLanguages().size()];
  private Logger logger = LoggerFactory.getLogger(ConceptSpotter.class);

//  /**
//   * The language.
//   */
//  public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
//
//  @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true) private String language;

  /**
   * The size of the cursor queue.
   */
  public static final String PARAM_QUEUE_SIZE = "queue_size";
  @ConfigurationParameter(name = PARAM_QUEUE_SIZE, mandatory = false) 
  private int QUEUE_SIZE = 1000;

  /**
   * Minimun matching ration
   */
  public static final String PARAM_MATCHING_RATIO = "matching_ratio";
  @ConfigurationParameter(name = PARAM_MATCHING_RATIO, mandatory = false) 
  private double MATCHING_RATIO = 0.9;



  @Override public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    Set<String> orderedMentions = null;
    try {
      logger.info("Initializing ConceptSpotter.");

      for(Language language : Language.activeLanguages()) {
        long start = System.currentTimeMillis();

        orderedMentions = new TreeSet<>(DataAccess.getMentionsforLanguage(language, false));
        trie[language.getID()] = TrieBuilder.buildTrie(orderedMentions);

        long dur = System.currentTimeMillis() - start;
        logger.info("Initialized ConceptSpotter '" + language + "' in " + dur/1_000 + "s.");
      }

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }


  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    Integer runningId = RunningTimer.recordStartTime("ConceptSpotter");
    Language language = Language.getLanguageForString(jCas.getDocumentLanguage());
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    Set<Integer> tokenEnd = new HashSet<>();
    Set<Integer> tokenBegin = new HashSet<>();
    for (Token token : tokens) {
      try {
        if (!StopWord.isStopwordOrSymbol(token.getCoveredText(), language)) {
          tokenEnd.add(token.getEnd());
          tokenBegin.add(token.getBegin());
        }
      } catch (EntityLinkingDataAccessException e) {
        throw new AnalysisEngineProcessException(
                "No stop words defined for language " + language + ". Concept Spotting will not work!",
                null,
                e);
      }
    }
    String text = jCas.getDocumentText();

    // Here all concept candidates including the ones that might have overlap with each other or with NEs are generated.
    // Elimination of overlaps/shared with NEs, should be done in Concept Filters.
    try {
      Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie[language.getID()], text,tokenBegin, tokenEnd, MATCHING_RATIO);
      for (Spot spot : spots) {
        String mentionSpotted = Utils.getStringbyKey(spot.getMatch(), trie[language.getID()]);
        String mention = text.substring(spot.getBegin(), spot.getEnd());

        // For the exact match: since even if matching ration is 1.0, very small differences are passed in the TextSpotter.
        if(MATCHING_RATIO == 1.0 && !mentionSpotted.equalsIgnoreCase(mention)) {
          continue;
        }
        ConceptMentionCandidate candid = new ConceptMentionCandidate(jCas, spot.getBegin(), spot.getEnd());
        candid.setConceptCandidate(mentionSpotted); // The spotted text
        candid.addToIndexes();
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
    RunningTimer.recordEndTime("ConceptSpotter", runningId);
  }
}