package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



public class FilterFacts extends JCasAnnotator_ImplBase {
  public static final String PARAM_WORDS = "words";
  @ConfigurationParameter(name = PARAM_WORDS)
  private String words;

  public static final String PARAM_ENTITIES = "entities";
  @ConfigurationParameter(name = PARAM_ENTITIES)
  private String entities;

  public static Set<String> wordsToFilter = new HashSet<>();
  public static Set<String> entitiesToFilter = new HashSet<>();;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    synchronized (FilterEntitiesByType.class) {
      if(words != null) {
        for(String type: words.split(",")) {
          wordsToFilter.add(type.toLowerCase());
        }
      }

      if(entities != null) {
        for(String type: entities.split(",")) {
          entitiesToFilter.add(type.toLowerCase());
        }
      }
    }
  }

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    Collection<OpenFact> facts = JCasUtil.select(jCas, OpenFact.class);
    try {
      for(OpenFact fact: facts) {
        boolean containsWord = containsWord(fact);
        boolean containsEntity = containsEntity(fact);
        if (!containsWord && !containsEntity) {
          fact.removeFromIndexes();
        }
      }
    } catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
    }
  }

  private boolean containsEntity(OpenFact fact) {
    boolean containsEntitySubject = containsEntity(fact.getSubject());
    boolean containsEntityRelation = containsEntity(fact.getRelation());
    boolean containsEntityObject = containsEntity(fact.getObject());

    if(!containsEntitySubject
        && !containsEntityRelation
        && !containsEntityObject) {
      return false;
    } else {
      return true;
    }
  }

  private boolean containsEntity(Constituent subject) {
    for(String entity: entitiesToFilter) {
      Collection<AidaEntity> entities = JCasUtil.selectCovered(AidaEntity.class, subject);
      for(AidaEntity ae: entities) {
        if(ae.getID().equals(entity)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean containsWord(OpenFact fact) {
    boolean containsWordSubject = containsWord(fact.getSubject());
    boolean containsWordRelation = containsWord(fact.getRelation());
    boolean containsWordsObject = containsWord(fact.getObject());

    if(!containsWordSubject
        && !containsWordRelation
        && !containsWordsObject) {
      return false;
    } else {
      return true;
    }
  }

  private boolean containsWord(Constituent constituent) {
    for(String word: wordsToFilter) {
      if(constituent.getCoveredText().toLowerCase().contains(word.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}
