package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntityContextEntry;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.yago3.Yago3CategoryDictionary;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Yago3WikipediaCategoryEntitiesContextDataProvider extends EntitiesContextEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3WikipediaCategoryEntitiesContextDataProvider.class);

  public final static String RELATION_NAME = YAGO3RelationNames.hasWikipediaCategory;

  private String contextSourceName;

  private boolean mapYagoIdsToOtherKBIds = false;

  private YagoIdsMapper yagoIdsMapper;

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  private Map<String, Yago3CategoryDictionary> language2CatDictionaryMap;

  public Yago3WikipediaCategoryEntitiesContextDataProvider(YAGO3Reader yago3Reader, String contextSourceName) throws IOException {
    this.yago3Reader = yago3Reader;
    this.contextSourceName = contextSourceName;
    language2CatDictionaryMap = new HashMap<>();
    for (Language language : Language.activeLanguages()) {
      String languageName = language.name();
      language2CatDictionaryMap.put(languageName, new Yago3CategoryDictionary(yago3Reader, languageName));
    }

  }

  private Map<String, Set<EntityContextEntry>> getEntitiesContexts() throws IOException {
    Map<String, Set<EntityContextEntry>> entitiesContext = new HashMap<>();

    List<Fact> facts = yago3Reader.getFacts(RELATION_NAME);

    if (facts == null) {
      logger.error("No facts found for relation: " + RELATION_NAME);
      throw new IllegalStateException("No facts found for relation: " + RELATION_NAME);
    } else {
      logger.info("Extracting Entities context from relation: " + RELATION_NAME);
    }

    for (Fact fact : facts) {
      String entity = fact.getSubject();
      String languageString = FactComponent.getLanguageOfEntity(fact.getObject());
      Language language = Language.getLanguageForString(languageString, "en");

      if (mapYagoIdsToOtherKBIds) {
        entity = yagoIdsMapper.mapFromYagoId(entity);
        if (entity == null) continue;
      }
      String keyphrase = FactComponent.stripCat(fact.getObject());

      Set<EntityContextEntry> entityContext = entitiesContext.get(entity);
      if (entityContext == null) {
        entityContext = new HashSet<>();
        entitiesContext.put(entity, entityContext);
      }
      for (Language activeLanguage : Language.activeLanguages()) {
        String activeLanguageName = activeLanguage.name();
        if (!activeLanguageName.equals("en")) {
          String translatedKeyphrase = language2CatDictionaryMap.get(activeLanguageName).fromEnglish(keyphrase);
          if (translatedKeyphrase == null) {
            continue;
          }
          keyphrase = translatedKeyphrase;
        }
        if (keyphrase == null) {
          continue;
        }
        if(!Utils.shouldKeepToken(keyphrase)) {
          continue;
        }
        EntityContextEntry entityContextEntry = new EntityContextEntry(keyphrase, contextSourceName, language);
        entityContext.add(entityContextEntry);
      }
    }
    logger.info("Done");
    return entitiesContext;
  }

  @Override public Iterator<Entry<String, Set<EntityContextEntry>>> iterator() {
    try {
      return getEntitiesContexts().entrySet().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override public String getKnowledgebaseName() {
    return knowledgebaseName;
  }

}
