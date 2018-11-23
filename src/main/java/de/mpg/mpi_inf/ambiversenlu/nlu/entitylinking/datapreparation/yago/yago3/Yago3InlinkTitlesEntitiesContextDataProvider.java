package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntityContextEntry;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.yago3.Yago3EntityDictionary;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yago3InlinkTitlesEntitiesContextDataProvider extends EntitiesContextEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3InlinkTitlesEntitiesContextDataProvider.class);

  private String contextSourceName;

  private Pattern pattern = Pattern.compile("(.*?) \\(.+?\\)");

  private boolean mapYagoIdsToOtherKBIds = false;

  private YagoIdsMapper yagoIdsMapper;

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  private Map<String, Yago3EntityDictionary> language2EntityDictionaryMap = null;

  public Yago3InlinkTitlesEntitiesContextDataProvider(YAGO3Reader yago3Reader, String contextSourceName) throws IOException {
    this.yago3Reader = yago3Reader;
    this.contextSourceName = contextSourceName;
    initEntityDictionaries();
  }

  public Yago3InlinkTitlesEntitiesContextDataProvider(YAGO3Reader yago3Reader, String contextSourceName, YagoIdsMapper yagoIdsMapper,
      String knowledgebaseName) throws IOException {
    this(yago3Reader, contextSourceName);
    this.yagoIdsMapper = yagoIdsMapper;
    this.knowledgebaseName = knowledgebaseName;
    mapYagoIdsToOtherKBIds = true;
  }

  private void initEntityDictionaries() throws IOException {
    language2EntityDictionaryMap = new HashMap<>();
    for (Language language : Language.activeLanguages()) {
      String languageName = language.name();
      language2EntityDictionaryMap.put(languageName, new Yago3EntityDictionary(yago3Reader, languageName));
    }
  }

  private Map<String, Set<EntityContextEntry>> getEntitiesContexts() throws IOException {
    logger.info("Reading " + YAGO3RelationNames.hasInternalWikipediaLinkTo);
    Map<String, Set<EntityContextEntry>> entitiesContext = new HashMap<String, Set<EntityContextEntry>>();

    List<Fact> hasInternalLinkToSet = yago3Reader.getFacts(YAGO3RelationNames.hasInternalWikipediaLinkTo);

    for (Fact entry : hasInternalLinkToSet) {
      String entity = entry.getObject();
      Language language = Language.getLanguageForString(FactComponent.getLanguageOfString(entry.getObject()), "en");
      if (mapYagoIdsToOtherKBIds) {
        entity = yagoIdsMapper.mapFromYagoId(entity);
        if (entity == null) continue;
      }
      String inlinkSourceEntity = entry.getSubject();





      String sourceEntityLangauge = Yago3Util.getEntityLanguage(inlinkSourceEntity);

      //translate the inlink title to different languages
      Set<String> keyphrases = new HashSet<>();
      for (Language activeLanguage : Language.activeLanguages()) {
        String activeLanguageName = activeLanguage.name();
        Yago3EntityDictionary dictionary = language2EntityDictionaryMap.get(activeLanguageName);
        //we have 4 cases based on the source entity language (the keyphraseLanguage)
        // and the target language
        if (activeLanguageName.equals("en") && sourceEntityLangauge.equals("en")) {
          //both English, no translation needed
          keyphrases.add(cleanEntityName(FactComponent.unYagoEntity(inlinkSourceEntity)));
        } else if (!activeLanguageName.equals("en") && sourceEntityLangauge.equals("en")) {
          // the target language is not English, try to translate if possible
          String translatedInlinkSourceEntity = dictionary.fromEnglish(inlinkSourceEntity);
          if (translatedInlinkSourceEntity != null) {
            keyphrases.add(cleanEntityName(FactComponent.unYagoEntity(translatedInlinkSourceEntity)));
          }
        } else if (activeLanguageName.equals("en") && !sourceEntityLangauge.equals("en")) {
          //the target language is English, the source entity is not English
          //cannot be translated, because it it had an English counter part, it would been
          //used instead of the non-English entity
          continue;
        } else if (!activeLanguageName.equals("en") && !sourceEntityLangauge.equals("en")) {
          //both target and entity languages are not English, try to translate twice if possible.
          //first to English, and then to the targetLangauge
          Yago3EntityDictionary sourceToEnglishDictionary = language2EntityDictionaryMap.get(sourceEntityLangauge);
          if (sourceToEnglishDictionary == null) {
            sourceToEnglishDictionary = new Yago3EntityDictionary(yago3Reader, sourceEntityLangauge);
            language2EntityDictionaryMap.put(sourceEntityLangauge, sourceToEnglishDictionary);
          }
          String englishSourceEntity = sourceToEnglishDictionary.toEnglish(inlinkSourceEntity);
          if (englishSourceEntity == null) {
            continue;
          }
          Yago3EntityDictionary englishToTargetDictionary = language2EntityDictionaryMap.get(activeLanguageName);
          String translatedInlinkSourceEntity = englishToTargetDictionary.fromEnglish(englishSourceEntity);
          if (translatedInlinkSourceEntity != null) {
            keyphrases.add(cleanEntityName(FactComponent.unYagoEntity(translatedInlinkSourceEntity)));
          }
        }
      }

      Set<EntityContextEntry> entityContext = entitiesContext.get(entity);
      if (entityContext == null) {
        entityContext = new HashSet<>();
        entitiesContext.put(entity, entityContext);
      }
      for (String keyphrase : keyphrases) {
        if(!Utils.shouldKeepToken(keyphrase)) {
          continue;
        }
        EntityContextEntry entityContextEntry = new EntityContextEntry(keyphrase, contextSourceName, language);
        entityContext.add(entityContextEntry);
      }
    }

    logger.info("Done!");
    return entitiesContext;
  }

  private String cleanEntityName(String entityName) {
    // Compile and use regular expression
    Matcher matcher = pattern.matcher(entityName);
    boolean matchFound = matcher.find();

    if (!matchFound) return entityName;
    else return matcher.group(1);
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
