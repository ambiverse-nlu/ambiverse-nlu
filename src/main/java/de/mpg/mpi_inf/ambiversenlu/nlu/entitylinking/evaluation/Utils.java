package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation;

import au.com.bytecode.opencsv.CSVWriter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EvaluationEntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.*;

import static org.apache.uima.fit.util.JCasUtil.select;


//TODO: This class needs to be rewritten, too messy, too much code replication. Ideally methods should be generic as they all do the same thing
public class Utils implements AutoCloseable {

  static CSVWriter analysedOutput;
  
  static final String TRUE = "True";
  static final String FALSE = "False";
      
  public static Map<Integer, Set<Entity>> getGoldAidaAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<Entity>> entities = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Entity entity : select(goldView, Entity.class)) {
      entities.computeIfAbsent(entity.getBegin(), k -> new HashSet<>()).add(entity);
    }
    return entities;
  }
  
  public static Map<Integer, Set<Concept>> getGoldConceptAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<Concept>> concepts = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Concept concept : select(goldView, Concept.class)) {
      concepts.computeIfAbsent(concept.getBegin(), k -> new HashSet<>()).add(concept);
    }
    return concepts;
  }
  
  public static Map<Integer, Set<Unknown>> getGoldUnknownAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<Unknown>> annotations = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Unknown un : select(goldView, Unknown.class)) {
      annotations.computeIfAbsent(un.getBegin(), k -> new HashSet<>()).add(un);
    }
    return annotations;
  }
  
  private static Map<Integer, Integer> getGoldAidaNERAnnotationsFromJCas(JCas jCas) {
    Map<Integer, Integer> annotations = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Entity entity : select(goldView, Entity.class)) {
      annotations.put(entity.getBegin(), entity.getEnd());
    }
    return annotations;
  }
  
  public static Map<Integer, Integer> getGoldConceptNERAnnotationsFromJCas(JCas jCas) {
    Map<Integer, Integer> annotations = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Concept entity : select(goldView, Concept.class)) {
      annotations.put(entity.getBegin(), entity.getEnd());
    }
    return annotations;
  }
  
  public static Map<Integer, Integer> getGoldUnknownNERAnnotationsFromJCas(JCas jCas) {
    Map<Integer, Integer> annotations = new HashMap<>();
    JCas goldView = JCasUtil.getView(jCas, "gold", jCas);
    if(goldView.getViewName().equals(jCas.getViewName())) {
      return Collections.emptyMap();
    }
    for (Unknown entity : select(goldView, Unknown.class)) {
      annotations.put(entity.getBegin(), entity.getEnd());
    }
    return annotations;
  }

  public static Map<Integer, Set<NamedEntity>> getNERAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<NamedEntity>> nentities = new HashMap<>();
    for (NamedEntity entity : select(jCas, NamedEntity.class)) {
      nentities.computeIfAbsent(entity.getBegin(), k -> new HashSet<>()).add(entity);
    }
    return nentities;
  }
  

  public static Map<Integer, Set<AidaEntity>> getAidaAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<AidaEntity>> aidaentities = new HashMap<>();
    Set<String> entities = new HashSet<>();
    for (AidaEntity entity : select(jCas, AidaEntity.class)) {
      aidaentities.computeIfAbsent(entity.getBegin(), k -> new HashSet<>()).add(entity);

      int start = 0;
      if ( entity.getID().indexOf(":") != -1 ) {
        start = entity.getID().indexOf(":") + 1;
      }
      entities.add(entity.getID().substring(start, entity.getID().length()));
    }
