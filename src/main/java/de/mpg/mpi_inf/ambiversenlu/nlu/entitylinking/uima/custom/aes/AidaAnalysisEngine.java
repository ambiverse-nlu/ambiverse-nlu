package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.DisambiguationEntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.Disambiguator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.Preparator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.DocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.ByteArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.uima.fit.util.JCasUtil.selectSingle;

public class AidaAnalysisEngine extends JCasAnnotator_ImplBase {

  private Logger logger = LoggerFactory.getLogger(AidaAnalysisEngine.class);

  private DisambiguationSettings ds;

  public static final String USE_RESULTS = "useResults";
  @ConfigurationParameter(name = USE_RESULTS, mandatory = true)
  private boolean useResults;

  @Override public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      logger.debug("Initializing AidaAnalysis Engine.");
      EntityLinkingManager.init();
      ds = new DisambiguationSettings.Builder().build();
    } catch (MissingSettingException | NoSuchMethodException | IOException | ClassNotFoundException | EntityLinkingDataAccessException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override public void destroy() {
    try {
      EntityLinkingManager.shutDown();
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    logger.debug("Processing jCas");

    Tokens tokens = Tokens.getTokensFromJCas(jCas);
    Mentions neMentions = Mentions.getNeMentionsFromJCas(jCas);
    List<ResultMention> resultMentionListNE = ResultMention.getResultAidaMentionsFromJCas(jCas);
    List<ResultMention> resultMentionListC;
    if (useResults) {
      resultMentionListC = ResultMention.getResultConceptMentionsFromJCas(jCas);
    }
    else {
      resultMentionListC = new ArrayList<>();
    }

    Map<Integer, Map<Integer, ResultMention>> resultMentionMap = new HashMap<>();
    Mentions mentions = new Mentions();

    String docId = JCasUtil.selectSingle(jCas, DocumentMetaData.class).getDocumentId();
    logger.debug("Processing document {}.", docId);

    AidaDocumentSettings ads = selectSingle(jCas, AidaDocumentSettings.class);
    AidaDisambiguationSettings dds = ads.getDisambiguationSettings();

    logger.debug("Creating disambiguation settings.");
    DisambiguationSettings disSettings;
    if (dds == null) {
      disSettings = ds;
      ds.setLanguageSettings(
          LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(Language.getLanguageForString(jCas.getDocumentLanguage())));
    } else {
      try {
        ByteArray ba = dds.getDisambiguationSettingsBytes();
        disSettings = DisambiguationSettings.decode(ba.toArray());
        disSettings.setLanguageSettings(
            LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(Language.getLanguageForString(jCas.getDocumentLanguage())));
      } catch (IOException | ClassNotFoundException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }

    Preparator p = new Preparator();
    PreparedInput input;
    try {
      for (ResultMention rm : resultMentionListNE) {
        resultMentionMap.putIfAbsent(rm.getCharacterOffset(), new HashMap<>());
        resultMentionMap.get(rm.getCharacterOffset()).put(rm.getCharacterLength(), rm);
      }

      // If the mention exists in Results do not add it in case of two-stage NER-NED (no need to disambiguate again).
      for (Map<Integer, Mention> innerMap : neMentions.getMentions().values()) {
        for (Mention m : innerMap.values()) {
          if (!resultMentionMap.containsKey(m.getCharOffset())) {
            mentions.addMention(m);
          }
        }
      }

      // Add the concept result if the mention is not available in the ne mentions.
      for (ResultMention rm : resultMentionListC) {
        if (!mentions.containsOffsetAndLength(rm.getCharacterOffset(), rm.getCharacterLength())) {
          resultMentionMap.putIfAbsent(rm.getCharacterOffset(), new HashMap<>());
          resultMentionMap.get(rm.getCharacterOffset()).put(rm.getCharacterLength(), rm);
        }
      }
      logger.debug("Creating prepared input");
      input = p.prepareInputData(docId, tokens, new Mentions(), mentions, resultMentionMap , disSettings, DocumentChunker.DOCUMENT_CHUNK_STRATEGY.valueOf(ads.getDocChunkStrategy()));
    } catch (EntityLinkingDataAccessException | AidaUnsupportedLanguageException e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException(e);
    }

    logger.debug("Creating disambiguator.");
    Disambiguator disambiguator;
    Tracer tracer = null;
    if (disSettings.getTracingTarget() == null) {
      logger.debug("Creating disambiguator when tracing target is null");
      disambiguator = new Disambiguator(input, disSettings, DisambiguationEntityType.NAMED_ENTITY);
    } else {
      logger.debug("Creating disambiguator when tracing target is not null");
      tracer = new Tracer(disSettings.getTracingPath(), docId);
      disambiguator = new Disambiguator(input, disSettings, tracer, DisambiguationEntityType.NAMED_ENTITY);
    }

    DisambiguationResults results = null;
    try {
      logger.debug("Disambiguating...");
      results = disambiguator.disambiguate();
      if (disSettings.getTracingTarget() != null) {
        logger.debug("Writing trace output ...");
        tracer.writeSimilarityOutput(false, false);
        if (disSettings.getDisambiguationMethod().equals(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE)) {
          tracer.writeGraphOutput(results);
        }
      }

    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    logger.debug("Writing results to CAS.");
    writeResultsToCAS(results, neMentions, jCas);
  }

  private void writeResultsToCAS(DisambiguationResults results, Mentions mentions, JCas jCas) {
    for (ResultMention rm : results.getResultMentions()) {
      if (mentions.containsOffsetAndLength(rm.getCharacterOffset(), rm.getCharacterLength())) {
        ResultEntity re = rm.getBestEntity();
        AidaEntity entity = new AidaEntity(jCas, rm.getCharacterOffset(), rm.getCharacterOffset() + rm.getCharacterLength());
        entity.setID(re.getKbEntity().getDictionaryKey());
        entity.setScore(re.getScore());
        entity.addToIndexes();
        logger.debug("NamedEntity results: " + entity.getID() + " for mention: " + rm.getMention() + " score: " + re.getScore() + " salient: " + re.getSalience());
      }
    }
  }
}