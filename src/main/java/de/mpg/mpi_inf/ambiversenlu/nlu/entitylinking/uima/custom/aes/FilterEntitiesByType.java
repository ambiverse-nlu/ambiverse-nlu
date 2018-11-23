package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



public class FilterEntitiesByType extends JCasAnnotator_ImplBase {

  public static final String PARAM_KG = "kgTypes";
  @ConfigurationParameter(name = PARAM_KG)
  private String kgTypes;

  public static final String PARAM_NER = "nerTypes";
  @ConfigurationParameter(name = PARAM_NER)
  private String nerTypes;

  public static Set<String> typesToFilterKG = new HashSet<>();
  public static Set<String> typesToFilterNER = new HashSet<>();;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    synchronized (FilterEntitiesByType.class) {
      if(kgTypes != null) {
        for(String type: kgTypes.split(",")) {
          typesToFilterKG.add(type.toLowerCase());
        }
      }

      if(nerTypes != null) {
        for(String type: nerTypes.split(",")) {
          typesToFilterNER.add(type.toLowerCase());
        }
      }
    }
  }

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    Collection<AidaEntity> entities = JCasUtil.select(jCas, AidaEntity.class);
    for(AidaEntity entity: entities) {
      String id = entity.getID().substring(entity.getID().indexOf(":")+1);
      try {
        int intkb = DataAccess.getInternalIdForKBEntity(KBIdentifiedEntity.getKBIdentifiedEntity(id, "YAGO3"));
        Set<Type> types = DataAccess.getTypes(new Entity(id, "YAGO3", intkb));
        if(!types.isEmpty()) {
          boolean typeFound = findTypes(typesToFilterKG, typesToFilterNER, types);
          if(!typeFound) {
            entity.removeFromIndexes();
          }
        } else {
          Collection<NamedEntity> nes = JCasUtil.selectCovered(jCas, NamedEntity.class, entity);
          for(NamedEntity ne: nes) {
            if(!typesToFilterNER.contains(ne.getValue().toLowerCase())) {
              entity.removeFromIndexes();
              break;
            }
          }
        }
      } catch (EntityLinkingDataAccessException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }

  private boolean findTypes(Set<String> typesToFilterKG, Set<String> typesToFilterNER, Set<Type> types) {
    for (Type type: types) {
      for(String filter: typesToFilterKG) {
        if(type.getName().toLowerCase().contains(filter)) {
          return true;
        }
      }
    }
    return false;
  }
}