//    return aidaentities;
    Map<String, String> wikidataIds = DataAccess.getWikidataIdForEntities(entities);
    Map<Integer, Set<AidaEntity>> aidaentitiesResult = new HashMap<>(aidaentities);
    for (Set<AidaEntity> entitySet:aidaentitiesResult.values()) {
      for (AidaEntity entity:entitySet) {
        int start = 0;
        if ( entity.getID().indexOf(":") != -1 ) {
          start = entity.getID().indexOf(":") + 1;
          String entityID = entity.getID().substring(start);
          if (wikidataIds.get(entityID) != null) {
            entity.setID(wikidataIds.get(entityID));
          } else {
            entity.setID(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity.OOKBE);
          }
        }
      }
    }
    return aidaentitiesResult;
  }
  
  public static Map<Integer, Set<ConceptEntity>> getConceptAnnotationsFromJCas(JCas jCas) throws CASException {
    Map<Integer, Set<ConceptEntity>> conceptEntities = new HashMap<>();
    Set<String> entities = new HashSet<>();
    for (ConceptEntity entity : select(jCas, ConceptEntity.class)) {
      conceptEntities.computeIfAbsent(entity.getBegin(), k -> new HashSet<>()).add(entity);
      int start = 0;
      if ( entity.getID().indexOf(":") != -1 ) {
        start = entity.getID().indexOf(":") + 1;
      }
      entities.add(entity.getID().substring(start, entity.getID().length()));
    }
//    return conceptEntities;
    Map<String, String> wikidataIds = DataAccess.getWikidataIdForEntities(entities);
    Map<Integer, Set<ConceptEntity>> conceptEntitiesResult = new HashMap<>(conceptEntities);
    for (Set<ConceptEntity> entitySet:conceptEntitiesResult.values()){
      for(ConceptEntity entity:entitySet) {
        int start = 0;
        if ( entity.getID().indexOf(":") != -1 ) {
          start = entity.getID().indexOf(":") + 1;
          String entityID = entity.getID().substring(start, entity.getID().length());
          if (wikidataIds.containsKey(entityID)) {
            entity.setID("<" + wikidataIds.get(entityID) + ">");
          }
        }
      }
    }
    return conceptEntitiesResult;
  }

  public static EvaluationCounts computeCountsDisambiguation(Map<Integer, Map<Integer, Set<String>>> allGolds, Map<Integer, Map<Integer, Set<String>>> allResults, EvaluationSettings es) {
    EvaluationCounts ec = new EvaluationCounts();
    for (Integer begin:allGolds.keySet()) {
      for (Integer end:allGolds.get(begin).keySet()) {
        Set<String> goldIds = allGolds.get(begin).get(end);
        
        if(es.isIgnoreGroundTruthOOKBE() && goldIds.size() == 1 && Entities.isOokbEntity(goldIds.toArray()[0].toString())) {
          ec.setIgnored(ec.getIgnored() + 1);
          continue;
        }
        
        Set<String> resultIds = null;
        if (allResults.containsKey(begin) && allResults.get(begin).containsKey(end)) {
          resultIds = allResults.get(begin).get(end);
        }
        if (resultIds == null || resultIds.isEmpty()) {
          ec.setFalseNegatives(ec.getFalseNegatives() + 1);
          continue;
        }
        
        boolean found = false;
        for (String resultId:resultIds) {
          if (goldIds.contains(resultId)) {
            ec.setTruePositives(ec.getTruePositives() + 1);
            found = true;
            break;
          }
        }
        if (!found) {
          //TODO: I think this is extra here: !es.isIgnoreGroundTruthOOKBE(), when result is ookb it is FN not FP regardless of IGTOOKB
          if (resultIds.size() == 1 && Entities.isOokbEntity(resultIds.toArray()[0].toString()) && !es.isIgnoreGroundTruthOOKBE()) {
            ec.setFalseNegatives(ec.getFalseNegatives() + 1);
          }
          else {
            ec.setFalsePositives(ec.getFalsePositives() + 1);
          }
        }
      }
    }
    
    for (Integer begin : allResults.keySet()) {
      for (Integer end : allResults.get(begin).keySet()) {
        if (!allGolds.containsKey(begin) || !allGolds.get(begin).containsKey(end)) {
          ec.setFalsePositives(ec.getFalsePositives() + 1);
        }
      }
    }
    return ec;
  }
  
  public static Map<String, EvaluationCounts> computeDisambiguationEvaluationfromJCas(JCas jCas, EvaluationSettings es, EvaluationEntityType entityType) throws CASException {
    Set<Integer> goldBegins = new HashSet<>();
    Set<Integer> resultBegins = new HashSet<>();
    Map<Integer, Set<Entity>> entities = new HashMap<>();
    Map<Integer, Set<Concept>> concepts = new HashMap<>();
    Map<Integer, Set<Unknown>> unknowns = new HashMap<>();
    Map<Integer, Set<AidaEntity>> aidaEntities = new HashMap<>();
    Map<Integer, Set<ConceptEntity>> conceptEntitites = new HashMap<>();
    
    if (entityType == EvaluationEntityType.NAMED_ENTITY) {
      entities = getGoldAidaAnnotationsFromJCas(jCas);
      aidaEntities = getAidaAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.CONCEPT) {
      concepts = getGoldConceptAnnotationsFromJCas(jCas);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.UNKNOWN) {
      unknowns = getGoldUnknownAnnotationsFromJCas(jCas);
      aidaEntities = getAidaAnnotationsFromJCas(jCas);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.ALL) {
      entities = getGoldAidaAnnotationsFromJCas(jCas);
      concepts = getGoldConceptAnnotationsFromJCas(jCas);
      unknowns = getGoldUnknownAnnotationsFromJCas(jCas);
      aidaEntities = getAidaAnnotationsFromJCas(jCas);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    goldBegins.addAll(entities.keySet());
    goldBegins.addAll(concepts.keySet());
    goldBegins.addAll(unknowns.keySet());
    resultBegins.addAll(aidaEntities.keySet());
    resultBegins.addAll(conceptEntitites.keySet());
    
    Map<Integer, Map<Integer, Set<String>>> allResults = new HashMap<Integer, Map<Integer, Set<String>>>(); // There can be entities with same begin and different ends
    Map<Integer, Map<Integer, Set<String>>> allGolds = new HashMap<Integer, Map<Integer, Set<String>>>();
    
    for (Integer begin:goldBegins) {
      if (entities.containsKey(begin)) {
        for (Entity e :entities.get(begin)) {
          allGolds.computeIfAbsent(begin, k -> new HashMap<>()).computeIfAbsent(e.getEnd(), k -> new HashSet<>()).add(e.getID());
        }
      }
      if (concepts.containsKey(begin)) {
        for (Concept e :concepts.get(begin)) {
          allGolds.computeIfAbsent(begin, k -> new HashMap<>()).computeIfAbsent(e.getEnd(), k -> new HashSet<>()).add(e.getID());
        }
      }
      if (unknowns.containsKey(begin)) {
        for (Unknown e :unknowns.get(begin)) {
          allGolds.computeIfAbsent(begin, k -> new HashMap<>()).computeIfAbsent(e.getEnd(), k -> new HashSet<>()).add(e.getID());
        }
      }
    }
    
    for (Integer begin:resultBegins) {
      if (aidaEntities.containsKey(begin)) {
        Set<AidaEntity> ends = aidaEntities.get(begin);
        for (AidaEntity entity:ends) {
          String entityId = entity.getID().substring(entity.getID().indexOf(':') + 1);
          allResults.computeIfAbsent(begin, k -> new HashMap<>()).computeIfAbsent(entity.getEnd(), k -> new HashSet<>()).add(entityId);
        }
      }
      if (conceptEntitites.containsKey(begin)) {
        Set<ConceptEntity> ends = conceptEntitites.get(begin);
        for (ConceptEntity entity:ends) {
          String entityId = entity.getID().substring(entity.getID().indexOf(':') + 1);
          allResults.computeIfAbsent(begin, k -> new HashMap<>()).computeIfAbsent(entity.getEnd(), k -> new HashSet<>()).add(entityId);
        }
      }
    }
    
    Map<String, EvaluationCounts> result = new HashMap<>();
    result.put("ec", computeCountsDisambiguation(allGolds, allResults, es));
    return result;
  }

  
  public static Map<String, EvaluationCounts> computeNEREvaluationfromJCas(JCas jCas, EvaluationSettings es, EvaluationEntityType entityType) throws CASException {
    Map<Integer, Integer> allGolds = new HashMap<>();
    Map<Integer, Set<Integer>> allResults = new HashMap<>();
    Map<Integer, Integer> entitiesAnnotations = new HashMap<>();
    Map<Integer, Integer> conceptsAnnotations = new HashMap<>();
    Map<Integer, Integer> unknownsAnnotations = new HashMap<>();
    Map<Integer, Set<NamedEntity>> nentities = new HashMap<>();
    Map<Integer, Set<ConceptEntity>> conceptEntitites = new HashMap<>();
    
    if (entityType == EvaluationEntityType.NAMED_ENTITY) {
      entitiesAnnotations = getGoldAidaNERAnnotationsFromJCas(jCas); //begin and end
      allGolds.putAll(entitiesAnnotations);
      nentities = getNERAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.CONCEPT) {
      conceptsAnnotations = getGoldConceptNERAnnotationsFromJCas(jCas);
      allGolds.putAll(conceptsAnnotations);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.UNKNOWN) {
      unknownsAnnotations = getGoldUnknownNERAnnotationsFromJCas(jCas);
      allGolds.putAll(unknownsAnnotations);
      nentities = getNERAnnotationsFromJCas(jCas);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    if (entityType == EvaluationEntityType.ALL) {
      entitiesAnnotations = getGoldAidaNERAnnotationsFromJCas(jCas);
      allGolds.putAll(entitiesAnnotations);
      conceptsAnnotations = getGoldConceptNERAnnotationsFromJCas(jCas);
      allGolds.putAll(conceptsAnnotations);
      unknownsAnnotations = getGoldUnknownNERAnnotationsFromJCas(jCas);
      allGolds.putAll(unknownsAnnotations);
      nentities = getNERAnnotationsFromJCas(jCas);
      conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    }
    
    for (Integer begin:nentities.keySet()) {
      Set<NamedEntity> ends = nentities.get(begin);
      for (NamedEntity entity:ends) {
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entity.getEnd());
      }
    }
    for (Integer begin:conceptEntitites.keySet()) {
      Set<ConceptEntity> ends = conceptEntitites.get(begin);
      for (ConceptEntity entity:ends) {
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entity.getEnd());
      }
    }
    
    Map<String, EvaluationCounts> result = new HashMap<>();
    result.put("ec", computeCountsNer(allGolds, allResults));
    return result;
  }
  
  private static EvaluationCounts computeCountsNer(Map<Integer, Integer> allGolds, Map<Integer, Set<Integer>> allResults) {
    EvaluationCounts ec = new EvaluationCounts();
    
    for(Integer begin: allGolds.keySet()) {
      Integer goldEnd = allGolds.get(begin);
      
      if (allResults.containsKey(begin) && allResults.get(begin).contains(goldEnd)) {
        ec.setTruePositives(ec.getTruePositives() + 1);
      }
      else {
        ec.setFalseNegatives(ec.getFalseNegatives() + 1);
      }
    }
    
    for (Integer begin : allResults.keySet()) {
      Set<Integer> ends = allResults.get(begin);
      for(int end: ends) {
        if (!allGolds.containsKey(begin) || !ends.contains(end)) {
          ec.setFalsePositives(ec.getFalsePositives() + 1);
        }
      }
    }
    return ec;
  }

  public static Map<String, SalientEntity> getSalientEntitiesFromJCas(JCas jCas) {
    Collection<SalientEntity> salientEntities = JCasUtil.select(jCas, SalientEntity.class);
    Map<String, SalientEntity> salientEntityMap = new HashMap<>();

    for(SalientEntity salientEntity : salientEntities) {
      salientEntityMap.put(salientEntity.getID(), salientEntity);
    }

    return salientEntityMap;
  }
  
  public static Map<String, Double> readResults(String input) throws FileNotFoundException, URISyntaxException {
    input = input + "disambiguationOutput/evaluation";
    Map<String, Double> result = new HashMap<>();
    File file = new File(input);
    if (!file.exists()) {
      file = ClassPathUtils.getFileFromClassPath(input);
    }
    Scanner in = new Scanner(new File(input));
    while (in.hasNextLine()) {
      String line = in.nextLine().trim();
    }
    boolean stop = false;
    in = new Scanner(new File(input));
    while (in.hasNextLine()) {
      String line = in.nextLine().trim();
      if (line.equals("Disambiguation")) {
        while (in.hasNextLine()) {
          line = in.nextLine();
          if (line.contains("TOTAL DOCS")) {
//            stop = true;
            break;
          }
          String[] sp = line.split(":");
          result.put(sp[0].trim(), Double.parseDouble(sp[1].trim()));
        }
      }
      if (line.equals("NER")) {
        while (in.hasNextLine()) {
          line = in.nextLine();
          if (line.contains("TOTAL DOCS")) {
            stop = true;
            break;
          }
          String[] sp = line.split(":");
          result.put("NER-" + sp[0].trim(), Double.parseDouble(sp[1].trim()));
        }
      }
      if (stop) {
        break;
      }
    }
    in.close();
    return result;
  }
  
  @Override
  public void close() throws Exception {
    analysedOutput.close();
  }
  
