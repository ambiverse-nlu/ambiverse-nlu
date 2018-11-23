package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntityContextEntry;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Yago3RelationBasedEntitiesContextDataProvider extends EntitiesContextEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3RelationBasedEntitiesContextDataProvider.class);

  private String relationName;

  private String contextSourceName;

  private boolean mapYagoIdsToOtherKBIds = false;

  private YagoIdsMapper yagoIdsMapper;

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  public Yago3RelationBasedEntitiesContextDataProvider(YAGO3Reader yago3Reader, String relationName, String contextSourceName) throws IOException {
    this.yago3Reader = yago3Reader;
    this.relationName = relationName;
    if (relationName.equals(Yago3WikipediaCategoryEntitiesContextDataProvider.RELATION_NAME)) {
      throw new IllegalStateException("Please use Yago3WikipediaCategoryEntitiesContextDataProvider for " + Yago3WikipediaCategoryEntitiesContextDataProvider.RELATION_NAME);
    }
    this.contextSourceName = contextSourceName;
  }

  public Yago3RelationBasedEntitiesContextDataProvider(YAGO3Reader yago3Reader, String relationName, String contextSourceName,
      YagoIdsMapper yagoIdsMapper, String knowledgebaseName) throws IOException {
    this(yago3Reader, relationName, contextSourceName);
    this.yagoIdsMapper = yagoIdsMapper;
    this.knowledgebaseName = knowledgebaseName;
    mapYagoIdsToOtherKBIds = true;
  }

  private Map<String, Set<EntityContextEntry>> getEntitiesContexts() throws IOException {
    Map<String, Set<EntityContextEntry>> entitiesContext = new HashMap<>();

    List<Fact> facts = yago3Reader.getFacts(relationName);

    if (facts == null) {
      logger.error("No facts found for relation: " + relationName);
      throw new IllegalStateException("No facts found for relation: " + relationName);
    } else {
      logger.info("Extracting Entities context from relation: " + relationName);
    }

    for (Fact fact : facts) {
      String entity = fact.getSubject();
      String languageString = FactComponent.getLanguageOfString(fact.getObject());

      Language language;
      if (languageString == null || "".equals(languageString)) {
        language = Language.getLanguageForString(languageString, "en");
      } else if (Language.isActiveLanguage(languageString)) {
        language = Language.getLanguageForString(languageString);
      } else {
        continue;
      }
      if (mapYagoIdsToOtherKBIds) {
        entity = yagoIdsMapper.mapFromYagoId(entity);
        if (entity == null) continue;
      }
      String keyphrase = fact.getObjectAsJavaString();

      Set<EntityContextEntry> entityContext = entitiesContext.get(entity);
      if (entityContext == null) {
        entityContext = new HashSet<>();
        entitiesContext.put(entity, entityContext);
      }
      if(!Utils.shouldKeepToken(keyphrase)) {
        continue;
      }
      EntityContextEntry entityContextEntry = new EntityContextEntry(keyphrase, contextSourceName, language);
      entityContext.add(entityContextEntry);
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
