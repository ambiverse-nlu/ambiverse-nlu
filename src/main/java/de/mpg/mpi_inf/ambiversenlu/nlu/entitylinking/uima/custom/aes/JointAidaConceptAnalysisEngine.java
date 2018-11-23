package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.DisambiguationEntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.Disambiguator;
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
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.ByteArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.uima.fit.util.JCasUtil.selectSingle;

public class JointAidaConceptAnalysisEngine extends JCasAnnotator_ImplBase {

  private Logger logger = LoggerFactory.getLogger(JointAidaConceptAnalysisEngine.class);
  
  private DisambiguationSettings ds;
  
  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      ds = new DisambiguationSettings.Builder().build();
    } catch (MissingSettingException | NoSuchMethodException | IOException
        | ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }
  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Tokens tokens = Tokens.getTokensFromJCas(jCas);
    Mentions neMentions = Mentions.getNeMentionsFromJCas(jCas);
    Mentions cpMentions = Mentions.getConceptMentionsFromJCas(jCas);
    Mentions mentionsC = new Mentions();
    Mentions mentionsNE = new Mentions();

    // This is needed in case a two-stage NER-NED pipeline is run - results from first NED run should be kept.
    List<ResultMention> resultMentionListNE = ResultMention.getResultAidaMentionsFromJCas(jCas);
    Map<Integer, Map<Integer, ResultMention>> resultMentionMap = new HashMap<>();

    
    String docId = JCasUtil.selectSingle(jCas, DocumentMetaData.class).getDocumentId();
    AidaDocumentSettings ads = selectSingle(jCas, AidaDocumentSettings.class);
    AidaDisambiguationSettings dds = ads.getDisambiguationSettings();
    
    DisambiguationSettings disSettings;
    if(dds == null) {
      disSettings = ds;
      ds.setLanguageSettings(LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(Language.getLanguageForString(jCas.getDocumentLanguage())));
    } else {
      try {
        ByteArray ba = dds.getDisambiguationSettingsBytes();
        disSettings = DisambiguationSettings.decode(ba.toArray());
        disSettings.setLanguageSettings(LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(Language.getLanguageForString(jCas.getDocumentLanguage())));
      } catch (IOException | ClassNotFoundException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
    
    Preparator p = new Preparator();
    PreparedInput input;
    try {
      for (Map<Integer, Mention> innerMap : cpMentions.getMentions().values()) {
        for (Mention m : innerMap.values()) {
          mentionsC.addMention(m);
        }
      }
      for (Map<Integer, Mention> innerMap : neMentions.getMentions().values()) {
        for (Mention m : innerMap.values()) {
          mentionsNE.addMention(m);
        }
      }
      for (ResultMention rm : resultMentionListNE) {
        resultMentionMap.putIfAbsent(rm.getCharacterOffset(), new HashMap<>());
        resultMentionMap.get(rm.getCharacterOffset()).put(rm.getCharacterLength(), rm);
      }
      input = p.prepareInputData(docId, tokens, mentionsC, mentionsNE, resultMentionMap, disSettings, DocumentChunker.DOCUMENT_CHUNK_STRATEGY.valueOf(ads.getDocChunkStrategy()));
    } catch (EntityLinkingDataAccessException | AidaUnsupportedLanguageException e) {
      throw new AnalysisEngineProcessException(e);
    }
    
    Disambiguator disambiguator;
    Tracer tracer = null;
    if(disSettings.getTracingTarget() == null) {
      disambiguator = new Disambiguator(input, disSettings, DisambiguationEntityType.JOINT);
    } else {
      tracer = new Tracer(disSettings.getTracingPath(), docId);
      disambiguator = new Disambiguator(input, disSettings, tracer, DisambiguationEntityType.JOINT);
    }
    
    DisambiguationResults results = null;
    try {
      results = disambiguator.disambiguate();
      if(disSettings.getTracingTarget() != null) {
        tracer.writeSimilarityOutput(false, false);
        if(disSettings.getDisambiguationMethod().equals(DisambiguationSettings.DISAMBIGUATION_METHOD.LM_COHERENCE)) {
          tracer.writeGraphOutput(results);
        }
      }

    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    writeResultsToCAS(results, cpMentions, neMentions, jCas);
  }

  private void writeResultsToCAS(DisambiguationResults results, Mentions cpMentions, Mentions neMentions, JCas jCas) {
    for (ResultMention rm : results.getResultMentions()) {
      if (cpMentions.containsOffsetAndLength(rm.getCharacterOffset(), rm.getCharacterLength())) {
        ResultEntity re = rm.getBestEntity();
        ConceptEntity entity = new ConceptEntity(jCas, rm.getCharacterOffset(), rm.getCharacterOffset() + rm.getCharacterLength());
        entity.setID(re.getKbEntity().getDictionaryKey());
        entity.setScore(re.getScore());
        entity.addToIndexes();
        logger.info("Concept results: " + entity.getID() + " for mention: " + rm.getMention());
      }
      if (neMentions.containsOffsetAndLength(rm.getCharacterOffset(), rm.getCharacterLength())) {
        ResultEntity re = rm.getBestEntity();
        AidaEntity entity = new AidaEntity(jCas, rm.getCharacterOffset(), rm.getCharacterOffset() + rm.getCharacterLength());
        entity.setID(re.getKbEntity().getDictionaryKey());
        entity.setScore(re.getScore());
        entity.addToIndexes();
        logger.info("NamedEntity results: " + entity.getID() + " for mention: " + rm.getMention());
      }
    }
  }
}
