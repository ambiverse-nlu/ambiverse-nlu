package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Match;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.OpenIEFactExtraction;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.model.OpenFact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;

public class OutputUtils {

  public static ObjectMapper om = new ObjectMapper();

  private static final String WIKIDATA_PREFIX = "http://www.wikidata.org/entity/";

  public static AnalyzeOutput generateAnalyzeOutputfromProcessedDocument(ProcessedDocument pd) throws EntityLinkingDataAccessException {
    AnalyzeOutput elo = new AnalyzeOutput();
    elo.setDocId(pd.getDocId());
    elo.setLanguage(pd.getLanguage().toString());
    List<Match> matchList = new ArrayList<>();

    //Loop once through the mentions to get the ids and the metadata
    Set<KBIdentifiedEntity> kbIds = new LinkedHashSet<>();
    pd.getEntityAnnotations().stream().forEach(ea -> {
      kbIds.add(ea.getBestEntity().getKbEntity());
    });

    Map<KBIdentifiedEntity, EntityMetaData> metadata = DataAccess.getEntitiesMetaData(kbIds);

    Set<Entity> entities = new HashSet<>();

    Mentions mentions = pd.getMentions();

    for (ResultMention rm : pd.getEntityAnnotations()) {
      //Check if mention is null
      //mention.getNerType();

      Match match = new Match();
      match.setText(rm.getMention());
      match.setCharOffset(rm.getCharacterOffset());
      match.setCharLength(rm.getCharacterLength());


      Entity matchEntity = new Entity();
      Entity entity = new Entity();
      //entity.setId(rm.getBestEntity().getKgId());
      //Set WikiDataID instead of kgID
      EntityMetaData entityMetadata = metadata.get(rm.getBestEntity().getKbEntity());
      if (entityMetadata != null) {
        String prefix;
        if(entityMetadata.getKnowledgebase().equals("YAGO3") || entityMetadata.getKnowledgebase().equals("TESTING")) {
          prefix = WIKIDATA_PREFIX;
        } else if(entityMetadata.getKnowledgebase() != null){
          prefix = entityMetadata.getKnowledgebase();
        } else {
          prefix = "";
        }


        String entityId = prefix + entityMetadata.getWikiData();
        matchEntity.setId(entityId);
        entity.setId(entityId);
        String name = entityMetadata.getHumanReadableRepresentation();
        if(name == null || name.isEmpty()) {
          name = rm.getMention();
        }
        entity.setName(name);
        entity.setUrl(entityMetadata.getUrl());

        //Set the most salient type of the entity
        de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity e = new de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity(rm.getBestEntity().getKbEntity(), entityMetadata.getId());
        TIntObjectHashMap<EntityType> classes = DataAccess.getEntityClasses(e);

        // It's a concept.
        if (classes.get(e.getId()) == EntityType.CONCEPT) {
          entity.setType(Entity.Type.CONCEPT);
        } else { // It's an entity!
          Set<Type> entityTypes = DataAccess.getTypes(e);
          if (entityTypes != null) {
            Set<String> entityTypesString = entityTypes.stream().map(t -> t.getName()).collect(Collectors.toSet());
            entity.setType(Entity.getEntityType(entityTypesString));
          }
        }
      }

      matchEntity.setConfidence(rm.getBestEntity().getScore());
      match.setEntity(matchEntity);

      entity.setSalience(rm.getBestEntity().getSalience());

      //Add empty Entity
      if (entityMetadata == null || entityMetadata.getWikiData() == null) {
        match.setEntity(new Entity());
      }

      Mention mention = mentions.getMentions().get(rm.getCharacterOffset()).get(rm.getCharacterLength());
      if(mention != null) {
        match.setType(mention.getNerType());
      } else {
        String type = entity.getType() == null ? "" : entity.getType().name();
        match.setType(type);
      }

      if(entity.getId() != null) {
        entities.add(entity);
      }

      matchList.add(match);
    }
    elo.setMatches(matchList);
    elo.setEntities(entities);
    elo.setText(pd.getText());

    if(pd.getServiceFacts() != null && !pd.getServiceFacts().isEmpty()) {
      elo.setFacts(pd.getServiceFacts());
    }
    return elo;
  }

  public static String generateJSONStringfromAnalyzeOutput(AnalyzeOutput elo) throws JsonProcessingException {
    return om.writeValueAsString(elo);
  }

