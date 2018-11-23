package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class ConceptEntityDatasetReader extends ResourceCollectionReaderBase {

  private static final int WORD = 0;
  private static final int POSITION = 1;
  private static final int LEMMA = 2;
  private static final int ENTITIES = 3;
  private static final int TYPE = 4; // Here type means if it is a Concept or Entity
 
  /**
   * Character encoding of the input data.
   */
  public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
  @ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
  private String encoding;
  
  /**
   * The language.
   */
  public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
  @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
  private String language;

  public static final String PARAM_MANUAL_TOKENS_NER = "evaluateRecognition";
  @ConfigurationParameter(name = PARAM_MANUAL_TOKENS_NER, mandatory = false)
  private boolean evaluateRecognition;
  
  public static final String PARAM_EVALUATE_CONCEPTS = "evaluateConcepts";
  @ConfigurationParameter(name = PARAM_EVALUATE_CONCEPTS, mandatory = false)
  private boolean evaluateConcepts;
  
  public static final String PARAM_EVALUATE_NE = "evaluateNE";
  @ConfigurationParameter(name = PARAM_EVALUATE_NE, mandatory = false)
  private boolean evaluateNE;

  public static final String PARAM_SINGLE_FILE = "isOneFile";
  @ConfigurationParameter(name = PARAM_SINGLE_FILE, mandatory = false)
  private boolean isOneFile;
  
  public static final String PARAM_ADD_MENTION_TO_BOTH = "addMentionToBoth";
  @ConfigurationParameter(name = PARAM_ADD_MENTION_TO_BOTH, mandatory = false)
  private boolean addMentionToBoth;
  
//  public static final String PARAM_USE_WIKIDATA_ID = "useWikidataId";
//  @ConfigurationParameter(name = PARAM_USE_WIKIDATA_ID, mandatory = false)
//  private boolean useWikidataId;

  public static final String PARAM_TRANINING_SIM_WEIGHTS = "trainingSimilarityWeights";
  @ConfigurationParameter(name = PARAM_TRANINING_SIM_WEIGHTS, mandatory = false)
  private boolean trainingSimilarityWeights;
  
  private Scanner reader;
  private boolean firstDocument = true;
  
  private static Map<String, String> wikidataIdsToEntities;
  
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    if (trainingSimilarityWeights) {
      try {
        wikidataIdsToEntities = new HashMap<>();
        List<String> wdIds = DataAccess.getAllWikidataIds();
        Map<String, Integer> internalEntityIds = DataAccess.getInternalIdsfromWikidataIds(wdIds);
        int[] tmp = new int[internalEntityIds.values().size()];
        int i = 0;
        for (Integer id:internalEntityIds.values()) {
          tmp[i++] = id;
        }
        TIntObjectHashMap<KBIdentifiedEntity> entities = DataAccess.getKnowlegebaseEntitiesForInternalIds(tmp);
        for (String wikidataId:wdIds) {
          if (internalEntityIds.containsKey(wikidataId)) {
            Integer internalId = internalEntityIds.get(wikidataId);
            if (entities.contains(internalId)) {
              KBIdentifiedEntity e = entities.get(internalId);
              wikidataIdsToEntities.put("<" + wikidataId + ">", e.getIdentifier());
            }
          }
        }
      } catch (EntityLinkingDataAccessException e) {
        System.err.println("Can not load wikidataIds for entities");
        e.printStackTrace();
      }
    }
  }
  
  private void convert(CAS aCAS) throws IOException, CollectionException, AnalysisEngineProcessException, CASException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    }
    catch (CASException e) {
      throw new CollectionException(e);
    }
    
    JCasBuilder doc = new JCasBuilder(jcas);
    
    JCas goldView = ViewCreatorAnnotator.createViewSafely(jcas, "gold");
    
    List<String[]> words;
    while((words = readSentence()) != null){
      if(words.isEmpty()){
        continue;
      }

      int sentenceBegin = doc.getPosition();
      int sentenceEnd = sentenceBegin;
      
      NamedEntity ne = null;
      Annotation cc = null;
      
      Boolean train_concepts = null;
      Boolean train_nes = null;
      if (trainingSimilarityWeights) {
        train_concepts = evaluateConcepts;
        train_nes = evaluateNE;
        System.out.println("Training Concepts: " + train_concepts);
      }

      Entity e = null;
      Concept c = null;
      Unknown ue = null;
      Unknown uc = null;
      
      
      for(String[] word : words){
        Token token = doc.add(word[WORD], Token.class);
        sentenceEnd = token.getEnd();
        if(language == null ||
            !language.equals("zh")) {
          doc.add(" ");
        }

        token.addToIndexes();
        
        if(word.length > 1) {
          if (addMentionToBoth || !word[TYPE].equals(EntityType.NAMED_ENTITY.name()))  // Concept/ Both/ Unknown
          {
            if (word[POSITION].equals("B")) {
              if (trainingSimilarityWeights) {
                cc = (ConceptMention) new ConceptMention(jcas);
              }
              else {
                cc = (ConceptMentionCandidate) new ConceptMentionCandidate(jcas);
              }
              cc.setBegin(token.getBegin());
              cc.setEnd(token.getEnd());
              if (!trainingSimilarityWeights) {
                ((ConceptMentionCandidate) cc).setConceptCandidate(word[LEMMA]);
              }
              if(!evaluateRecognition && (train_concepts == null || train_concepts)) {
                cc.addToIndexes();
              }
              
              if (evaluateConcepts) {
                String[] entities = word[ENTITIES].split(",");
                for (String entity : entities) {
                  if (word[TYPE].equals(EntityType.CONCEPT.name())) {
                    c = new Concept(goldView);
                    if (trainingSimilarityWeights && wikidataIdsToEntities.containsKey(entity)) {
                      c.setID(wikidataIdsToEntities.get(entity));
                    }
                    else {
                      c.setID(entity);
                    }
                    c.setBegin(token.getBegin());
                    c.setEnd(token.getEnd());
                    c.addToIndexes();
                  } else {
                    uc = new Unknown(goldView);
                    if (trainingSimilarityWeights && wikidataIdsToEntities.containsKey(entity)) {
                      uc.setID(wikidataIdsToEntities.get(entity));
                    }
                    else {
                      uc.setID(entity);
                    }
                    uc.setBegin(token.getBegin());
                    uc.setEnd(token.getEnd());
                    uc.addToIndexes();
                  }

                }
              }
            } else {
                cc.setEnd(token.getEnd());
                if(evaluateConcepts) {
                  if (word[TYPE].equals(EntityType.CONCEPT.name())) {
                    c.setEnd(token.getEnd());
                  }
                  else {
                    uc.setEnd(token.getEnd());
                  }
                }
              }
            }
          if (addMentionToBoth || !word[TYPE].equals(EntityType.CONCEPT.name()))  // NamedEntity/ Both/ Unknown
          {
            if (word[POSITION].equals("B")) {
              ne = new NamedEntity(jcas);
              ne.setBegin(token.getBegin());
              ne.setEnd(token.getEnd());
              if(!evaluateRecognition && (train_nes == null || train_nes)) {
                ne.addToIndexes();
              }

              if(evaluateNE) {
                String[] entities = word[ENTITIES].split(",");
                for (String entity : entities) {
                  if (word[TYPE].equals(EntityType.NAMED_ENTITY.name())) {
                    e = new Entity(goldView);
                    if (trainingSimilarityWeights && wikidataIdsToEntities.containsKey(entity)) {
                      e.setID(wikidataIdsToEntities.get(entity));
                    }
                    else {
                      e.setID(entity);
                    }
                    e.setBegin(token.getBegin());
                    e.setEnd(token.getEnd());
                    e.addToIndexes();
                    
                  } else {
                    ue = new Unknown(goldView);
                    if (trainingSimilarityWeights && wikidataIdsToEntities.containsKey(entity)) {
                      ue.setID(wikidataIdsToEntities.get(entity));
                    }
                    else {
                      ue.setID(entity);
                    }
                    ue.setBegin(token.getBegin());
                    ue.setEnd(token.getEnd());
                    ue.addToIndexes();
                  }

                }
              }
            } else {
                ne.setEnd(token.getEnd());
                if(evaluateNE) {
                  if (word[TYPE].equals(EntityType.NAMED_ENTITY.name())) {
                    e.setEnd(token.getEnd());
                  }
                  else {
                    ue.setEnd(token.getEnd());
                  }
                }
              }
          }
        }
      }
      
      Sentence sentence = new Sentence(jcas, sentenceBegin, sentenceEnd);
      sentence.addToIndexes();
      doc.add("\n");
    }
    doc.close();
  }
  
  /**
   * Read a single sentence.
   */
  private List<String[]> readSentence()
      throws IOException
  {
    if(!reader.hasNextLine()) {
      return null;
    }
    List<String[]> words = new ArrayList<>();
    String line;
    while(reader.hasNextLine()) {
      line = reader.nextLine();
      if(line.contains("DOCSTART")) {
        if(!firstDocument && isOneFile) {
          return null;
        } else {
          firstDocument = false;
        }
        if(reader.hasNextLine()) {
          line = reader.nextLine();
        } else {
          return null;
        }
      }
      if (StringUtils.isBlank(line)) {
        break; // End of sentence
      }
      String[] fields = line.split("\t");
      words.add(fields);
    }
    return words;
  }

  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    if(!isOneFile || reader == null) {
      Resource res = nextFile();
      initCas(aCAS, res);
      reader = new Scanner(new InputStreamReader(res.getInputStream(), encoding));
    }
    if (language != null) {
      aCAS.setDocumentLanguage(language);
    }
    try {
      convert(aCAS);
    } catch (AnalysisEngineProcessException e) {
      throw new RuntimeException(e);
    } catch (CASException e) {
      e.printStackTrace();
    }
    
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    if(isOneFile && reader != null && reader.hasNextLine()) {
      return true;
    } else {
      reader = null;
      return super.hasNext();
    }
  }


  @Override
  public void destroy() {
    IOUtils.closeQuietly(reader);
  }
  
  
  public static void main(String[] args) {
    
  }
}