/* version changed not corrected
  public static void analysOutput(JCas jCas) throws IOException, CASException {
    docCnt++;
    analysedOutput.writeNext(new String[] {    
        "MentionBegin",
        "Mention", 
        "MentionExistInDB",
        "GoldEntity", 
        "GoldEntityExistInDB", 
        "GoldPosition", 
        "GoldEntityPositionInDB", 
        "ResultEntity",
        "ResultPositionInDB"});
    analysedOutput.flush();
    
    

    Map<Integer, Set<Entity>> entities = getGoldAidaAnnotationsFromJCas(jCas);
    Map<Integer, Set<Concept>> concepts = getGoldConceptAnnotationsFromJCas(jCas);
    Set<Integer> goldBegins = new HashSet<>();
    goldBegins.addAll(entities.keySet());
    goldBegins.addAll(concepts.keySet());
    
    Map<Integer, AidaEntity> aidaEntities = getAidaAnnotationsFromJCas(jCas);
    Map<Integer, ConceptEntity> conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    
    Map<Integer, Set<String>> allResults = new HashMap<Integer, Set<String>>();
    Map<Integer, Set<String>> allGolds = new HashMap<Integer, Set<String>>();
    
    for (Integer begin:goldBegins) {
      
      if (entities.containsKey(begin)) {
        for (Entity e :entities.get(begin)) {
<<<<<<< HEAD
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
=======
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
      }
      if (concepts.containsKey(begin)) {
        for (Concept e :concepts.get(begin)) {
<<<<<<< HEAD
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
=======
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
      }
      
      if (aidaEntities.containsKey(begin)) {
<<<<<<< HEAD
        String entityId = aidaEntities.get(begin).getID().substring(aidaEntities.get(begin).getID().indexOf(':') + 1);
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
      if (conceptEntitites.containsKey(begin)) {
        String entityId = conceptEntitites.get(begin).getID().substring(conceptEntitites.get(begin).getID().indexOf(':') + 1);
=======
        String entityId = aidaEntities.get(begin).getID().substring(aidaEntities.get(begin).getID().indexOf(':') + 1);
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
      if (conceptEntitites.containsKey(begin)) {
        String entityId = conceptEntitites.get(begin).getID().substring(conceptEntitites.get(begin).getID().indexOf(':') + 1);
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
    }
    
    for (Integer begin:goldBegins) {
      Set<String> goldIds = allGolds.get(begin);
      Set<String> resultIds = allResults.get(begin);
      
      if (resultIds != null && !resultIds.isEmpty()) {
        boolean found = false;
        for (String resultId:resultIds) {
          if (goldIds.contains(resultId)) {
            found = true;
            break;
          }
        }
        if (found) {
          continue;
        }
      }
      // if here: Not TP, so write it
      if (entities.containsKey(begin))
      for (Entity e:entities.get(begin)) {
        AidaEntity aentity = aidaEntities.get(begin);
        String resultEntityId = "";
        if (aentity != null) {
<<<<<<< HEAD
          resultEntityId = FactComponent.stripBrackets(aentity.getID());
=======
          resultEntityId = FactComponent.stripBrackets(aentity.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
        
        String mention = jCas.getDocumentText().substring(e.getBegin(), e.getEnd());
        mention = EntityLinkingManager.conflateToken(mention, true);
        Integer mentionBegin = e.getBegin();
        
        if (!allMentions.contains(mention)) continue; //don't print the ones that their mention is not available in db
        
<<<<<<< HEAD
        String goldId = FactComponent.stripBrackets(e.getID());
=======
        String goldId = FactComponent.stripBrackets(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        String position = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(goldId)) {
          position = allWikidataIds.get(goldId).name();
        }
        String resPosition = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(resultEntityId)) {
          resPosition = allWikidataIds.get(resultEntityId).name();
        }
        
        
        analysedOutput.writeNext(new String[] {     docCnt.toString() + "_" + mentionBegin.toString(),
                                                    mention,
                                                    allMentions.contains(mention) ? TRUE : FALSE,
                                                    goldId, 
                                                    allWikidataIds.containsKey(goldId) ? TRUE : FALSE,
                                                    EntityType.NAMED_ENTITY.name(),
                                                    position,
                                                    resultEntityId,
                                                    resPosition});
        analysedOutput.flush();
      }
      
      if (concepts.containsKey(begin))
      for (Concept e : concepts.get(begin)) {
        ConceptEntity centity = conceptEntitites.get(begin);
        String resultEntityId = "";
        if (centity != null) {
<<<<<<< HEAD
          resultEntityId = FactComponent.stripBrackets(centity.getID());
=======
          resultEntityId = FactComponent.stripBrackets(centity.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
        
        String mention = jCas.getDocumentText().substring(e.getBegin(), e.getEnd());
        mention = EntityLinkingManager.conflateToken(mention, false);
        Integer mentionBegin = e.getBegin();
        
        if (!allMentions.contains(mention)) continue; //don't print the ones that their mention is not available in db
        
<<<<<<< HEAD
        String goldId = FactComponent.stripBrackets(e.getID());
=======
        String goldId = FactComponent.stripBrackets(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        String position = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(goldId)) {
          position = allWikidataIds.get(goldId).name();
        }
        String resPosition = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(resultEntityId)) {
          resPosition = allWikidataIds.get(resultEntityId).name();
        }
        
        analysedOutput.writeNext(new String[] {     docCnt.toString() + "_" + mentionBegin.toString(),
                                                    mention,
                                                    allMentions.contains(mention) ? TRUE : FALSE,
                                                    goldId, 
                                                    allWikidataIds.containsKey(goldId) ? TRUE : FALSE,
                                                    EntityType.CONCEPT.name(),
                                                    position,
                                                    resultEntityId,
                                                    resPosition});
        analysedOutput.flush();
      }
    }
    
    
  }
  
  public static void analysOutput_all(JCas jCas) throws CASException, EntityLinkingDataAccessException, IOException {
    docCnt++;
    analysedOutput.writeNext(new String[] {    
                  "MentionBegin",  
                  "Mention", 
                  "MentionExistInDB",
                  "GoldEntity", 
                  "GoldEntityExistInDB", 
                  "GoldPosition", 
                  "GoldEntityPositionInDB", 
                  "ResultEntity",
                  "ResultPositionInDB"});
    analysedOutput.flush();
    
    Map<Integer, Set<Entity>> entities = getGoldAidaAnnotationsFromJCas(jCas);
    Map<Integer, AidaEntity> aidaentities = getAidaAnnotationsFromJCas(jCas);
    
    for(Integer begin : entities.keySet()) {
      AidaEntity aentity = aidaentities.get(begin);
      String resultEntityId = "";
      if (aentity != null) {
<<<<<<< HEAD
        resultEntityId = FactComponent.stripBrackets(aentity.getID());
=======
        resultEntityId = FactComponent.stripBrackets(aentity.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
      }
      
      for (Entity e : entities.get(begin)) {
        String mention = jCas.getDocumentText().substring(e.getBegin(), e.getEnd());
        mention = EntityLinkingManager.conflateToken(mention, true);
        Integer mentionBegin = e.getBegin();
<<<<<<< HEAD
        String goldId = FactComponent.stripBrackets(e.getID());
=======
        String goldId = FactComponent.stripBrackets(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        String position = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(goldId)) {
          position = allWikidataIds.get(goldId).name();
        }
        String resPosition = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(resultEntityId)) {
          resPosition = allWikidataIds.get(resultEntityId).name();
        }
        
        
        analysedOutput.writeNext(new String[] {     docCnt.toString() + "_" + mentionBegin.toString(),
                                                    mention,
                                                    allMentions.contains(mention) ? TRUE : FALSE,
                                                    goldId, 
                                                    allWikidataIds.containsKey(goldId) ? TRUE : FALSE,
                                                    EntityType.NAMED_ENTITY.name(),
                                                    position,
                                                    resultEntityId,
                                                    resPosition});
        analysedOutput.flush();
      }
    }
    
    Map<Integer, Set<Concept>> concepts = getGoldConceptAnnotationsFromJCas(jCas);
    Map<Integer, ConceptEntity> conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    
    for(Integer begin : concepts.keySet()) {
      ConceptEntity centity = conceptEntitites.get(begin);
      String resultEntityId = "";
      if (centity != null) {
<<<<<<< HEAD
        resultEntityId = FactComponent.stripBrackets(centity.getID());
=======
        resultEntityId = FactComponent.stripBrackets(centity.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
      }
      
      for (Concept e : concepts.get(begin)) {
        String mention = jCas.getDocumentText().substring(e.getBegin(), e.getEnd());
        mention = EntityLinkingManager.conflateToken(mention, false);
        Integer mentionBegin = e.getBegin();
<<<<<<< HEAD
        String goldId = FactComponent.stripBrackets(e.getID());
=======
        String goldId = FactComponent.stripBrackets(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        String position = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(goldId)) {
          position = allWikidataIds.get(goldId).name();
        }
        String resPosition = EntityType.UNKNOWN.name();
        if (allWikidataIds.containsKey(resultEntityId)) {
          resPosition = allWikidataIds.get(resultEntityId).name();
        }
        
        analysedOutput.writeNext(new String[] {     docCnt.toString() + "_" + mentionBegin.toString(),
                                                    mention,
                                                    allMentions.contains(mention) ? TRUE : FALSE,
                                                    goldId, 
                                                    allWikidataIds.containsKey(goldId) ? TRUE : FALSE,
                                                    EntityType.CONCEPT.name(),
                                                    position,
                                                    resultEntityId,
                                                    resPosition});
        analysedOutput.flush();
      }
    }
  }
  
  
  public static void analysOutputText(JCas jCas) throws CASException, EntityLinkingDataAccessException, IOException {
    docCnt++;
    analysedOutput.writeNext(new String[] {    
                  "word",  
                  "GoldEntity", 
                  "ResultEntity",
                  "Correct"
                  });
    analysedOutput.flush();
    
    Map<Integer, Set<Entity>> entities = getGoldAidaAnnotationsFromJCas(jCas);
    Map<Integer, Set<Concept>> concepts = getGoldConceptAnnotationsFromJCas(jCas);
    Set<Integer> goldBegins = new HashSet<>();
    goldBegins.addAll(entities.keySet());
    goldBegins.addAll(concepts.keySet());
    
    Map<Integer, AidaEntity> aidaEntities = getAidaAnnotationsFromJCas(jCas);
    Map<Integer, ConceptEntity> conceptEntitites = getConceptAnnotationsFromJCas(jCas);
    
    Map<Integer, Set<String>> allResults = new HashMap<Integer, Set<String>>();
    Map<Integer, Set<String>> allGolds = new HashMap<Integer, Set<String>>();
    
    for (Integer begin:goldBegins) {
      
      if (entities.containsKey(begin)) {
        for (Entity e :entities.get(begin)) {
<<<<<<< HEAD
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
=======
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
      }
      if (concepts.containsKey(begin)) {
        for (Concept e :concepts.get(begin)) {
<<<<<<< HEAD
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
=======
          allGolds.computeIfAbsent(begin, k -> new HashSet<>()).add(e.getID());
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        }
      }
      
      if (aidaEntities.containsKey(begin)) {
<<<<<<< HEAD
        String entityId = aidaEntities.get(begin).getID().substring(aidaEntities.get(begin).getID().indexOf(':') + 1);
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
      if (conceptEntitites.containsKey(begin)) {
        String entityId = conceptEntitites.get(begin).getID().substring(conceptEntitites.get(begin).getID().indexOf(':') + 1);
=======
        String entityId = aidaEntities.get(begin).getID().substring(aidaEntities.get(begin).getID().indexOf(':') + 1);
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
      if (conceptEntitites.containsKey(begin)) {
        String entityId = conceptEntitites.get(begin).getID().substring(conceptEntitites.get(begin).getID().indexOf(':') + 1);
>>>>>>> bc78a6ed03294e479d88c9a91ae74756ba98ba32
        allResults.computeIfAbsent(begin, k -> new HashSet<>()).add(entityId);
      }
    }
    
    List<Integer> begins = new ArrayList<>();
    Collection<Token> tokens = select(jCas, Token.class);
    Map<Integer, String> tokens_map = new HashMap<>();
    for (Token t : tokens) {
      begins.add(t.getBegin());
      tokens_map.put(t.getBegin(), t.getCoveredText());
    }
    Collections.sort(begins);
    for (Integer begin : begins) {
      if (goldBegins.contains(begin)) {
        boolean found = false;
        for (String r:allResults.get(begin)) {
          if (allGolds.get(begin).contains(r)) {
            found = true;
            break;
          }
        }
        analysedOutput.writeNext(new String[] {
            tokens_map.get(begin),
            allGolds.get(begin).toString(),
            allResults.get(begin).toString(),
            found?"YES":"NO"
        });
      }
      else {
        analysedOutput.writeNext(new String[] {
            tokens_map.get(begin),
            "",
            "",
            ""
        });
      }
      analysedOutput.flush();
    }
    analysedOutput.writeNext(new String[] {
        "------",
        "",
        "",
        ""
    });
    analysedOutput.flush();
  }
  */
}