  public static ProcessedDocument generateProcessedPutputfromJCas(JCas jCas) throws CASException {
    AidaDocumentSettings ads = selectSingle(jCas, AidaDocumentSettings.class);
    ProcessedDocument result = new ProcessedDocument(ads.getDocumentId(), Language.getLanguageForString(jCas.getDocumentLanguage()),
        jCas.getDocumentText());

    Map<String, SalientEntity> salientEntities = Utils.getSalientEntitiesFromJCas(jCas);

    List<ResultMention> rmentions = new ArrayList<>();
    // Add aida results:
    for (AidaEntity ae : select(jCas, AidaEntity.class)) {
      ResultMention rm = new ResultMention();
      rm.setMention(ae.getCoveredText());
      rm.setCharacterOffset(ae.getBegin());
      rm.setCharacterLength(ae.getEnd() - ae.getBegin());
      ResultEntity re = new ResultEntity();
      re.setKgId(ae.getID());
      re.setScore(ae.getScore());
      if(salientEntities.containsKey(ae.getID())) {
        re.setSalience(salientEntities.get(ae.getID()).getSalience());
      }
      rm.setBestEntity(re);
      rmentions.add(rm);
    }
    // Add concept results:
    for (ConceptEntity ae : select(jCas, ConceptEntity.class)) {
      ResultMention rm = new ResultMention();
      rm.setMention(ae.getCoveredText());
      rm.setCharacterOffset(ae.getBegin());
      rm.setCharacterLength(ae.getEnd() - ae.getBegin());
      ResultEntity re = new ResultEntity();
      re.setKgId(ae.getID());
      re.setScore(ae.getScore());
      rm.setBestEntity(re);
      rmentions.add(rm);
    }
    result.setEntityAnnotations(rmentions);

    Mentions mentions = new Mentions();
    Map<Integer, Set<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity>> goldNE = Utils.getGoldAidaAnnotationsFromJCas(jCas);

    for (Map<Integer, Mention> innerMap : Mentions.getNeMentionsFromJCas(jCas).getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if(goldNE != null && goldNE.containsKey(m.getCharOffset())) {
          for (de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity e : goldNE.get(m.getCharOffset())) {
            if(e.getEnd() == m.getCharOffset() + m.getCharLength()) {
              m.addGroundTruthResult(e.getID());
            }
          }
        }
        mentions.addMention(m);
      }
    }
    Map<Integer, Set<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept>> goldC = Utils.getGoldConceptAnnotationsFromJCas(jCas);
    for (Map<Integer, Mention> innerMap : Mentions.getConceptMentionsFromJCas(jCas).getMentions().values()) {
      for (Mention m : innerMap.values()) {
        if(goldC != null && goldC.containsKey(m.getCharOffset())) {
          for (de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept e : goldC.get(m.getCharOffset())) {
            if(e.getEnd() == m.getCharOffset() + m.getCharLength()) {
              m.addGroundTruthResult(e.getID());
            }
          }
        }
        mentions.addMention(m);
      }
    }

    result.setMentions(mentions);
    result.setTokens(Tokens.getTokensFromJCas(jCas));

    List<OpenFact> openFacts = OpenFact.getOpenFactsFromJCas(jCas);
    result.setFacts(openFacts);

    try {
      List<Fact> serviceFacts =  OpenIEFactExtraction.linkFactsToKnowledgeGraph(jCas);
      result.setServiceFacts(serviceFacts);
    } catch (ClassNotFoundException | NoSuchMethodException | MissingSettingException | IOException | UIMAException | EntityLinkingDataAccessException | UnprocessableDocumentException e) {
      throw new CASException(e);
    }



    //Add domains:
    List<String> domains = new ArrayList<>();
    for (Domain domain:select(jCas, Domain.class))
     domains.add(domain.getDomain());
    result.setDomains(domains);

    //Add synsets:
    List<String> synsets = new ArrayList<>();
    for (Synset synset:select(jCas, Synset.class))
      synsets.add(synset.getSynset());
    result.setSynsets(synsets);

    //Add Domain Words:
    List<String> domainWords = new ArrayList<>();
    for (DomainWord dw:select(jCas, DomainWord.class)) {
      domainWords.add(dw.getDomainWord());
    }
    result.setDomainWords(domainWords);

//    //Add Concept Candidate:
//    List<String> conceptMentionCandidates = new ArrayList<String>();
//    for (ConceptMentionCandidate cc : select(jCas, ConceptMentionCandidate.class)) {
//      conceptMentionCandidates.add(cc.getCoveredText());
//    }
//    result.setConceptMentionCandidates(conceptMentionCandidates);
//
//    //Add Concepts spotted in text:
//    List<String> concepts = new ArrayList<>();
//    for (ConceptMention c:select(jCas, ConceptMention.class)) {
//      concepts.add(c.getCoveredText());
//    }
//    result.setConceptMentions(concepts);


    //Add Top Types:
    List<Pair<Integer, String> > topTypes = new ArrayList<>();
    for (WikiType type:select(jCas, WikiType.class)) {
      topTypes.add(new Pair<Integer, String>(type.getId(), type.getName()));
    }
    result.setTopTypes(topTypes);

    return result;
  }

}
